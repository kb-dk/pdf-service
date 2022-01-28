package dk.kb.pdfservice.api.impl;

import com.google.common.util.concurrent.Striped;
import com.google.common.util.concurrent.StripedFactory;
import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.alma.MarcClient;
import dk.kb.pdfservice.alma.PdfInfo;
import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.footer.CopyrightFooterInserter;
import dk.kb.pdfservice.titlepage.PdfTitlePageCleaner;
import dk.kb.pdfservice.titlepage.PdfTitlePageCreator;
import dk.kb.pdfservice.titlepage.PdfTitlePageInserter;
import dk.kb.pdfservice.utils.PdfUtils;
import dk.kb.pdfservice.webservice.exception.InternalServiceObjection;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceObjection;
import dk.kb.pdfservice.webservice.exception.ServiceObjection;
import dk.kb.util.other.NamedThread;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;

import static dk.kb.pdfservice.utils.Objections.object;

/**
 * pdf-service
 */
public class PdfServiceApiServiceImpl implements PdfServiceApi {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final Logger downloadLogger = LoggerFactory.getLogger("DownloadLogger");
    
    /* How to access the various web contexts. See https://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-Contextannotations */
    @Context
    private transient UriInfo uriInfo;
    
    @Context
    private transient SecurityContext securityContext;
    
    @Context
    private transient HttpHeaders httpHeaders;
    
    @Context
    private transient Providers providers;
    
    @Context
    private transient Request request;
    
    @Context
    private transient ContextResolver<?> contextResolver;
    
    @Context
    private transient HttpServletRequest httpServletRequest;
    
    @Context
    private transient HttpServletResponse httpServletResponse;
    
    @Context
    private transient ServletContext servletContext;
    
    @Context
    private transient ServletConfig servletConfig;
    
    @Context
    private transient MessageContext messageContext;
    
    /**
     * Each key is mapped to as tripe. Num of stripes are the number of possible locks
     * So this means that at most 1023 locks can be handled at the same time
     * We do not use the special features of ReentrantLock, but it basically means that the same thread can lock a
     * Reentrant Lock multiple times, and must unlock the same number of times to release it
     * When fair=true locks favor granting access to the longest-waiting thread.
     */
    private static final Striped<ReadWriteLock> stripedLock
            = StripedFactory.readWriteLockLazyWeak(1023, true);
    
    
    /**
     * Request pdf file with copyright info added
     *
     * @param requestedPdfFile : a pdf file with a name like {barcode}-something.pdf
     * @return <ul>
     *         <li>code = 200, message = "A pdf with attached page", response = String.class</li>
     *         </ul>
     * @throws ServiceObjection when other http codes should be returned
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other
     *         codes
     **/
    @Override
    public StreamingOutput getPdf(String requestedPdfFile) {
        
        try (NamedThread namedThread = NamedThread.postfix(requestedPdfFile)) {
    
            //This is the file to return to the use user. It might not exist yet
            File copyrightedPdfFile = getTempFile(requestedPdfFile);
    
            //1. Lock for this filename, so only one user can use it
            final ReadWriteLock readWriteLock = stripedLock.get(requestedPdfFile);
    
            //First we acquire the read lock, to be able to read the file
            readWriteLock.readLock().lock();
            //state: w=0,r=1
            try {
                boolean useTempPdf = canUseTempPdf(copyrightedPdfFile);
        
                //The copyrighted PDF is not usable, so it must be regenerated
                if (!useTempPdf) {
            
                    upgradeToWriteLock(readWriteLock);
                    //state: w=1,r=0,
                    try {
                        //recheck that somebody did not update the pdf beneath us while we were waiting for the write lock
                        if (!canUseTempPdf(copyrightedPdfFile)) {
                            //recreate the copyrighted PDF
                            createCopyrightedPDF(requestedPdfFile, copyrightedPdfFile);
                        }
                        //If we got to here, everything worked
                    } finally {
                        downgradeToReadLock(readWriteLock);
                        //w=0,r=1
                    }
                }
                //w=0,r=1, no matter if we went through the if or not
                return returnPdfFile(copyrightedPdfFile);
        
            } catch (Exception e) {
                log.error("Exception", e);
                return object(() -> handleObjections(e, requestedPdfFile));
            } finally {
                //And we can release the readlock after the the method is complete
                readWriteLock.readLock().unlock();
                //w=0,r=0
            }
        }
    }
    
    
    private void downgradeToReadLock(ReadWriteLock readWriteLock) {
        //Downgrade by acquiring read lock before releasing write lock
        readWriteLock.readLock().lock();
        //Now we release the write lock
        readWriteLock.writeLock().unlock();
    }
    
    private void upgradeToWriteLock(ReadWriteLock readWriteLock) {
        //Must release read lock before acquiring write lock
        readWriteLock.readLock().unlock();
        //acquire write lock. This CAN take a while
        readWriteLock.writeLock().lock();
    }
    
