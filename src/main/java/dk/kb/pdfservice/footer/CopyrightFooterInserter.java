package dk.kb.pdfservice.footer;

import com.google.common.collect.Lists;
import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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
        //TODO log something so we can see what is happening...
        
        final PDType1Font font = PDType1Font.HELVETICA;
        //We add them from the bottom up, to reverse to preserve the order from the config file.
        final java.util.List<String> copyrightFooterTexts = Lists.reverse(ServiceConfig.getCopyrightFooterText());
        
        
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
            //float boxHeight = page.getHeight();
            //float a4Height = A4.getHeight();
            //float boxWidth = page.getWidth();
            //float a4Width = A4.getWidth();
            
            float fontSize = relative(p, ratio, ServiceConfig.getCopyrightFooterFontSize().floatValue());
            
            float footer_height = getLineHeight(fontSize);
    
            //Slightly below footer_height to account for letters like 'j' going below the line
            float textboxBottomMargin = footer_height * .8f;
            
            try (var contentStream = new PDPageContentStream(doc,
                                                             p,
                                                             PDPageContentStream.AppendMode.APPEND,
                                                             true,
                                                             true)) {
                //Reset content boolean to ensure we do not inherit some state from the page
                
                contentStream.setRenderingMode(RenderingMode.FILL);
                
                
                int footerTextLineNumber = 0;
                for (String copyrightFooterText : copyrightFooterTexts) {
                    
                    //TODO calculate for font1 up front, rahter than for each page
                    //font is page-dependent, so we have to calculate this for each page
                    float textWidth = calculateTextLengthPixels(copyrightFooterText, fontSize, font);
                    float textboxWidth = calculateTextLengthPixels("  " + copyrightFooterText + "  ", fontSize, font);
                    
                    contentStream.moveTo(0, 0);//Ensure we start at lowest left corner
                    
                    { // TEXTBOX
                        contentStream.setNonStrokingColor(textboxColor);
                        setTransparency(contentStream, backgroundAlpha);
                        
                        final float textboxY = textboxBottomMargin +  (footerTextLineNumber * footer_height);
                        final float textboxX = (page.getWidth() - textboxWidth) / 2;
                        contentStream.addRect(textboxX, textboxY, textboxWidth, footer_height);
                        contentStream.fill();
                    }
    
                    { // TEXT
                        contentStream.setNonStrokingColor(textColor);
                        setTransparency(contentStream, textAlpha);
    
                        //Centered text
                        final float textX = (page.getWidth() - textWidth) / 2;
    
                        contentStream.beginText();
    
                        contentStream.setFont(font, fontSize);
    
                        //y=0 is lowest line, so start line at footer_height
                        contentStream.newLineAtOffset(textX, footer_height * (footerTextLineNumber+1));
    
                        contentStream.showText(copyrightFooterText);
                        contentStream.endText();
                    }
                    //Increment line number
                    footerTextLineNumber += 1;
                }
            }
        }
    }
    
    private static void setTransparency(PDPageContentStream contentStream,
                                        float backgroundAlpha) throws IOException {
        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant(backgroundAlpha);
        contentStream.setGraphicsStateParameters(graphicsState);
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
