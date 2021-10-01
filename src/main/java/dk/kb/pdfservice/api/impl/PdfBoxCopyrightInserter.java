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
import org.apache.pdfbox.tools.PDFBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
    
            int pagecount = 0;
            for (PDPage p : doc.getPages()) {
                pagecount++;
                if (!foundRealPage) {
                    // get text of a certain page
                    stripper.setStartPage(pagecount);
                    stripper.setEndPage(pagecount);
                    String content = stripper.getText(doc);
                    if (content.toLowerCase().contains("det kongelige bibliotek")) {
                        doc.removePage(p);
                        pagecount--;
                        continue;
                    } else {
                        foundRealPage = true;
                    }
                }
                
                log.debug("before mediabox");
                PDRectangle mediaBox = p.getMediaBox();
                PDRectangle cropbox = p.getCropBox();
        
                float ratio = mediaBox.getHeight() / PDRectangle.A4.getHeight();
                float footer_height = 15 * ratio * p.getUserUnit();
        
                mediaBox.setLowerLeftY(mediaBox.getLowerLeftY() - footer_height);
                p.setMediaBox(mediaBox);
                cropbox.setLowerLeftY(cropbox.getLowerLeftY() - footer_height);
                p.setCropBox(cropbox);
                log.debug("Before try");
                try (var contentStream = new PDPageContentStream(doc,
                                                                 p,
                                                                 PDPageContentStream.AppendMode.PREPEND,
                                                                 true)) {
                    log.debug("Handled page");
                    contentStream.setRenderingMode(RenderingMode.FILL);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.COURIER, footer_height);
            
                    final float x = p.getMediaBox().getWidth() * 0.10f;
                    final float y = -footer_height * 1.2f; //above 100% due to compensation for font height
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
}