    private void createCopyrightedPDF(String pdfFileString, File readyPdfFile) {
        //4. otherwise create
        final File sourcePdfFile = getPdfFile(pdfFileString);
        
        String barcode = sourcePdfFile.getName().split("[-._]", 2)[0];
        PdfInfo pdfInfo = MarcClient.getPdfInfo(barcode);
    
        //System.out.println(JSON.toJson(pdfInfo));
        //TODO suppressed records should be available?
    
        //TODO configurable produceApron module, so we can insert another
        try (InputStream apronFile = produceApron(pdfFileString, pdfInfo);
             ClosablePair<InputStream,PDDocumentInformation> requestedPDF = cleanOldApronPages(sourcePdfFile, pdfInfo);
             InputStream completePDF = PdfTitlePageInserter.mergeApronWithPdf(apronFile, requestedPDF.getLeft(), requestedPDF.getRight(), pdfInfo);
             OutputStream pdfFileOnDisk = new FileOutputStream(readyPdfFile)) {
            log.debug("Finished merging documents for {}", pdfFileString);
            IOUtils.copy(completePDF, pdfFileOnDisk);
            log.info("Finished returning pdf {}", pdfFileString);
        } catch (IOException e) {
            log.error("Failed for {}", pdfFileString, e);
            object(() -> new InternalServiceObjection("Unknown failure for " + pdfFileString, e));
        }
    }
    
    
    @Nonnull
    private File getPdfFile(String pdfFileString) {
        List<String> pdfSourcePaths = ServiceConfig.getPdfSourcePath();
        return pdfSourcePaths.stream()
                             .map(File::new)
                             .filter(File::isDirectory)
                             .map(dir -> new File(dir, new File(pdfFileString).getName()))
                             .filter(this::isUsable)
                             .findFirst()
                             .orElseThrow(() -> new NotFoundServiceObjection("Failed to find pdf file '"
                                                                             + pdfFileString
                                                                             + "'"));
    }
    
    
    private File getTempFile(String requestedPdfFile) {
        File file = new File(requestedPdfFile);
        return new File(ServiceConfig.getPdfTempPath(), file.getName());
    }
    
    private boolean canUseTempPdf(File readyPdfFile) {
        
        //2. Check if file exists
        if (isUsable(readyPdfFile)) {
            Instant pdfLastModified = Instant.ofEpochMilli(readyPdfFile.lastModified());
            TemporalAmount maxAllowedAge = ServiceConfig.getMaxAgeTempPdf();
            //Check if the file is not too old
            //3. if good, return the ready file
            return !pdfLastModified.plus(maxAllowedAge).isBefore(Instant.now());
        }
        return false;
    }
    
    
    private boolean isUsable(File readyPdfFile) {
        return readyPdfFile != null &&
               readyPdfFile.exists() &&
               readyPdfFile.isFile() &&
               readyPdfFile.canRead() &&
               readyPdfFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }
    
    @Nonnull
    private StreamingOutput returnPdfFile(File readyPdfFile) throws IOException {
        //buffer the file here. This allows us to release any locks BEFORE the StreamingOutput is completely read
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream pdfFile = new FileInputStream(readyPdfFile)) {
            IOUtils.copy(pdfFile, buffer);
        }
        return output -> {
            httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + readyPdfFile.getName() + "\"");
            try {
                IOUtils.copy(buffer.toInputStream(), output);
            } finally {
                buffer.close();
                //TODO perhaps log Author info here?
                downloadLogger.info("IP {} downloaded {}", httpServletRequest.getRemoteAddr(), readyPdfFile.getName());
            }
        };
    }
    
    private InputStream produceApron(String pdfFileString, PdfInfo pdfInfo) throws IOException {
        InputStream apronFile;
        try {
            apronFile = PdfTitlePageCreator.produceHeaderPage(pdfInfo);
        } catch (TransformerException | SAXException e) {
            throw new InternalServiceObjection("Failed to construct header page for '" + pdfFileString + "'", e);
        }
        return apronFile;
    }
    
    private ClosablePair<InputStream, PDDocumentInformation> cleanOldApronPages(File pdfFile, PdfInfo pdfInfo) throws IOException {
        InputStream requestedPDF;
        PDDocumentInformation pdfMetadata;
        try (PDDocument pdDocument = PdfUtils.openDocument(new FileInputStream(pdfFile))) {
            pdfMetadata = pdDocument.getDocumentInformation();
            PdfTitlePageCleaner.cleanHeaderPages(pdDocument);
            if (pdfInfo.getApronType() == ApronType.C) {
                log.info("Starting to insert footers for {}", pdfFile);
                CopyrightFooterInserter.insertCopyrightFooter(pdDocument);
                log.info("Finished inserting footers for {}", pdfFile);
            }
            requestedPDF = PdfUtils.dumpDocument(pdDocument);
        }
        return ClosablePair.of(requestedPDF, pdfMetadata);
    }
    
    
    /**
     * This method simply converts any Exception into a Service exception
     *
     * @param e : Any kind of exception
     * @param requestedPdfFile
     * @return A ServiceException
     */
    private ServiceObjection handleObjections(Exception e, String requestedPdfFile) {
        if (e instanceof ServiceObjection) {
            return (ServiceObjection) e; // Do nothing - this is a declared ServiceException from within module.
        } else {// Unforseen exception (should not happen). Wrap in internal service exception
            //log.error("ServiceObjection(HTTP 500): for requested file {}", requestedPdfFile, e); //You probably want to log this.
            return new InternalServiceObjection("Exception "+e.getClass().getName()+"("+e.getMessage()+") when trying to prepare "+requestedPdfFile+".");
        }
    }
}
