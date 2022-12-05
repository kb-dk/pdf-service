package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileInputStream;
import java.io.IOException;

class PdfTitlePageCleanerTest {

    //Asgers test-files, than may be worth testing??
    //String testfile = "data/130020902567.pdf";
    //String testfile = "data/130020159147.pdf";
    //String testfile = "data/130020902524.pdf";
    //String testfile = "data/130023138892-color.pdf";
    //String testfile = "data/115808025307_bw.pdf";
    //String testfile = "data/130021589854-color.pdf";



    @Test
    @Disabled("Depends on test data not in git repo")
    void cleanHeaderNoExistingApron() throws IOException {
        String testfile = "/data1/e-mat/dod/130018854342.pdf"; // Document 95 pages with no existing apron
        cleanDocumentAndVerifyRemainingNumberOfPages(testfile, 95);
    }

    @Test
    @Disabled("Depends on test data not in git repo")
    void testBarcodeLabelPageIsMaintained() throws IOException {
        //Before bugfix, pages containgen Royal Danish Library barcode labels could cause pages to be cleanes as apron-pages
        String testfile = "/data1/e-mat/dod/130023294959-color.pdf"; // Document 3 pages with 1 page existing apron. Next page is containing barcode label
        cleanDocumentAndVerifyRemainingNumberOfPages(testfile, 2);
    }

    @Test
    @Disabled("Depends on test data not in git repo")
    void testBarcodeLabelAndStampPageIsMaintained() throws IOException {
        //Before bugfix, pages containgen Royal Danish Library barcode labels AND "DRAMATISK BIBLIOTET"-stamp could cause pages to be cleanes as apron-pages
        String testfile = "/data1/e-mat/dod/130020641909-color.pdf"; // Document 22 pages with 1 page existing apron. Next page is containing barcode label and stamp
        cleanDocumentAndVerifyRemainingNumberOfPages(testfile, 21);
    }

    @Test
    @Disabled("Depends on test data not in git repo")
    void cleanHeaderAndRemoveEmptyPages() throws IOException {
        String testfile = "/data1/e-mat/dod/130020340488.pdf"; // Document - 112 pages with 1 page existing apron and 77 empty pages after document end
        cleanDocumentAndVerifyRemainingNumberOfPages(testfile, 33);
    }

    @Test
    @Disabled("Depends on test data not in git repo")
    void test() throws IOException {
        String testfile = "/data1/e-mat/dod/130020708043.pdf"; // Document - 106 pages with 6 apron pages containing oldHeaderImage's
        cleanDocumentAndVerifyRemainingNumberOfPages(testfile, 100);
    }

    private void cleanDocumentAndVerifyRemainingNumberOfPages(String testfile, int numberOfPagesAfterCleaning) throws IOException {
        ServiceConfig.initialize("conf/*.yaml");
        try (PDDocument pdDocument = PDDocument.load(new FileInputStream(testfile),
                                                     ServiceConfig.getMemoryUsageSetting())) {
            int numPagesBefore = pdDocument.getNumberOfPages();
            PdfApronPageCleaner.cleanApronPages(pdDocument);
            int numPagesAfter = pdDocument.getNumberOfPages();
            assertEquals(numberOfPagesAfterCleaning, numPagesAfter);
            String testfileAfter = testfile + "unitTestCOPY";
            pdDocument.save(testfileAfter);
        }
    }
}
