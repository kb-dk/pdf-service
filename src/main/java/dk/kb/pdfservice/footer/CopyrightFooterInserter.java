package dk.kb.pdfservice.footer;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.io.IOException;

public class CopyrightFooterInserter {
    
    private static final Logger log = LoggerFactory.getLogger(CopyrightFooterInserter.class);
    
    
    public static void insertCopyrightFooter(PDDocument doc)
            throws IOException {
        
        final PDType1Font font = PDType1Font.HELVETICA;
        final String copyrightFooterText = ServiceConfig.getCopyrightFooterText();
        
        //Text width is linear with font-size so just calc it with font size 1
        float textWidth1 = calculateTextLengthPixels(copyrightFooterText, 1, font);
        
        float textboxWidth1 = calculateTextLengthPixels("  " + copyrightFooterText + "  ", 1, font);
    
        final Color textboxColor = ServiceConfig.getCopyrightFooterBackgroundColor();
    
    
        for (PDPage p : doc.getPages()) {
            
            //TODO cropbox or mediabox?
            //Mediabox can be larger for images, but cropbox should correspond to the page
            //cropbox <= mediebox always, I think.
            PDRectangle box = p.getCropBox();
            
            boolean landscape = box.getWidth() > box.getHeight();
            float ratio = box.getWidth() / (landscape ? PDRectangle.A4.getHeight() : PDRectangle.A4.getWidth());
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
                contentStream.moveTo(0, 0);//Ensure we start at lowest left corner
                
                //resetContext to ensure we are not scaled, rotated or anything else
                contentStream.setRenderingMode(RenderingMode.FILL);
                
                
                contentStream.saveGraphicsState();
    
                contentStream.setNonStrokingColor(textboxColor);
                
                final float textboxY = footer_height * .8f;
                final float textBoxH = footer_height * 1.1f;
                final float textboxW = textboxWidth1 * fontSize;
                final float textboxX = (box.getWidth() - textboxW) / 2;
                
                
                contentStream.addRect(textboxX, textboxY, textboxW, textBoxH);
                contentStream.fill();
                
                contentStream.restoreGraphicsState();
                
                
                float text_width = textWidth1 * fontSize;
                
                //Centered text
                final float x = (box.getWidth() - text_width) / 2;
                
                contentStream.beginText();
                
                contentStream.setFont(font, fontSize);
                
                //y=0 is lowest line, so start line at footer_height
                contentStream.newLineAtOffset(x, footer_height);
                
                contentStream.showText(copyrightFooterText);
                contentStream.endText();
            }
        }
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
