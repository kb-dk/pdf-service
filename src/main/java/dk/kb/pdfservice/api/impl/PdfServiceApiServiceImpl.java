package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.api.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import dk.kb.pdfservice.cachingtransformerfactory.SingletonCachingTransformerFactory;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.*;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.w3c.dom.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
// New
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

/**
 * pdf-service
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform. 
 *
 */
public class PdfServiceApiServiceImpl implements PdfServiceApi {
    private Logger log = LoggerFactory.getLogger(this.toString());

    // Setup directories
    File baseDir = new File(".");
    File outDir = new File(baseDir, "out");


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
    private transient ContextResolver contextResolver;

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
     * Request a theater manuscript summary.
     * 
     * @param barcode: Barcode for a theater manuscript
     * 
     * @return <ul>
      *   <li>code = 200, message = "A pdf with attached page", response = File.class</li>
      *   </ul>
      * @throws ServiceException when other http codes should be returned
      *
      * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other codes
     */
    @Override
    public javax.ws.rs.core.StreamingOutput getManuscript(String barcode, String pdflink) throws ServiceException {
        // TODO: Implement...
        
        try{ 
            httpServletResponse.setHeader("Content-Disposition", "inline; filename=\"filename.ext\"");
            String doc = getRawManuscript((barcode));
     //       return output -> output.write("Magic".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return output -> output.write(doc.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e){
            throw handleException(e);
        }
    }


    /**
     * Request a theater manuscript summary.
     * 
     * @param barCode : Barcode for a theater manuscript
     *
     * @return <ul>
      *   <li>code = 200, message = "A pdf with attached page", response = String.class</li>
      *   </ul>
      * @throws ServiceException when other http codes should be returned
      *
      * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other codes
     */
    @Override
    public String getRawManuscript(String barCode) throws ServiceException {
        // TODO: Implement...barcode=130018972949
        final String USER_AGENT = "Mozilla/5.0";
        String response = null;
        try {
            String urlString = "https://soeg.kb.dk/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&query=barcode=" + barCode + "&recordSchema=marcxml";
            URL url = new URL(urlString);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());
            doc.getDocumentElement().normalize();
            System.out.println("Document type: " + doc.getDoctype());
             return transformToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
            throw handleException(e);
        }
    }

    public String transformToString(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer xform = transformerFactory.newTransformer();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        xform.transform(new DOMSource(doc), new StreamResult(bao));
        return bao.toString(StandardCharsets.UTF_8);
    }

public void convertToPdf(String barCode) throws TransformerException, SAXException, IOException {
    // the XSL FO file
    File xsltFile = new File(ServiceConfig.getResourcesDir() + "//formatter.xsl");

    String response = getRawManuscript(barCode);
    System.out.println("Response: " + response);
    StreamSource xmlSource =  new StreamSource(new StringReader(response));

    FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
    builder.setAccessibility(true);
    FopFactory fopFactory = builder.build();

    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    OutputStream outStream = null;
    Fop fop = null;
    Result result = null;
    File pdfOut = new File((ServiceConfig.getOutputDir() + "//" + barCode + ".pdf"));
    try {
        outStream = new BufferedOutputStream(new FileOutputStream(pdfOut));
        fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);

        // TransformerFactory factory = TransformerFactory.newInstance(); // Old alt. adaption
        TransformerFactory factory = SingletonCachingTransformerFactory.newInstance();   // NEW adaption
        Transformer xslfoTransformer = factory.newTransformer(new StreamSource(xsltFile));

        result = new SAXResult(fop.getDefaultHandler());

        // everything will happen here..
        xslfoTransformer.transform(xmlSource, result);
    }
     finally {
        outStream.close();
    }
}


