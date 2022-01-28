package dk.kb.pdfservice.utils;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PdfUtils {
    public static PDDocument openDocument(InputStream pdData) throws IOException {
        PDFParser parser;
        try (final RandomAccessRead rabfis = new RandomAccessBufferedFileInputStream(pdData)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }
        return parser.getPDDocument();
    }
    
    public static InputStream dumpDocument(PDDocument pdDocument, String name) throws IOException {
        Path tempFile = Files.createTempFile(ServiceConfig.getTempPath(), name, ".pdf");
        
        DeferredFileOutputStream output = new DeferredFileOutputStream(ServiceConfig.getTempThresholdBytes(),
                                                                       tempFile.toFile());
        try (output) {
            pdDocument.save(new BufferedOutputStream(output));
        }
        
        return new ProxyInputStream(new BufferedInputStream(output.toInputStream())) {
            @Override
            public void close() throws IOException {
                super.close();
                // Deletes the spilled file, if nessesary
                //file is never null, as we specify the exact file in the constructor above
                Files.deleteIfExists(output.getFile().toPath());
            }
        };
    }
    
    public static float calculateTextLengthPixelsPdfBox(String text,
                                                        float fontSize,
                                                        PDFont font) {
        if (text == null) {
            return 0;
        }
        float stringWidth;
        try {
            stringWidth = font.getStringWidth(text);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get width of string '" + text + "'", e);
        }
        
        float width = (fontSize * stringWidth);
        return (width / 1000f);
    }
    
    
    public static float calculateTextLengthPixelsFop(String text,
                                                     float fontSize,
                                                     TTFFile font,
                                                     Map<Integer, OFMtxEntry> fontWidthMap) {
        if (text == null) {
            return 0;
        }
        //TODO do NOT do this for each invocation
        
        float stringWidth;
        try {
            int sum = text.codePoints().map(codepoint -> fontWidthMap.get(codepoint).getWx()).sum();
            int ttfUnitValue = font.convertTTFUnit2PDFUnit(sum);
            stringWidth = ttfUnitValue * fontSize / 1000;
            return stringWidth;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to get width of string '" + text + "'", e);
        }
    }
    
}
