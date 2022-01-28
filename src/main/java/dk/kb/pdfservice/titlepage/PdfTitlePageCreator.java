package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.alma.PdfInfo;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.utils.PdfUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PdfTitlePageCreator {
    
    private static final Logger log = LoggerFactory.getLogger(PdfTitlePageCreator.class);
    
    public static InputStream produceHeaderPage(PdfInfo pdfInfo)
            throws TransformerException, FOPException, IOException {
        File formatterFile = ServiceConfig.getFrontPageFopFile().toFile();
        
        
        FopFactoryBuilder builder = new FopFactoryBuilder(formatterFile.getAbsoluteFile().getParentFile().toURI());
        builder.setAccessibility(false);
        
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        try {
            Configuration cfg = cfgBuilder.buildFromFile(ServiceConfig.getFOPConfigFile());
            builder.setConfiguration(cfg);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        
        FopFactory fopFactory = builder.build();
        
        
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            FOUserAgent agent = fopFactory.newFOUserAgent();
            FontManager fontManager = agent.getFontManager();
            //How to override the FOP logging
            //LoggingEventListener loggingEventListener = new LoggingEventListener();
            //agent.getEventBroadcaster().addEventListener(loggingEventListener);
            if (pdfInfo.getAuthors() != null && !pdfInfo.getAuthors().isEmpty()) {
                agent.setAuthor(pdfInfo.getAuthors());
            }
            if (pdfInfo.getTitle() != null && !pdfInfo.getTitle().isEmpty()) {
                agent.setTitle(pdfInfo.getTitle());
            }
    
            Date creationDate = new Date(pdfInfo.getPublicationDate().toEpochDay() * ChronoUnit.DAYS.getDuration().toMillis());
            agent.setCreationDate(creationDate);
            //agent.setCreator();
            //agent.setProducer();
            //agent.setKeywords(pdfInfo.getSubjects());
            //agent.setSubject();
            
            
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, agent, outStream);
            
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
            
            try (InputStream formatterStream = new FileInputStream(formatterFile)) {
                
                Transformer xslfoTransformer = factory.newTransformer(new StreamSource(formatterStream,
                                                                                       formatterFile.toURI()
                                                                                                    .toASCIIString()));
                
                
                List<String> info = List.of(fixChars(pdfInfo.getAuthors()),
                                            fixChars(pdfInfo.getTitle()),
                                            fixChars(pdfInfo.getAlternativeTitle()),
                                            fixChars(pdfInfo.getUdgavebetegnelse()),
                                            fixChars(pdfInfo.getPlaceAndYear()),
                                            fixChars(pdfInfo.getSize()));
                int apronMetadataTableFontSize = ServiceConfig.getApronMetadataTableFontSize();
                float apronMetadataTableWidthCm = ServiceConfig.getApronMetadataTableWidthCm();
                info = enforceLimits(info,
                                     pdfInfo.getApronType(),
                                     apronMetadataTableFontSize,
                                     apronMetadataTableWidthCm);
                
                xslfoTransformer.setParameter("authors", info.get(0));
                xslfoTransformer.setParameter("title", info.get(1));
                xslfoTransformer.setParameter("altTitle", info.get(2));
                xslfoTransformer.setParameter("edition", info.get(3));
                xslfoTransformer.setParameter("placeAndYear", info.get(4));
                xslfoTransformer.setParameter("size", info.get(5));
                xslfoTransformer.setParameter("documentType", pdfInfo.getApronType().name());
    
    
                xslfoTransformer.setParameter("metadataTableFont",
                                              Objects.requireNonNull(ServiceConfig.getApronMetadataTableFont())
                                                     .getFullName());
                
                xslfoTransformer.setParameter("metadataTableFontSize", apronMetadataTableFontSize);
                
                xslfoTransformer.setParameter("metadataTableWidth", apronMetadataTableWidthCm);
                
                xslfoTransformer.transform(new StreamSource(new StringReader("<xml/>")),
                                           new SAXResult(fop.getDefaultHandler()));
            }
            outStream.flush(); //just in case it is not done automatically
            return outStream.toInputStream();
        }
    }
    
    private static String fixChars(String string) {
        //String resplaced = string.replaceAll("̈\u0308", "\u200B̈ \\1");
        return string;
    }
    
    protected static List<String> enforceLimits(List<String> info,
                                                ApronType apronType,
                                                int apronMetadataTableFontSize,
                                                float apronMetadataTableWidthCm) {
        //a4 width in pixels * 9cm/21cm -- 21cm being the width of A4 in cm
        final float lineWidth = PDRectangle.A4.getWidth() * apronMetadataTableWidthCm / 21;
        final TTFFile font = ServiceConfig.getApronMetadataTableFont();
        
        Map<Integer, Pair<String, Double>> lengthMap = new HashMap<>();
        
        
        for (int i = 0; i < info.size(); i++) {
            String string = info.get(i);
            
            //Now we know how many pixels each entry takes. Then figure out how many lines this corresponds to, by dividing with the
            //line length
            double lines = getNumLines(string, apronMetadataTableFontSize, font, lineWidth);
            if (i < 2) { //The two first entries are written even if empty
                lines = Math.max(lines, 1);
            }
            lengthMap.put(i, MutablePair.of(string, lines));
        }
        
        double usedLines = lengthMap.values().stream().mapToDouble(Pair::getValue).sum();
        
        final double maxLines = ServiceConfig.getMaxLinesForApron(apronType);
        
        //If is is to many lines, remove last line from the longest, and keep going until reduced to acceptable level
        while (usedLines > maxLines) {
            //TODO If multiple, prefer the last entry
            Pair<String, Double> longestEntry = lengthMap.values()
                                                         .stream()
                                                         .max(Map.Entry.comparingByValue())
                                                         .get();
            
            double newValue = longestEntry.getValue() - 1;
            longestEntry.setValue(newValue);
            
            usedLines = lengthMap.values().stream().mapToDouble(Pair::getValue).sum();
        }
        
        
        List<String> result = new ArrayList<>(info.size());
        //Now reduce each entry down to the computed length
        for (Map.Entry<Integer, Pair<String, Double>> entry : lengthMap.entrySet()) {
            
            String text = entry.getValue().getLeft();
            Double finalLength = entry.getValue().getRight();
            
            double currentLength = getNumLines(text, apronMetadataTableFontSize, font, lineWidth);
            
            while (currentLength > finalLength) {
                
                //replace last "word" with ... and fix if the text already ends with ...
                text          = text.replaceFirst("\\s\\S+\\s*$", "...").replaceFirst("\\.{3,}$", "...");
                currentLength = getNumLines(text, apronMetadataTableFontSize, font, lineWidth);
            }
            
            result.add(entry.getKey(), text);
        }
        
        return result;
    }
    
    private static int getNumLines(String string, int fontSize, TTFFile font, float lineWidth) {
        String[] splits = string.split("\\s");
        if (splits.length == 0) { //if empty, assume we use NO lines, as the entry will not be displayed
            return 0;
        }
        int currentlineNr = 0;
        List<String> currentLine = new ArrayList<>();
        for (String word : splits) {
            currentLine.add(word);
            String currentLinePlusWord = String.join(" ", currentLine);
            float widthOfCurrentLinePlusWord = PdfUtils.calculateTextLengthPixelsFop(currentLinePlusWord,
                                                                                     fontSize,
                                                                                     font,
                                                                                     ServiceConfig.getFontWidthMap());
            if (widthOfCurrentLinePlusWord > lineWidth) {
                currentlineNr += 1;
                currentLine = new ArrayList<>(List.of(word));
            }
        }
        return currentlineNr + 1;
    }
    
    
}
