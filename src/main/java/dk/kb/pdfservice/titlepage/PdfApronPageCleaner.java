package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
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

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PdfApronPageCleaner {
    
    private static final Logger log = LoggerFactory.getLogger(PdfApronPageCleaner.class);
    
    public static void cleanApronPages(PDDocument doc) throws IOException {
        
        log.debug("Starting to strip old apron pages");
        
        //Initialise the text stripper before iterating through the pages
        PDFTextStripper stripper = new PDFTextStripper();
        
        //The reference images we will compare against
        List<BufferedImage> referenceImages = ServiceConfig.getOldHeaderImages();
        
        
        //We assume that all previously inserted header pages contains the text "det kongelige bibliotek"
        //So we remove them, until we find a page that does NOT contain this string. Then the removal stops
        //and we assume that all the rest of the document are real pages
        
        int realPageNumber = 0; //For log purposes
        int pagenumber = 0; //Text Stripper works on page numbers
        pageloop:
        for (PDPage pdfPage : doc.getPages()) {
            pagenumber++;
            realPageNumber++;
            
            log.debug("Examining page {}", realPageNumber);
            List<BufferedImage> pageImages = getImagesFromPage(pdfPage.getResources());
            
            log.debug("Extracted {} images from page {}", pageImages.size(), realPageNumber);
            for (BufferedImage pageImage : pageImages) {
                for (BufferedImage referenceImage : referenceImages) {
                    boolean identical = compareImages(pageImage, referenceImage);
                    if (identical) {
                        doc.removePage(pdfPage);
                        log.info("Removed page {} as it match an oldHeaderImage. Page {} is now the new page {}",
                                 realPageNumber,
                                 realPageNumber+1,
                                 pagenumber);
                        pagenumber--;
                        continue pageloop;
                    }
                }
            }
            log.debug("Page {} did not match an oldHeaderImage", realPageNumber);
            
            
            log.debug("Starting to extract text from page {}",realPageNumber);
            stripper.setStartPage(pagenumber);
            stripper.setEndPage(pagenumber);
            String content = stripper.getText(doc);
            if (content != null) {
                log.trace("Found text '{}' om page {}",content, realPageNumber);
                String pageText = content.replaceAll("\\s+", " ")
                                         .toLowerCase(Locale.ROOT);

                final List<String> headerLines = ServiceConfig.getHeaderLines();
                final boolean headerLineMatchFound = headerLines
                        .stream()
                        .map(s -> s.toLowerCase(Locale.ROOT))
                        .anyMatch(pageText::contains);

                final boolean hasStampOrBarcodeLabel = hasStampOrBarcodeLabel(pageText, headerLines);

                if (headerLineMatchFound && !hasStampOrBarcodeLabel) {
                    doc.removePage(pdfPage);
                    log.info("Removed page {} as it match a known header string. Page {} is now the new page {}",
                             realPageNumber,
                             realPageNumber+1,
                             pagenumber);
                    pagenumber--;
                    continue pageloop;
                } else {
                    log.debug("Text content from page {} does not match known header strings", realPageNumber);
                }
            } else {
                log.debug("No text found on page {}",realPageNumber);
            }
            //If we got here, we did not remove a page, and the removal should stop
            log.info("Page {} is apparently not an old apron page, so stopping removal", realPageNumber);
            break;
        }
    }

    /**
     *
     * @param pageText
     * @param headerLines
     * @return true if we assume to have found either a stamp containing the text beneath or a barcode label
     */
    private static boolean hasStampOrBarcodeLabel(String pageText, List<String> headerLines) {
        final List<String> matchingHeaderlines = headerLines
                .stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(pageText::contains)
                .collect(Collectors.toList());
        final String stampTextInLowerCase = "det kongelige bibliotek dramatisk bibliotek postboks 2149 1016 københavn k danmark";
        final boolean stampTextFound = pageText.contains(stampTextInLowerCase);
        final boolean hasStamp = matchingHeaderlines.contains("DET KONGELIGE BIBLIOTEK".toLowerCase(Locale.ROOT)) && stampTextFound;
        Pattern pattern = Pattern.compile("\\d{12}");
        Matcher matcher = pattern. matcher(pageText);
        boolean found12Digits = matcher.find();
        final boolean barcodeLabelExists = pageText.contains("\\1")|| pageText.contains("1\\")||found12Digits;// "\\1" or "1\\" will most likely be part of barcode if present. If NOT present we look for 12 digits in a row.
        final boolean hasFullRoyalLibraryText = matchingHeaderlines.contains("DET KONGELIGE BIBLIOTEK".toLowerCase(Locale.ROOT));
        final boolean hasAbbreviatedRoyalLibraryText = matchingHeaderlines.contains("det kgl. bibliotek");
        final boolean hasBarcodeLabel = (hasFullRoyalLibraryText || hasAbbreviatedRoyalLibraryText) && barcodeLabelExists;
        return hasStamp || hasBarcodeLabel;
    }

    private static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        
        //TODO if we scale UP, we already know that this is a failure, as the reference images are SCALED DOWN versions of the apron
        //But do we know this in general?
        
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
        
        
        //TODO There MUST be a faster way to do this...
        
        //How to perhaps do it in java 17
        //
        //IntBuffer intBufferA = pixelsAsIntBuffer(imgA);
        //ByteVector vectorA = IntVector.fromArray(IntVector.SPECIES_PREFERRED, intBufferA.array(), 0).reinterpretAsBytes();
        //IntBuffer intBufferB = pixelsAsIntBuffer(imgB);
        //ByteVector vectorB = IntVector.fromArray(IntVector.SPECIES_PREFERRED, intBufferB.array(), 0).reinterpretAsBytes();
        //
        //
        //int vectorDifference = Arrays.stream(vectorA.sub(vectorB).abs().toIntArray()).sum();
        
        
        //Outer loop for rows(height)
        // treating images likely 2D matrix
        for (int y = 0; y < height; y++) {
            
            // Inner loop for columns(width)
            for (int x = 0; x < width; x++) {
                //We have to separate the colors, or otherwise red will matter more then blue which will matter more than gren
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
                if (percentage > ServiceConfig.getOldHeaderImagesmMaxDifferenceAllowedForMatch() * 100) {
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