/**
 * Request a theater manuscript summary in pdf format.
 *
 * @param barcode : code to get xml result tree String containing info about pdf
 * @param pdflink2 : Relative path including .pdf file
 *
 * @return <ul>
 *   <li>code = 200, message = "A pdf with attached page", response = String.class</li>
 *   </ul>
 * @throws ServiceException when other http codes should be returned
 *
 * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other codes
 **/
    @Override

    public StreamingOutput getPdf(String barcode, String pdflink2) {
        String basepath;

        System.out.println("barcode: " + barcode);
        System.out.println("pdfLink2: " + pdflink2);
        basepath = ServiceConfig.getBasepath();
        System.out.println("basepath: " + basepath);
        String resourcesDir = ServiceConfig.getResourcesDir();
        System.out.println("Resources dir: " + resourcesDir );
        String outputDir = ServiceConfig.getOutputDir();

        if (pdflink2 == null)
            pdflink2 = "";

        httpServletResponse.setHeader("Content-disposition", " filename = " + pdflink2);

        try {
            convertToPdf(barcode);
            System.out.println("mergePDFile(" + outputDir + barcode + ".pdf" + "," + pdflink2 +")");
            // mergePDFFile(outputDir + "output.pdf",pdflink2);
            mergePDFFile(outputDir + barcode + ".pdf",pdflink2); // TEST
            final InputStream inputStream = new FileInputStream(new File((outputDir + "//" + barcode + ".pdf")));
            return output ->  {
                IOUtils.copy(inputStream, output);
            };
        } catch ( TransformerException | SAXException| IOException e ) {
            e.printStackTrace();
            throw new InternalServiceException("Fejl med getPdf", e);
        }
    }

    /**
     * Request a theater manuscript summary in pdf format.
     *
     * @param apron : name of  xml result tree String containing info about pdf
     * @param pdfFile : Relative path including .pdf file
     *
     * @return <ul>
     *   <li>code = 200, message = "A pdf apron and with attached pages", response = String.class</li>
     *   </ul>
     * @throws ServiceException when other http codes should be returned
     *
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other codes
     **/

    public StreamingOutput getMergePDFs(@NotNull String apron, @NotNull String pdfFile) {
    return  null; }

    private void mergePDFFile(String apron, @NotNull String pdfFile) {
    // public StreamingOutput mergePDFs(String apron, String pdfFile) {
        String outputDir = ServiceConfig.getOutputDir();

        String resourcesDir = ServiceConfig.getResourcesDir();
        // File file1 = new File(outputDir + "output.pdf");
        File file1 = new File( apron);
        File file2 = new File(resourcesDir + pdfFile);

        //Instantiating PDFMergerUtility class
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        System.out.println(("APRON in mergePDFFile: " + apron));
        //Setting the destination file
        PDFmerger.setDestinationFileName( apron );

        httpServletResponse.setHeader("Content-disposition", " filename = merged_" + pdfFile);
        //adding the source files
        try {
            PDFmerger.addSource(file1);
            PDFmerger.addSource(file2);
            //Merging the two documents
            System.out.println("merging the two documents: " + file1 + " and " + file2 + ")");
            PDFmerger.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 500));
        } catch ( IOException e) {
            e.printStackTrace();
            throw new InternalServiceException("Fejl med merge af forside og pdf", e);
        }
        System.out.println("Documents merged");
        //return  PDFmerger.getDestinationStream();
    }


    /**
     * Ping the server to check if the server is reachable.
     * 
     * @return <ul>
      *   <li>code = 200, message = "OK", response = String.class</li>
      *   <li>code = 406, message = "Not Acceptable", response = ErrorDto.class</li>
      *   <li>code = 500, message = "Internal Error", response = String.class</li>
      *   </ul>
      * @throws ServiceException when other http codes should be returned
      *
      * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other codes
     */
    @Override
    public String ping() throws ServiceException {
        // TODO: Implement...
        try{ 
            String response = "e7nJq";
        return response;
        } catch (Exception e){
            throw handleException(e);
        }
    
    }

    /**
    * This method simply converts any Exception into a Service exception
    * @param e: Any kind of exception
    * @return A ServiceException
    *  dk.kb.webservice.ServiceExceptionMapper
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
