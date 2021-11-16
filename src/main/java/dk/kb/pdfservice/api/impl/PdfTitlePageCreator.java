package dk.kb.pdfservice.api.impl;

import dk.kb.alma.gen.bibs.Bib;
import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.commons.io.output.ByteArrayOutputStream;
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
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfTitlePageCreator {
    
    private static final Logger log = LoggerFactory.getLogger(PdfTitlePageCreator.class);
    
    public static InputStream produceHeaderPage(String barCode) throws TransformerException, SAXException, IOException {
        
        String actualBarcode = barCode.split("-", 2)[0];
    
        Bib bib = AlmaLookupClient.getBib(actualBarcode);
    
        //Portfolios portFolios = almaInventoryClient.getBibPortfolios(mmsID);
        
        MarcClient marc21 = new MarcClient(bib.getAnies().get(0));
        String authors = getAuthors(marc21);
        String title = getTitle(marc21);
        String alternativeTitle = getAlternativeTitle(marc21);
        String udgavebetegnelse = getUdgavebetegnelse(marc21);
        String place = getPlace(marc21);
        String size = getSize(marc21);
        
    
    
        final LocalDate publicationDate = CopyrightDecider.getPublicationDate(bib, marc21);
        boolean isWithinCopyright = CopyrightDecider.isWithinCopyright(publicationDate);
        
        
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
                xslfoTransformer.setParameter("authors", authors);
                xslfoTransformer.setParameter("title", title);
                xslfoTransformer.setParameter("altTitle", alternativeTitle);
                xslfoTransformer.setParameter("edition", udgavebetegnelse);
                xslfoTransformer.setParameter("place", place);
                xslfoTransformer.setParameter("size", size);
                xslfoTransformer.setParameter("isWithinCopyright", isWithinCopyright);
                
                final String logoPath = ServiceConfig.getLogoPath();
                xslfoTransformer.setParameter("logoPath", logoPath);
                
                xslfoTransformer.transform(new StreamSource(new StringReader("<xml/>")),
                                           new SAXResult(fop.getDefaultHandler()));
            }
            outStream.flush(); //just in case it is not done automatically
            return outStream.toInputStream();
        }
    }
    
    private static String getSize(MarcClient marc21) {
        // Forlæggets fysiske størrelse:
        // hentes fra Marc21 300a
        return marc21.getString("300","a").orElse("");
    }
    
    private static String getPlace(MarcClient marc21) {
        //  * hentes fra Marc21 260a + b + c eller 500a (hvis man kan afgrænse til *premiere*, da man ved overførslen til ALMA slog mange felter sammen til dette felt),
        //  * 710 (udfaset felt, der stadig er data i) eller
        //  * felt 96 (i 96 kan dog også være rettighedsbegrænsninger indskrevet).
        
        String tag260a = marc21.getString("260","a").orElse(null);
        if (tag260a != null) {
            String tag260b = marc21.getString("260","b").orElse(null);
            String tag260c = marc21.getString("260","c").orElse(null);
            return Stream.of(tag260a, tag260b, tag260c).filter(Objects::nonNull).collect(Collectors.joining(" "));
        } else {
            Optional<String> place = marc21.getStrings("500","a")
                                          .stream()
                                          .filter(str -> str.startsWith("Premiere ")).findFirst();
            
            return place.orElse("No place specified");
        }
    }
    
    private static String getUdgavebetegnelse(MarcClient marc21) {
        //* Udgavebetegnelse:
        //  * 250a
        //  * hvis ikke noget = ingenting (ikke relevant for teatermanuskripter)
        String tag250a = marc21.getString("250","a").orElse(null);
        return Stream.of(tag250a).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    private static String getAlternativeTitle(MarcClient marc21) {
        return marc21.getString("246","a").orElse("");
    }
    
    private static String getTitle(MarcClient marc21) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?
        
        String tag245a = marc21.getString("245","a").orElse(null);
        String tag245b = marc21.getString("245","b").orElse(null);
        return Stream.of(tag245a, tag245b).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    private static String getAuthors(MarcClient marc21) {
        //* Forfatter(e):
        //  * hentes fra Marc21 100a,
        //  * 700a + d ( kan forekomme flere gange),
        //  * 245c - komma separeret og opdelt i flere linier hvis længere end ?
        //  * [] - hvis ingen værdi(er).
        String tag100a = marc21.getString("100","a").orElse(null);
        String tag700a = marc21.getString("700","a").orElse(null);
        String tag245c = marc21.getString("245","c").orElse(null);
        if (tag100a == null && tag700a == null && tag245c == null) {
            return "[]";
        }
        return Stream.of(tag100a, tag700a, tag245c)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining(", "));
    }
    
    
}
