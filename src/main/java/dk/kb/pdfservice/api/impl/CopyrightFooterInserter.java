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

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class CopyrightFooterInserter {
    
    private static final Logger log = LoggerFactory.getLogger(CopyrightFooterInserter.class);
    
    public static InputStream cleanHeaderPages(InputStream input) throws IOException {
        PDFParser parser;
        try (final RandomAccessRead rabfis = new RandomAccessBufferedFileInputStream(input)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }
    
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
            }
            log.debug("before doc.save(output)");
            doc.save(new BufferedOutputStream(output));
        }
        return output.toInputStream();
    }
    
    
    public static InputStream insertCopyrightFooter(InputStream input)
            throws IOException {
        PDFParser parser;
        try (final RandomAccessRead rabfis = new RandomAccessBufferedFileInputStream(input)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }
        
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (final PDDocument doc = parser.getPDDocument();) {
    
            final PDType1Font font = PDType1Font.HELVETICA;
            final String copyrightFooterText = ServiceConfig.getCopyrightFooterText();
    
            //Text width is linear with font-size so just calc it with font size 1
            float textWidth1 = calculateTextLengthPixels(copyrightFooterText, 1, font);
            
            float textboxWidth1 = calculateTextLengthPixels("  "+copyrightFooterText+"  ", 1, font);
    
            for (PDPage p : doc.getPages()) {
                
                //TODO cropbox or mediabox?
                //Mediabox can be larger for images, but cropbox should correspond to the page
                //cropbox <= mediebox always, I think.
                PDRectangle box = p.getCropBox();
    
                boolean landscape = box.getWidth() > box.getHeight();
                float ratio = box.getWidth() / (landscape? PDRectangle.A4.getHeight() : PDRectangle.A4.getWidth());
                float boxHeight = box.getHeight();
                float a4Height = PDRectangle.A4.getHeight();
                float boxWidth = box.getWidth();
                float a4Width = PDRectangle.A4.getWidth();
                
                float fontSize = relative(p, ratio, ServiceConfig.getCopyrightFooterFontSize().floatValue());
             
                float footer_height = getLineHeight(fontSize);
                
                try (var contentStream = new PDPageContentStream(doc,
                                                                 p,
                                                                 PDPageContentStream.AppendMode.APPEND,
                                                                 true,
                                                                 true)) {
                    contentStream.moveTo(0,0);//Ensure we start at lowest left corner
    
                    //resetContext to ensure we are not scaled, rotated or anything else
                    contentStream.setRenderingMode(RenderingMode.FILL);
                    
                    
                    contentStream.saveGraphicsState();
    
                    contentStream.setNonStrokingColor(Color.YELLOW);
                    
                    final float textboxY = footer_height * .8f;
                    final float textBoxH = footer_height * 1.1f;
                    final float textboxW = textboxWidth1 * fontSize;
                    final float textboxX = (box.getWidth()-textboxW)/2;
                    
                    
                    contentStream.addRect(textboxX, textboxY, textboxW, textBoxH);
                    contentStream.fill();
                    
                    contentStream.restoreGraphicsState();
    
    
                    float text_width = textWidth1 * fontSize;
                    
                    //Centered text
                    final float x = (box.getWidth()-text_width)/2;
    
                    contentStream.beginText();
                    
                    contentStream.setFont(font, fontSize);
                    
                    //y=0 is lowest line, so start line at footer_height
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
