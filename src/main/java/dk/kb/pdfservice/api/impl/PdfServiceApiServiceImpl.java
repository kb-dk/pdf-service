package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
     * @param pdfFile  : a pdf file with a name like {barcode}-somthing.pdf
     * @return <ul>
     *         <li>code = 200, message = "A pdf with attached page", response = String.class</li>
     *         </ul>
     * @throws ServiceException when other http codes should be returned
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other
     *         codes
     **/
    @Override
    public StreamingOutput getPdf(String pdfFile) {
        
        String barcode = pdfFile.split("[-.]", 2)[0];
        
        InputStream apronFile;
        try {
            apronFile = PdfTitlePageCreator.produceHeaderPage(barcode);
            //apronFile = new NullInputStream();
        } catch ( Exception e) {
            log.error("Fejl", e);
            throw new InternalServiceException("Fejl med getPdf", e);
        }

        try {
            final File url = new File(ServiceConfig.getPdfSourcePath(),pdfFile);
            try (InputStream inPdf = new FileInputStream(url)) {
                InputStream resultingPdf = CopyrightFooterInserter.insertCopyrightFooter(inPdf);
                log.info("Finished inserting footers");
                
                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                pdfMergerUtility.addSource(apronFile);
                pdfMergerUtility.addSource(resultingPdf);
                try (final var completePDF = new org.apache.commons.io.output.ByteArrayOutputStream()) {
                    pdfMergerUtility.setDestinationStream(completePDF);
                    //TODO Configurable memory settings
                    pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 500));
                    log.debug("Finished merging documents");
                    return output -> {
                        httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + pdfFile + "\"");
                        completePDF.flush(); //just in case it is not done automatically
                        try (var resultInputStream = completePDF.toInputStream();) {
                            IOUtils.copy(resultInputStream, output);
                        }
                        log.debug("Finished returning pdf");
                    };
                    
                }
            }
    
        } catch (IOException e) {
            log.error("Fejl", e);
            throw new InternalServiceException("Fejl med getPdf", e);
        }
    }
}
