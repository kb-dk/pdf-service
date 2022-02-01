package dk.kb.pdfservice.utils;

import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.Map;

public class PdfUtils {
    
    
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
