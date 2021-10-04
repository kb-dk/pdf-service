package dk.kb.pdfservice.api.impl;

import dk.kb.alma.client.AlmaInventoryClient;
import dk.kb.alma.client.AlmaRestClient;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
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
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * pdf-service
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform.
 */
public class PdfServiceApiServiceImpl implements PdfServiceApi {
    // Setup directories
    File baseDir = new File(".");
    File outDir = new File(baseDir, "out");
    private final Logger log = LoggerFactory.getLogger(this.toString());
    
    
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
    
    
    public String getRawManuscript(String barCode) throws ServiceException {
        // TODO: Implement...barcode=130018972949
        //TODO switch to using AlmaClient
        try {
            String urlString =
                    "https://soeg.kb.dk/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&query=barcode="
                    + barCode
                    + "&recordSchema=marcxml";
            
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()))) {
                return bufferedReader.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    
    public InputStream produceHeaderPage(String barCode) throws TransformerException, SAXException, IOException {
        
        String response = getRawManuscript(barCode);
        
        StreamSource xmlSource = new StreamSource(new StringReader(response));
        
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        builder.setAccessibility(true);
        FopFactory fopFactory = builder.build();
    
        //TODO use alma client instead of this thing
        //AlmaRestClient restClient = new AlmaRestClient("https://api-eu.hosted.exlibrisgroup.com/almaws/v1/", "TODO API KEY");
        //
        //AlmaInventoryClient inventoryClient = new AlmaInventoryClient(restClient);
        //Item item = inventoryClient.getItem(barCode);
        
        
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outStream);

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setErrorListener(new ErrorListener() {
                @Override
                public void warning(TransformerException exception) throws TransformerException {
                    log.warn("Transformer warning", exception);
                }
                
                @Override
                public void error(TransformerException exception) throws TransformerException {
                    log.error("Transformer Exception", exception);
                }
                
                @Override
                public void fatalError(TransformerException exception) throws TransformerException {
                    log.error("Transformer Fatal Exception", exception);
                    throw exception;
                }
            });
            
            try (InputStream formatterStream = Thread.currentThread()
                                                     .getContextClassLoader()
                                                     .getResourceAsStream("formatter.xsl")) {
                Transformer xslfoTransformer = factory.newTransformer(new StreamSource(formatterStream));
                //TODO do not perform logic in the extremely limited language of XSLT1.0
                //Instead perform the logic here and feed the results in via params
                xslfoTransformer.setParameter("input1","testInput1Value");
                xslfoTransformer.transform(xmlSource, new SAXResult(fop.getDefaultHandler()));
            }
            outStream.flush(); //just in case it is not done automatically
            return outStream.toInputStream();
        }
    }
    
    
    /**
     * Request a theater manuscript summary in pdf format.
     *
     * @param barcode  : code to get xml result tree String containing info about pdf
     * @param pdflink2 : Relative path including .pdf file
     * @return <ul>
     *         <li>code = 200, message = "A pdf with attached page", response = String.class</li>
     *         </ul>
     * @throws ServiceException when other http codes should be returned
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other
     *         codes
     **/
    @Override
    public StreamingOutput getPdf(String barcode, String pdflink2) {
        
        System.out.println("barcode: " + barcode);
        
        httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + barcode + "-bw.pdf\"");
        
        try {
            InputStream apronFile = produceHeaderPage(barcode);
            
            //TODO retrieve pdf from https://www.kb.dk/e-mat/dod/<barcode>-bw.pdf
            
            final URL url = new URL("http://www5.kb.dk/e-mat/dod/" + barcode + "-bw.pdf");
            try (InputStream inPdf = url.openStream()) {
                InputStream resultingPdf = PdfBoxCopyrightInserter.insertCopyrightFooter(inPdf);
                log.info("Finished inserting footers");
                
                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                pdfMergerUtility.addSource(apronFile);
                pdfMergerUtility.addSource(resultingPdf);
                try (final var completePDF = new org.apache.commons.io.output.ByteArrayOutputStream()) {
                    pdfMergerUtility.setDestinationStream(completePDF);
                    pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 500));
                    log.debug("Finished merging documents");
                    return output -> {
                        completePDF.flush(); //just in case it is not done automatically
                        try (var resultInputStream = completePDF.toInputStream();) {
                            IOUtils.copy(resultInputStream, output);
                        }
                        log.debug("Finished returning pdf");
                    };
                    
                }
            }
            
        } catch (TransformerException | SAXException | IOException e) {
            log.error("Fejl", e);
            throw new InternalServiceException("Fejl med getPdf", e);
        }
    }
    
    
    /**
     * This method simply converts any Exception into a Service exception
     *
     * @param e: Any kind of exception
     * @return A ServiceException
     *         dk.kb.webservice.ServiceExceptionMapper
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
