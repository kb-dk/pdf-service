package dk.kb.pdfservice.api.impl;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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
                        content = content.replaceAll("\\s+", " ");
                        if (content.toLowerCase(Locale.getDefault()).contains("det kongelige bibliotek")) {
                            doc.removePage(p);
                            pagenumber--;
                            continue;
                        }
                    }
                    //If we got here, we did not remove a page, and the removal should stop
                    foundRealPage = true;
                }
                
                
                PDRectangle mediaBox = p.getMediaBox();
                
                float ratio = mediaBox.getHeight() / PDRectangle.A4.getHeight();
                float fontSize = 15 * ratio * p.getUserUnit();
                float footer_height = getLineHeight(fontSize);
    
                float lowerLeftY = mediaBox.getLowerLeftY() - footer_height;
                mediaBox.setLowerLeftY(lowerLeftY);
                p.setMediaBox(mediaBox);
                log.debug("mediebox {}", mediaBox);
                
                PDRectangle cropbox = p.getCropBox();
                cropbox.setLowerLeftY(lowerLeftY);
                p.setCropBox(cropbox);
                log.debug("cropbox {}", cropbox);
           
                
                log.debug("Before try");
                try (var contentStream = new PDPageContentStream(doc,
                                                                 p,
                                                                 PDPageContentStream.AppendMode.PREPEND,
                                                                 true)) {
                    log.debug("Handled page");
                    contentStream.setRenderingMode(RenderingMode.FILL);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.COURIER, fontSize);
                    
                    final float x = p.getMediaBox().getWidth() * 0.10f;
                    final float y = -fontSize; //above 100% due to compensation for font height
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText("Copyright footer");
                    contentStream.endText();
                }
            }
            log.debug("before doc.save(output)");
            doc.save(new BufferedOutputStream(output));
        }
        return output.toInputStream();
    }
    
    
    public static int getLineHeight(double fontSize) {
        return Math.toIntExact(Math.round(fontSize * 1.25));
    }
    
}
