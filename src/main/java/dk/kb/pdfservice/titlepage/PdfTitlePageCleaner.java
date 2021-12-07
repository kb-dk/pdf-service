package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.Locale;

public class PdfTitlePageCleaner {
    public static void cleanHeaderPages(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        
        boolean foundRealPage = false;
        
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
                foundRealPage = true;
            }
        }
    }
}
