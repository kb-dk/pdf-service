package dk.kb.pdfservice.utils;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PdfUtils {
    public static PDDocument openDocument(InputStream pdData) throws IOException {
        PDFParser parser;
        try (final RandomAccessRead rabfis = new RandomAccessBufferedFileInputStream(pdData)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }
        return parser.getPDDocument();
    }
    
    public static InputStream dumpDocument(PDDocument pdDocument) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        pdDocument.save(new BufferedOutputStream(output));
        return output.toInputStream();
    }
    
    public static float calculateTextLengthPixels(String text,
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
                                                  TTFFile font) {
        if (text == null) {
            return 0;
        }
        
        Map<Integer, OFMtxEntry> table = font.getMtx()
                                             .stream()
                                             .collect(Collectors.toMap(OFMtxEntry::getIndex,
                                                                       Function.identity()));
        float stringWidth;
        try {
            //Replace all weird chars with ?. This will make the width a little less precise but it is easier than
            // fixing the font to handle this
            //TODO use actual font, not this crap
            String textWithoutBadChars = text.replaceAll("[^\\p{Print}]", "?");
            stringWidth = font.convertTTFUnit2PDFUnit(text.codePoints().map(codepoint -> table.get(codepoint).getWx()).sum())*fontSize/14;
            //stringWidth = font.getStringWidth(textWithoutBadChars);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to get width of string '" + text + "'", e);
        }
        
        float width = (fontSize * stringWidth);
        return (width / 1000f);
    }
    
}
