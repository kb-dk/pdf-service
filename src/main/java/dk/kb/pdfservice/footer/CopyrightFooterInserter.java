package dk.kb.pdfservice.footer;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

public class CopyrightFooterInserter {
    
    private static final Logger log = LoggerFactory.getLogger(CopyrightFooterInserter.class);
    
    
    public static void insertCopyrightFooter(PDDocument doc)
            throws IOException {
        
        final PDType1Font font = PDType1Font.HELVETICA;
        final java.util.List<String> copyrightFooterTexts = ServiceConfig.getCopyrightFooterText();
        
        
        final Color textboxColor = ServiceConfig.getCopyrightFooterBackgroundColor();
        final Color textColor = ServiceConfig.getCopyrightFooterColor();
        float textAlpha = ServiceConfig.getCopyrightFooterTransparency();
        float backgroundAlpha = ServiceConfig.getCopyrightFooterBackgroundTransparency();
    
    
        for (PDPage p : doc.getPages()) {
            
            
            //TODO cropbox or mediabox?
            //Mediabox can be larger for images, but cropbox should correspond to the page
            //cropbox <= mediebox always, I think.
            PDRectangle page = p.getCropBox();
            
            float ratio = (float) Math.sqrt(page.getWidth() * page.getHeight() / (A4.getHeight() * A4.getWidth()));
            float boxHeight = page.getHeight();
            float a4Height = A4.getHeight();
            float boxWidth = page.getWidth();
            float a4Width = A4.getWidth();
            
            float fontSize = relative(p, ratio, ServiceConfig.getCopyrightFooterFontSize().floatValue());
            
            float footer_height = getLineHeight(fontSize);
            
            try (var contentStream = new PDPageContentStream(doc,
                                                             p,
                                                             PDPageContentStream.AppendMode.APPEND,
                                                             true,
                                                             true)) {
                
                contentStream.setRenderingMode(RenderingMode.FILL);
    
    
                int footerTextLineNumber =0;
                for (String copyrightFooterText : copyrightFooterTexts) {
    
                    //Text width is linear with font-size so just calc it with font size 1
                    float textWidth1 = calculateTextLengthPixels(copyrightFooterText, 1, font);
                    
                    float textboxWidth1 = calculateTextLengthPixels("  " + copyrightFooterText + "  ", 1, font);
    
                    contentStream.moveTo(0, 0);//Ensure we start at lowest left corner
                    //contentStream.saveGraphicsState();
                    {
                        contentStream.setNonStrokingColor(textboxColor);
                        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                        graphicsState.setNonStrokingAlphaConstant(backgroundAlpha);
                        contentStream.setGraphicsStateParameters(graphicsState);
    
                        final float textboxY = footer_height * .8f + (footerTextLineNumber++ * footer_height);
                        final float textboxW = textboxWidth1 * fontSize;
                        final float textboxX = (page.getWidth() - textboxW) / 2;
                        contentStream.addRect(textboxX, textboxY, textboxW, footer_height);
                        contentStream.fill();
                    }
                    //contentStream.restoreGraphicsState();
    
                    contentStream.setNonStrokingColor(textColor);
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setNonStrokingAlphaConstant(textAlpha);
                    contentStream.setGraphicsStateParameters(graphicsState);
    
                    float text_width = textWidth1 * fontSize;
                    
                    //Centered text
                    final float x = (page.getWidth() - text_width) / 2;
                    
                    contentStream.beginText();
                    
                    contentStream.setFont(font, fontSize);
                    
                    //y=0 is lowest line, so start line at footer_height
                    contentStream.newLineAtOffset(x, footer_height * footerTextLineNumber);
                    
                    contentStream.showText(copyrightFooterText);
                    contentStream.endText();
                }
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
