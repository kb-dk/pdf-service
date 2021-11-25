package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class PdfTitlePageCreator {
    
    private static final Logger log = LoggerFactory.getLogger(PdfTitlePageCreator.class);
    
    public static InputStream produceHeaderPage(PdfInfo pdfInfo) throws TransformerException, FOPException, IOException {
        
    
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        builder.setAccessibility(true);
        FopFactory fopFactory = builder.build();
        
        
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outStream);
            
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setErrorListener(new ErrorListener() {
                @Override
                public void warning(TransformerException exception) {
                    log.warn("Transformer warning", exception);
                }
                
                @Override
                public void error(TransformerException exception) {
                    log.error("Transformer Exception", exception);
                }
                
                @Override
                public void fatalError(TransformerException exception) throws TransformerException {
                    log.error("Transformer Fatal Exception", exception);
                    throw exception;
                }
            });
            
            try (InputStream formatterStream = new FileInputStream(ServiceConfig.getFrontPageFopFile().toFile())) {
                Transformer xslfoTransformer = factory.newTransformer(new StreamSource(formatterStream));
                xslfoTransformer.setParameter("authors", pdfInfo.getAuthors());
                xslfoTransformer.setParameter("title", pdfInfo.getTitle());
                xslfoTransformer.setParameter("altTitle", pdfInfo.getAlternativeTitle());
                xslfoTransformer.setParameter("edition", pdfInfo.getUdgavebetegnelse());
                xslfoTransformer.setParameter("place", pdfInfo.getPlace());
                xslfoTransformer.setParameter("size", pdfInfo.getSize());
                xslfoTransformer.setParameter("isWithinCopyright", pdfInfo.isWithinCopyright());
                
                final String logoPath = ServiceConfig.getLogoPath();
                xslfoTransformer.setParameter("logoPath", logoPath);
                
                xslfoTransformer.transform(new StreamSource(new StringReader("<xml/>")),
                                           new SAXResult(fop.getDefaultHandler()));
            }
            outStream.flush(); //just in case it is not done automatically
            return outStream.toInputStream();
        }
    }
    
    
}