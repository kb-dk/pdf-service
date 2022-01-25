package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PdfTitlePageCleaner {
    public static void cleanHeaderPages(PDDocument doc) throws IOException {
        
    
        PDFTextStripper stripper = new PDFTextStripper();
        
        //We assume that all previously inserted header pages contains the text "det kongelige bibliotek"
        //So we remove them, until we find a page that does NOT contain this string. Then the removal stops
        //and we assume that all the rest of the document are real pages
        int pagenumber = 0;
        for (PDPage p : doc.getPages()) {
            pagenumber++;
    
            List<RenderedImage> images = getImagesFromResources(p.getResources());
    
            //TODO check if this image match something we include in conf/
            
            //Only extract text from this page
            stripper.setStartPage(pagenumber);
            stripper.setEndPage(pagenumber);
            String content = stripper.getText(doc);
            if (content != null) {
                String pageText = content.replaceAll("\\s+", " ")
                                         .toLowerCase(Locale.ROOT);
                if (ServiceConfig.getHeaderLines()
                                 .stream()
                                 .map(s -> s.toLowerCase(Locale.ROOT))
                                 .anyMatch(pageText::contains)) {
                    doc.removePage(p);
                    pagenumber--;
                    continue;
                }
            }
            //If we got here, we did not remove a page, and the removal should stop
            break;
            
        }
    }
    
    /**
     *      * https://stackoverflow.com/a/37664125/4527948
     * @param resources
     * @return
     * @throws IOException
     */
    private static List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
        List<RenderedImage> images = new ArrayList<>();
        
        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);
            
            if (xObject instanceof PDFormXObject) {
                images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
            } else if (xObject instanceof PDImageXObject) {
                images.add(((PDImageXObject) xObject).getImage());
            }
        }
        
        return images;
    }
}
