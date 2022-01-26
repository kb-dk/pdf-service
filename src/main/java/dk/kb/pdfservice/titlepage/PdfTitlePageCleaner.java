package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PdfTitlePageCleaner {
    
    private static final Logger log = LoggerFactory.getLogger(PdfTitlePageCleaner.class);
    
    public static void cleanHeaderPages(PDDocument doc) throws IOException {
        
        //Initialise the text stripper before iterating through the pages
        PDFTextStripper stripper = new PDFTextStripper();
        
        //The reference images we will compare against
        List<BufferedImage> referenceImages = FileUtils.listFiles(ServiceConfig.getOldHeaderImageDir(), new String[]{"png"}, false)
                                                       .stream()
                                                       .map(file -> {
                                                           try {
                                                               return ImageIO.read(file);
                                                           } catch (IOException e) {
                                                               throw new UncheckedIOException(e);
                                                           }
                                                       })
                                                       .collect(Collectors.toList());
        
        
        //We assume that all previously inserted header pages contains the text "det kongelige bibliotek"
        //So we remove them, until we find a page that does NOT contain this string. Then the removal stops
        //and we assume that all the rest of the document are real pages
        int pagenumber = 0;
        pageloop:
        for (PDPage p : doc.getPages()) {
            pagenumber++;
            
            
            List<BufferedImage> pageImages = getImagesFromPage(p.getResources());
            
            for (BufferedImage pageImage : pageImages) {
                for (BufferedImage referenceImage : referenceImages) {
                    boolean identical = compareImages(pageImage, referenceImage);
                    if (identical) {
                        doc.removePage(p);
                        pagenumber--;
                        log.info("Removed page ({}) as it match an oldHeaderImage", pagenumber);
                        continue pageloop;
                    }
                }
            }
            
            
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
                    log.info("Removed page ({}) as it match an known header string", pagenumber);
                    continue pageloop;
                }
            }
            //If we got here, we did not remove a page, and the removal should stop
            log.info("Found page ({}) that is not an old apron page, so stopping removal", pagenumber);
            break;
        }
    }
    
    private static boolean compareImages(BufferedImage imgA, BufferedImage imgB) throws IOException {
        
        //TODO if we scale UP, we already know that this is a failure, as the reference images are SCALED DOWN versions of the apron
        
        imgA = scaleImage(imgA, imgB.getWidth(), imgB.getHeight()); //scale A to Bs size
        // By now, images are of same size
        
        //ImageIO.write(imgA, "PNG", new File("image.png"));
        
        
        
        int width = imgA.getWidth();
        int height = imgA.getHeight();
        
        long difference = 0;
    
        // Total number of red pixels = width * height
        // Total number of blue pixels = width * height
        // Total number of green pixels = width * height
        // So total number of pixels = width * height *
        // 3
        double total_pixels = width * height * 3;
        
        
        // treating images likely 2D matrix
        
        //There MUST be a faster way to do this...
        // Outer loop for rows(height)
        for (int y = 0; y < height; y++) {
            
            // Inner loop for columns(width)
            for (int x = 0; x < width; x++) {
                //We have to separate the colors, or otherwise red will matter more then blue which will matter more than gren
                
                //TODO read out the entire array and compare, do NOT do this
                int rgbA = imgA.getRGB(x, y);
                int rgbB = imgB.getRGB(x, y);
                
                int redA = (rgbA >> 16) & 0xff;
                int redB = (rgbB >> 16) & 0xff;
                difference += Math.abs(redA - redB);
                
                int greenA = (rgbA >> 8) & 0xff;
                int greenB = (rgbB >> 8) & 0xff;
                difference += Math.abs(greenA - greenB);
                
                int blueA = (rgbA) & 0xff;
                int blueB = (rgbB) & 0xff;
                difference += Math.abs(blueA - blueB);
    
                // There are 255 values of pixels in total
                double percentage
                        = (difference / total_pixels / 255) * 100;
                if (percentage > 1.0){ //TODO configurable percentage
                    //If we reach above the threshold, just break rather than compare the rest of the image data
                    //The difference is only increasing so it will never get better
                    return false;
                }
            }
        }
        return true;
    }
    
    
    public static BufferedImage scaleImage(BufferedImage image, int newWidth, int newHeight) {
        BufferedImage scaledImage = new BufferedImage(newWidth,
                                                      newHeight,
                                                      BufferedImage.TYPE_INT_ARGB);
        
        double sx = newWidth / (image.getWidth() + 0.0);
        double sy = newHeight / (image.getHeight() + 0.0);
        scaledImage = new AffineTransformOp(AffineTransform.getScaleInstance(sx, sy),
                                            AffineTransformOp.TYPE_BICUBIC)
                .filter(image, scaledImage);
        
        return scaledImage;
    }
    
    /**
     * * https://stackoverflow.com/a/37664125/4527948
     *
     * @param resources
     * @return
     * @throws IOException
     */
    private static List<BufferedImage> getImagesFromPage(PDResources resources) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        
        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);
            
            if (xObject instanceof PDFormXObject) {
                images.addAll(getImagesFromPage(((PDFormXObject) xObject).getResources()));
            } else if (xObject instanceof PDImageXObject) {
                images.add(((PDImageXObject) xObject).getImage());
            }
        }
        
        return images;
    }
}
