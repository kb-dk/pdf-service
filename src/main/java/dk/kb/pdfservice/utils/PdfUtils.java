package dk.kb.pdfservice.utils;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
