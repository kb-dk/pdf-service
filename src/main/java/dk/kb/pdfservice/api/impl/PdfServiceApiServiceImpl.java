package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.api.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import java.io.*;
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
//        HttpURLConnection conn = null;
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

// NEW
public void convertToPdf() throws TransformerException, SAXException, IOException {
    // the XSL FO file
    File xsltFile = new File(ServiceConfig.getResourcesDir() + "//formatter.xsl");
    // the XML file from which we take the name
    StreamSource xmlSource = new StreamSource(new File(ServiceConfig.getResourcesDir() + "//response_1620140259669.xml"));

    // create an instance of fop factory
// Ourdated!
    // FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
    // a user agent is needed for transformation

    // to store output
    // ByteArrayOutputStream outStream = new ByteArrayOutputStream();
// New
    File xconf = new File("fop.xconf");
    FopConfParser parser = new FopConfParser(xconf); //parsing configuration
    FopFactoryBuilder builder = parser.getFopFactoryBuilder(); //building the factory with the user options
    FopFactory fopFactory = builder.build();
// Outdated
    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    OutputStream outStream = null;

    // Transformer xslfoTransformer = null; // WRONG
    try {
        outStream = new BufferedOutputStream(new FileOutputStream(ServiceConfig.getOutputDir() + "//output.pdf"));
        // Outdated
        // Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer xslfoTransformer = factory.newTransformer(new StreamSource(xsltFile));
        Result res = new SAXResult(fop.getDefaultHandler());

        // everything will happen here..
        xslfoTransformer.transform(xmlSource, res);

        // if you want to get the PDF bytes, use the following code
        //return outStream.toByteArray();

        // if you want to save PDF file use the following code
			/* File pdffile = new File("Result.pdf");
			OutputStream outStream = new java.io.FileOutputStream(pdffile);
                        outStream = new java.io.BufferedOutputStream(out);
                        FileOutputStream str = new FileOutputStream(pdffile);
                        str.write(outStream.toByteArray());
                        str.close();
                        out.close(); */

        // to write the content to out put stream
        //        byte[] pdfBytes = outStream.toByteArray();
        // for servlet use:
            /*        response.setContentLength(pdfBytes.length);
                    response.setContentType("application/pdf");
                    response.addHeader("Content-Disposition",
                            "attachment;filename=pdffile.pdf");
                    response.getOutputStream().write(pdfBytes);
                    response.getOutputStream().flush(); */
    } finally {
        outStream.close();
    }
}


/**
 * Request a theater manuscript summary in pdf format.
 *
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
        PDDocument pdfDoc;
        String basepath;
        basepath = ServiceConfig.getBasepath();
        System.out.println("basepath: " + basepath);

        if (pdflink2 == null)
            pdflink2 = "";

        try {
            httpServletResponse.setHeader("Content-disposition", " filename = " + pdflink2);
            final InputStream inputStream = new FileInputStream(new File( pdflink2));

           return output ->  {
                IOUtils.copy(inputStream, output);
            };
        } catch (FileNotFoundException e) {
          //  throw new NoContentException("Kan ikke finde pdflink");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
    * @see dk.kb.webservice.ServiceExceptionMapper
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
