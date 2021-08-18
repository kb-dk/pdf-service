package dk.kb.pdfservice.api.impl;

import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

import java.io.File;
import java.io.IOException;

public class PdfBoxCopyrightFooterInserter {
    public static long count;
    public static PDDocument insertCopyrightFooter(File input, File output)
            throws IOException {
        PDFParser parser;
        System.out.println("start of PdfBoxCopyrightFooterInserter");
        try (final RandomAccessBufferedFileInputStream rabfis = new RandomAccessBufferedFileInputStream(input)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }

        final PDDocument doc = parser.getPDDocument();
        boolean initial = true;

        for (PDPage p : doc.getPages()) {
            if (initial) { //nasty way of skipping first page
                initial = false;
                continue;
            }

            PDRectangle mediaBox = p.getMediaBox();
            PDRectangle cropbox = p.getCropBox();

            float ratio = mediaBox.getHeight() / PDRectangle.A4.getHeight();
            float footer_height = 15 * ratio * p.getUserUnit();

            mediaBox.setLowerLeftY(mediaBox.getLowerLeftY() - footer_height);
            p.setMediaBox(mediaBox);
            cropbox.setLowerLeftY(cropbox.getLowerLeftY() - footer_height);
            p.setCropBox(cropbox);

            try (var contentStream = new PDPageContentStream(doc, p, PDPageContentStream.AppendMode.PREPEND, true);) {
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
        System.out.println("count: " + ++count);
        doc.save(output);
        return doc;
    }

}
