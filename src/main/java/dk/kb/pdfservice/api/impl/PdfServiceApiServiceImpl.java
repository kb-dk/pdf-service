package dk.kb.pdfservice.api.impl;

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
import java.io.InputStream;
import java.util.Locale;

/**
 * pdf-service
 *
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
        try {
            //TODO race conditions and caching. No need to generate pdf for EACH call?
            final File pdfFile = new File(ServiceConfig.getPdfSourcePath(), pdfFileString);
            
            if (!(
                    pdfFile.exists() &&
                    pdfFile.isFile() &&
                    pdfFile.canRead() &&
                    pdfFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))) {
                throw new NotFoundServiceException("Failed to find pdf file '" + pdfFileString + "'");
            }
            
            String barcode = pdfFileString.split("[-._]", 2)[0];
            
            PdfInfo pdfInfo = MarcClient.getPdfInfo(barcode);
            
            InputStream apronFile;
            try {
                apronFile = PdfTitlePageCreator.produceHeaderPage(pdfInfo);
            } catch (TransformerException | SAXException e) {
                throw new InternalServiceException("Failed to construct header page for '" + pdfFileString + "'", e);
            }
            
            InputStream resultingPdf;
            try (PDDocument pdDocument = PdfUtils.openDocument(new FileInputStream(pdfFile))) {
                PdfTitlePageCleaner.cleanHeaderPages(pdDocument);
                if (!pdfInfo.isWithinCopyright()) {
                    log.info("Starting to insert footers for {}",pdfFile);
                    CopyrightFooterInserter.insertCopyrightFooter(pdDocument);
                    log.info("Finished inserting footers for {}", pdfFile);
                }
                resultingPdf = PdfUtils.dumpDocument(pdDocument);
            }
            
            try (final var completePDF = PdfTitlePageInserter.mergeFrontPageWithPdf(apronFile, resultingPdf);) {
                log.debug("Finished merging documents for {}", pdfFileString);
                return output -> {
                    httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + pdfFileString + "\"");
                    IOUtils.copy(completePDF, output);
                    log.debug("Finished returning pdf {}", pdfFileString);
                };
                
            }
        } catch (Exception e) {
            log.error("Exception for {}", pdfFileString, e);
            throw handleException(e);
        }
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
