package dk.kb.pdfservice.api.impl;

import org.apache.fop.pdf.PDFCIELabColorSpace;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class PdfBoxCopyrightFooterInserter {
    public static long count;
    public PDDocument insertCopyrightFooter(File input, File output)
            throws IOException {
        PDFParser parser;
        System.out.println("start of PdfBoxCopyrightFooterInserter");
        try (final RandomAccessBufferedFileInputStream rabfis = new RandomAccessBufferedFileInputStream(input)) {
            parser = new PDFParser(rabfis);
            parser.parse();
        }

        final PDDocument doc = parser.getPDDocument();

        PDPageTree nbPages = doc.getPages();
        Iterator<PDPage> pddIter = nbPages.iterator();
        int no_pages = 0;
        while (pddIter.hasNext()) {
            PDPage pd = pddIter.next();
            PDRectangle mediaBox = pd.getMediaBox();
            PDRectangle cropbox = pd.getCropBox();

            float ratio = mediaBox.getHeight() / PDRectangle.A4.getHeight();
            float footer_height = 15 * ratio * pd.getUserUnit();

            mediaBox.setLowerLeftY(mediaBox.getLowerLeftY() - footer_height);
            pd.setMediaBox(mediaBox);
            cropbox.setLowerLeftY(cropbox.getLowerLeftY() - footer_height);
            pd.setCropBox(cropbox);

            try (var contentStream = new PDPageContentStream(doc, pd, PDPageContentStream.AppendMode.PREPEND, true);) {
                contentStream.setRenderingMode(RenderingMode.FILL);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.COURIER, footer_height);

                final float x = mediaBox.getWidth() * 0.10f;
                final float y = -footer_height * 1.2f; //above 100% due to compensation for font height
                contentStream.newLineAtOffset(x, y);
                contentStream.showText("Copyright footer");
                contentStream.endText();
            }
            no_pages++;
        }
        System.out.println("count: " + count++);
        System.out.println("no_pages in pdf-file: " + no_pages);
        doc.save(output);
        return doc;
    }

}
