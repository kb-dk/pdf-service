package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceException;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * pdf-service
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform.
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
     * @param pdfFile : a pdf file with a name like {barcode}-somthing.pdf
     * @return <ul>
     *         <li>code = 200, message = "A pdf with attached page", response = String.class</li>
     *         </ul>
     * @throws ServiceException when other http codes should be returned
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other
     *         codes
     **/
    @Override
    public StreamingOutput getPdf(String pdfFile) {
        try {
            
            final File origPdfFile = new File(ServiceConfig.getPdfSourcePath(), pdfFile);
            
            if (!(
                    origPdfFile.exists() &&
                    origPdfFile.isFile() &&
                    origPdfFile.canRead() &&
                    origPdfFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))) {
                throw new NotFoundServiceException("Failed to find pdf file '" + pdfFile + "'");
            }
            
            String barcode = pdfFile.split("[-._]", 2)[0];
            
            PdfInfo pdfInfo = MarcClient.getPdfInfo(barcode);
            
            InputStream apronFile;
            try {
                apronFile = PdfTitlePageCreator.produceHeaderPage(pdfInfo);
            } catch (TransformerException | SAXException e) {
                throw new InternalServiceException("Failed to construct header page for '" + pdfFile + "'", e);
            }
            
            PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
            pdfMergerUtility.addSource(apronFile);
            try (InputStream origPdfStream = new BufferedInputStream(new FileInputStream(origPdfFile))) {
                InputStream resultingPdf = CopyrightFooterInserter.cleanHeaderPages(origPdfStream);
                if (!pdfInfo.isWithinCopyright()) {
                    resultingPdf = CopyrightFooterInserter.insertCopyrightFooter(resultingPdf);
                    log.info("Finished inserting footers");
                }
                pdfMergerUtility.addSource(resultingPdf);
            }
                
                try (final var completePDF = new org.apache.commons.io.output.ByteArrayOutputStream()) {
                    pdfMergerUtility.setDestinationStream(completePDF);
                    //TODO Configurable memory settings
                    //Just use 100MBs and unlimited temp files
                    pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 100));
                    log.debug("Finished merging documents for {}", pdfFile);
                    return output -> {
                        httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + pdfFile + "\"");
                        completePDF.flush(); //just in case it is not done automatically
                        try (var resultInputStream = completePDF.toInputStream();) {
                            IOUtils.copy(resultInputStream, output);
                        }
                        log.debug("Finished returning pdf {}", pdfFile);
                    };
                    
                }
            } catch (Exception e) {
                log.error("Exception for {}", pdfFile, e);
                throw handleException(e);
            }
        }
        
        
        /**
         * This method simply converts any Exception into a Service exception
         *
         * @param e: Any kind of exception
         * @return A ServiceException
         */
        private ServiceException handleException (Exception e){
            if (e instanceof ServiceException) {
                return (ServiceException) e; // Do nothing - this is a declared ServiceException from within module.
            } else {// Unforseen exception (should not happen). Wrap in internal service exception
                log.error("ServiceException(HTTP 500):", e); //You probably want to log this.
                return new InternalServiceException(e.getMessage());
            }
        }
    }
