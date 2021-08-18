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
import java.util.List;

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
        boolean initial = true;

        PDPageTree nbPages = doc.getPages();
        Iterator<PDPage> pddIter = nbPages.iterator();
        int i = 1;
        while (pddIter.hasNext()) {
        //for (PDPage p : doc.getPages()) {

            PDPage pd = pddIter.next();
            System.out.println("Før mediaBox");
            PDRectangle mediaBox = pd.getMediaBox();
            System.out.println("After mediaBox");
            PDRectangle cropbox = pd.getCropBox();
            System.out.println("Efter cropbox");

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
            i++;
        }
        System.out.println("count: " + ++count);
        doc.save(output);
        return doc;
    }

}
