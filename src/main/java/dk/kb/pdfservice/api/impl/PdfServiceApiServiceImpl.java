package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.alma.MarcClient;
import dk.kb.pdfservice.alma.PdfInfo;
import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.footer.CopyrightFooterInserter;
import dk.kb.pdfservice.titlepage.PdfApronCreator;
import dk.kb.pdfservice.titlepage.PdfApronPageCleaner;
import dk.kb.pdfservice.utils.PdfMetadataUtils;
import dk.kb.pdfservice.utils.SizeUtils;
import dk.kb.pdfservice.webservice.exception.InternalServiceObjection;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceObjection;
import dk.kb.pdfservice.webservice.exception.ServiceObjection;
import dk.kb.util.other.NamedThread;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
            File copyrightedPdfFile = getCacheFile(requestedPdfFile);
            
            //1. Lock for this filename, so only one user can use it
            final ReadWriteLock readWriteLock = ServiceConfig.getPdfServeLocks().get(requestedPdfFile);
            
            //First we acquire the read lock, to be able to read the file
            readWriteLock.readLock().lock();
            //state: w=0,r=1
            try {
    
    
                String barcode = copyrightedPdfFile.getName().split("[-._]", 2)[0];
                
                PdfInfo pdfInfo = MarcClient.getPdfInfo(barcode);
                
                boolean useCachePdf = canUseCachePdf(copyrightedPdfFile, pdfInfo);
                
                //The copyrighted PDF is not usable, so it must be regenerated
                if (!useCachePdf) {
                    
                    upgradeToWriteLock(readWriteLock);
                    //state: w=1,r=0,
                    try {
                        //recheck that somebody did not update the pdf beneath us while we were waiting for the write lock
                        if (!canUseCachePdf(copyrightedPdfFile, pdfInfo)) {
                            
                            //recreate the copyrighted PDF
    
                            //Extract the thread name while still in this context
                            final String existingThreadName = namedThread.getName();
                            
                            //Done in a threadpool so we can control the number of concurrect PDFs being created
                            Future<?> result = ServiceConfig.getPdfBuildersThreadPool().submit(() -> {
                                //Name the thread with the old name as postfix
                                //This allows us to track (in the log) which pool-thread continued the work from above
                                try (NamedThread namedPoolThread = NamedThread.postfix(existingThreadName)) {
                                    //recreate the copyrighted PDF
                                    createCopyrightedPDF(
                                            requestedPdfFile,
                                            copyrightedPdfFile,
                                            pdfInfo);
                                }
                            });
                            try {
                                result.get(); //wait on result or exception, potentially forever
                            } catch (ExecutionException e){
                                throw e.getCause();
                            }
                        }
                        //If we got to here, everything worked
                    } finally {
                        downgradeToReadLock(readWriteLock);
                        //w=0,r=1
                    }
                }
                //w=0,r=1, no matter if we went through the write procedure or not
                return returnPdfFile(copyrightedPdfFile);
                
            } catch (Throwable e) {
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
    
    private void createCopyrightedPDF(String pdfFileString, File cachedPdfFile, PdfInfo pdfInfo) throws NotFoundServiceObjection {
        //4. otherwise create
        final File sourcePdfFile = getSourcePdfFile(pdfFileString);
        
        
        try {
            //Just in case the folder for the cached files do not already exist
            Files.createDirectories(cachedPdfFile.getParentFile().toPath());
            
            //Temp file is same dir as the resulting pdf file.
            //This way, we KNOW that the files will be on the same mount
            
            File tempFile = Files.createTempFile(cachedPdfFile.getParentFile().toPath(),
                                                 sourcePdfFile.getName(),
                                                 ".tmp").toFile();
            
            try {
                //TODO suppressed records should be available?
                //TODO configurable produceApron module, so we can insert another
                try (InputStream apronFile = produceApron(pdfFileString, pdfInfo);
                     OutputStream tempPdfFileStream = new FileOutputStream(tempFile)) {
                    
                    //Write to the tempfile. This can take a while
                    //People should be free to read the cached version (if they got a lock before we started)
                    createCombinedPdf(sourcePdfFile,
                                      pdfInfo,
                                      apronFile,
                                      tempPdfFileStream);
                }
                log.debug("Finished merging documents for {}", pdfFileString);
                
                //And when done, atomically move the temp file to replace the cachedPdfFile
                //This ensures that already open instances of this file is not mangled
                Files.move(tempFile.toPath(),
                           cachedPdfFile.toPath(),
                           StandardCopyOption.ATOMIC_MOVE,
                           StandardCopyOption.REPLACE_EXISTING);
                
                log.info("Finished returning pdf {}", pdfFileString);
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        } catch (IOException e) {
            log.error("Failed for {}", pdfFileString, e);
            object(() -> new InternalServiceObjection("Unknown failure for " + pdfFileString, e));
        }
    }
    
    
    @Nonnull
    private File getSourcePdfFile(String pdfFileString) {
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
    
    
    private File getCacheFile(String requestedPdfFile) {
        File file = new File(requestedPdfFile);
        return new File(ServiceConfig.getPdfCachePath(), file.getName());
    }
    
    private boolean canUseCachePdf(File readyPdfFile, PdfInfo pdfInfo) {
        
        //2. Check if file exists
        if (isUsable(readyPdfFile)) {
            Instant pdfLastModified = Instant.ofEpochMilli(readyPdfFile.lastModified());
            TemporalAmount maxAllowedAge = ServiceConfig.getMaxAgeTempPdf();
    
            final Instant otherInstant = pdfInfo.getLatestModDate().toInstant();
            return pdfLastModified.isAfter(otherInstant);
            //
            ////Check if the file is not too old
            ////3. if good, return the ready file
            //final boolean before = pdfLastModified.plus(maxAllowedAge).isBefore(Instant.now());
            //
            //return !before;
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
    private StreamingOutput returnPdfFile(File cachedPdfFile) {
        //We never write directly to the cached pdf file. Rather, we write to a temp file that is atomically moved into the cachedPdfFile
        //So you will never get a mangled pdf file here.
        return output -> {
            try (NamedThread ignored = NamedThread.postfix(cachedPdfFile.getName())){
                httpServletResponse.setHeader("Content-disposition",
                                              "inline; filename=\"" + cachedPdfFile.getName() + "\"");
    
                String userIP = httpServletRequest.getRemoteAddr();
                
                try (InputStream buffer = IOUtils.buffer(new FileInputStream(cachedPdfFile))) {
                 
                    IOUtils.copy(buffer, output);
                    
                    log.info("IP {} downloaded {}",
                                        userIP,
                                        cachedPdfFile.getName());
                    
                    //TODO perhaps log Author info here?
                    downloadLogger.info("IP {} downloaded {}",
                                        userIP,
                                        cachedPdfFile.getName());
                } catch (IOException e) {
                    List<String> messages = getExceptionMessages(e);
                    if (messages.contains("Connection reset by peer") ||
                        messages.contains("Broken pipe")){
                        //Do not log the stack trace for this rather standard error
                        log.info("User ({}) closed connection while downloading {}. This is not counted as a download", userIP, cachedPdfFile.getName());
                    } else {
                        log.warn("IOException while sending {} to user {}",
                                 cachedPdfFile.getName(),
                                 userIP,
                                  e);
                    }
                } catch (Exception e){
                    log.error("Exception while sending {} to user {}",
                             cachedPdfFile.getName(),
                             userIP,
                             e);
                }
            }
        };
    }
    
    private static List<String> getExceptionMessages(Throwable e) {
        List<String> result = new ArrayList<>();
        result.add(e.getMessage());
        if(e.getCause() != null){
            result.addAll(getExceptionMessages(e.getCause()));
        }
        return result;
    }
    
    private InputStream produceApron(String pdfFileString, PdfInfo pdfInfo) throws IOException {
        InputStream apronFile;
        try {
            apronFile = PdfApronCreator.produceApronPage(pdfInfo);
        } catch (TransformerException | SAXException e) {
            throw new InternalServiceObjection("Failed to construct header page for '" + pdfFileString + "'", e);
        }
        return apronFile;
    }
    
    private void createCombinedPdf(File originalPdfFile,
                                   PdfInfo pdfInfo,
                                   InputStream apronFile,
                                   OutputStream tempPdfFileStream)
            throws IOException {
        
        //TODO serious bug. The closing of the Document also closes the ScratchFile (for some insane reason), causing it
        // to not be usable any more. How to work around this?
        log.info("Starting to open {} of size {}", originalPdfFile, SizeUtils.toHumanReadable(originalPdfFile.length()));
        try (PDDocument pdDocument = PDDocument.load(new FileInputStream(originalPdfFile),
                                                     ServiceConfig.getMemoryUsageSetting())) {
            log.info("Opened {}", originalPdfFile);
            PDDocumentInformation newMetadata = PdfMetadataUtils.constructPdfMetadata(pdfInfo,
                                                                                      pdDocument.getDocumentInformation());
            pdDocument.setDocumentInformation(newMetadata);
            
            PdfApronPageCleaner.cleanApronPages(pdDocument);
            
            if (pdfInfo.getApronType() == ApronType.C) {
                log.info("Starting to insert footers for {}", originalPdfFile);
                CopyrightFooterInserter.insertCopyrightFooter(pdDocument);
                log.info("Finished inserting footers for {}", originalPdfFile);
            }
            
            mergeApronPagesWithPdf(apronFile, pdDocument, tempPdfFileStream);
            
        }
    }
    
    private void mergeApronPagesWithPdf(InputStream apronFile,
                                        PDDocument pdDocument,
                                        OutputStream tempPdfFileStream) throws IOException {
        log.info("Merging apron and original pdf");
        //Do not use specialised memory settings for loading apronfile, as it is always small
        try (PDDocument apronDocument = PDDocument.load(apronFile)) {
            //This weird for loop is to accound for the possibility of multiple apron pages
            int apronPageCount = apronDocument.getNumberOfPages();
            for (int i = apronPageCount - 1; i >= 0; i--) {
                //Start from the last apron page
                //This ensures that multiple apron pages end up in the correct order
                
                //Import the page as the last page of the document
                PDPage apronPage = apronDocument.getPage(i);
                pdDocument.importPage(apronPage);
                
                //Move the last page to the first page
                PDPageTree allPages = pdDocument.getPages();
                int lastPageIndex = allPages.getCount() - 1;
                PDPage lastPage = allPages.get(lastPageIndex);
                allPages.remove(lastPageIndex);
                PDPage firstPage = allPages.get(0);
                allPages.insertBefore(lastPage, firstPage);
            }
            
            log.info("Saving resulting pdf");
            //The apronDoc apparently needs to still be open while this happens. Go figure
            pdDocument.save(new BufferedOutputStream(tempPdfFileStream));
        }
    }
    
    
    /**
     * This method simply converts any Exception into a Service exception
     *
     * @param e                : Any kind of exception
     * @param requestedPdfFile
     * @return A ServiceException
     */
    private ServiceObjection handleObjections(Throwable e, String requestedPdfFile) {
        if (e instanceof ServiceObjection) {
            return (ServiceObjection) e; // Do nothing - this is a declared ServiceException from within module.
        } else {// Unforseen exception (should not happen). Wrap in internal service exception
            //log.error("ServiceObjection(HTTP 500): for requested file {}", requestedPdfFile, e); //You probably want to log this.
            return new InternalServiceObjection("Exception "
                                                + e.getClass().getName()
                                                + "("
                                                + e.getMessage()
                                                + ") when trying to prepare "
                                                + requestedPdfFile
                                                + ".");
        }
    }
}
