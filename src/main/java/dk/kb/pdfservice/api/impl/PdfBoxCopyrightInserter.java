package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class PdfBoxCopyrightInserter {
    
    private static final Logger log = LoggerFactory.getLogger(PdfBoxCopyrightInserter.class);
    
    public static InputStream insertCopyrightFooter(InputStream input)
            throws IOException {
        PDFParser parser;
        log.debug("start of PdfBoxCopyrightInserter");
        try (final RandomAccessRead rabfis = new RandomAccessBufferedFileInputStream(input)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }
        log.debug("After try RandomAccessBuffer");
        
        PDFTextStripper stripper = new PDFTextStripper();
        
        boolean foundRealPage = false;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (final PDDocument doc = parser.getPDDocument();) {
            
            //We assume that all previously inserted header pages contains the text "det kongelige bibliotek"
            //So we remove them, until we find a page that does NOT contain this string. Then the removal stops
            //and we assume that all the rest of the document are real pages
            int pagenumber = 0;
            for (PDPage p : doc.getPages()) {
                pagenumber++;
                if (!foundRealPage) {
                    //Only extract text from this page
                    stripper.setStartPage(pagenumber);
                    stripper.setEndPage(pagenumber);
                    String content = stripper.getText(doc);
                    if (content != null) {
                        if (ServiceConfig.getHeaderLines()
                                         .stream()
                                         .anyMatch(line -> content.replaceAll("\\s+", " ")
                                                                  .toLowerCase(Locale.ROOT)
                                                                  .contains(line.toLowerCase(Locale.ROOT)))) {
                            doc.removePage(p);
                            pagenumber--;
                            continue;
                        }
                    }
                    //If we got here, we did not remove a page, and the removal should stop
                    foundRealPage = true;
                }
                
                
                PDRectangle cropbox = p.getMediaBox();
                
                
                PDRectangle box = cropbox;
    
                boolean landscape = box.getWidth() > box.getHeight();
                float ratio = box.getHeight() / (landscape? PDRectangle.A4.getWidth() : PDRectangle.A4.getHeight());
                //log.debug("box height {}, A4 height {}", box.getHeight(), PDRectangle.A4.getHeight());
                //log.debug("box width {}, A4 width {}", box.getWidth(), PDRectangle.A4.getWidth());
                float fontSize = 14;
                float relative_fontsize = relative(p, ratio, fontSize);
                float footer_height = getLineHeight(fontSize);
                
       
                //log.debug("Before try");
                try (var contentStream = new PDPageContentStream(doc,
                                                                 p,
                                                                 PDPageContentStream.AppendMode.APPEND,
                                                                 true,
                                                                 true)) {
                    //log.debug("Handled page");
                    contentStream.setRenderingMode(RenderingMode.FILL);
                    final PDType1Font courier = PDType1Font.HELVETICA;
                    
                    contentStream.moveTo(0,0);//Ensure we start at lowest left corner
                    contentStream.beginText();
                    
                    
                    contentStream.setFont(courier, relative_fontsize);
                    final String copyrightFooterText = ServiceConfig.getCopyrightFooterText();
                    float text_width = calculateTextLengthPixels(copyrightFooterText, relative_fontsize, courier);
                    
                    //Centered text
                    final float x = (box.getWidth()-text_width)/2;
    
                    contentStream.newLineAtOffset(x, footer_height);
                    
                    contentStream.showText(copyrightFooterText);
                    contentStream.endText();
                }
            }
            log.debug("before doc.save(output)");
            doc.save(new BufferedOutputStream(output));
        }
        return output.toInputStream();
    }
    
    private static float relative(PDPage p, float ratio, float fontSize) {
        return fontSize * ratio * p.getUserUnit();
    }
    
    
    public static int getLineHeight(double fontSize) {
        return Math.toIntExact(Math.round(fontSize * 1.25));
    }
    
    
    protected static float calculateTextLengthPixels(String text,
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
