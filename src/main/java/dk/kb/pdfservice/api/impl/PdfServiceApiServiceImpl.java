package dk.kb.pdfservice.api.impl;

import com.google.common.util.concurrent.Striped;
import dk.kb.pdfservice.alma.MarcClient;
import dk.kb.pdfservice.alma.PdfInfo;
import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.footer.CopyrightFooterInserter;
import dk.kb.pdfservice.titlepage.PdfTitlePageCleaner;
import dk.kb.pdfservice.titlepage.PdfTitlePageCreator;
import dk.kb.pdfservice.titlepage.PdfTitlePageInserter;
import dk.kb.pdfservice.utils.PdfUtils;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceException;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
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
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * pdf-service
 */
public class PdfServiceApiServiceImpl implements PdfServiceApi {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    
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
    
    private static final Striped<ReadWriteLock> stripedLock = Striped.readWriteLock(10);
    
    
    /**
     * Request a theater manuscript summary in pdf format.
     *
     * @param pdfFileString : a pdf file with a name like {barcode}-somthing.pdf
     * @return <ul>
     *         <li>code = 200, message = "A pdf with attached page", response = String.class</li>
     *         </ul>
     * @throws ServiceException when other http codes should be returned
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other
     *         codes
     **/
    @Override
    public StreamingOutput getPdf(String pdfFileString) {
        
        //1. Lock for this filename, so only one user can use it
        Lock lock = stripedLock.get(pdfFileString).writeLock();
        
        lock.lock();
        try {
            
            File readyPdfFile = new File(ServiceConfig.getPdfTempPath(), pdfFileString);
            //2. Check if file exists and is not to old
            if ( isUsable(readyPdfFile)) {
                Instant lastModifiedAt = Instant.ofEpochMilli(readyPdfFile.lastModified());
                TemporalAmount maxAge = ServiceConfig.getMaxAgeTempPdf();
                if (!lastModifiedAt.plus(maxAge).isBefore(Instant.now())) {
                    //3. if good, return the ready file
                    return returnPdfFile(readyPdfFile);
                }
            }
            
            //4. otherwise create
            final File sourcePdfFile = new File(ServiceConfig.getPdfSourcePath(), pdfFileString);
            
            if (!isUsable(sourcePdfFile)) {
                throw new NotFoundServiceException("Failed to find pdf file '" + pdfFileString + "'");
            }
            
            String barcode = pdfFileString.split("[-._]", 2)[0];
            PdfInfo pdfInfo = MarcClient.getPdfInfo(barcode);
            
            try (InputStream apronFile = produceApron(pdfFileString, pdfInfo);
                 InputStream requestedPDF = transformPdfFile(sourcePdfFile, pdfInfo);
                 InputStream completePDF = PdfTitlePageInserter.mergeFrontPageWithPdf(apronFile, requestedPDF);
                 OutputStream pdfFileOnDisk = new FileOutputStream(readyPdfFile)) {
                log.debug("Finished merging documents for {}", pdfFileString);
                IOUtils.copy(completePDF, pdfFileOnDisk);
                log.info("Finished returning pdf {}", pdfFileString);
            } catch (Exception e) {
                log.error("Failed for {}", pdfFileString, e);
                throw new WebApplicationException("Unknown failure for " + pdfFileString,
                                                  e,
                                                  Response.Status.INTERNAL_SERVER_ERROR);
            }
            return returnPdfFile(readyPdfFile);
        } catch (Exception e) {
            log.error("Exception for {}", pdfFileString, e);
            throw handleException(e);
        } finally {
            lock.unlock();
        }
    }
    
    private boolean isUsable(File readyPdfFile) {
        return readyPdfFile.exists() &&
               readyPdfFile.isFile() &&
               readyPdfFile.canRead() &&
               readyPdfFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }
    
    @Nonnull
    private StreamingOutput returnPdfFile(File readyPdfFile) {
        //TODO log the return of the file
        return output -> {
            httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + readyPdfFile.getName() + "\"");
            try (InputStream pdfFile = new FileInputStream(readyPdfFile)) {
                IOUtils.copy(pdfFile, output);
            }
        };
    }
    
    private InputStream produceApron(String pdfFileString, PdfInfo pdfInfo) throws IOException {
        InputStream apronFile;
        try {
            apronFile = PdfTitlePageCreator.produceHeaderPage(pdfInfo);
        } catch (TransformerException | SAXException e) {
            throw new InternalServiceException("Failed to construct header page for '" + pdfFileString + "'", e);
        }
        return apronFile;
    }
    
    private InputStream transformPdfFile(File pdfFile, PdfInfo pdfInfo) throws IOException {
        InputStream requestedPDF;
        try (PDDocument pdDocument = PdfUtils.openDocument(new FileInputStream(pdfFile))) {
            PdfTitlePageCleaner.cleanHeaderPages(pdDocument);
            if (!pdfInfo.isWithinCopyright()) {
                log.info("Starting to insert footers for {}", pdfFile);
                CopyrightFooterInserter.insertCopyrightFooter(pdDocument);
                log.info("Finished inserting footers for {}", pdfFile);
            }
            requestedPDF = PdfUtils.dumpDocument(pdDocument);
        }
        return requestedPDF;
    }
    
    
    /**
     * This method simply converts any Exception into a Service exception
     *
     * @param e: Any kind of exception
     * @return A ServiceException
     */
    private ServiceException handleException(Exception e) {
        if (e instanceof ServiceException) {
            return (ServiceException) e; // Do nothing - this is a declared ServiceException from within module.
        } else {// Unforseen exception (should not happen). Wrap in internal service exception
            log.error("ServiceException(HTTP 500):", e); //You probably want to log this.
            return new InternalServiceException(e.getMessage());
        }
    }
}
