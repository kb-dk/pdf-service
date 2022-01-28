package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.utils.PdfUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class PdfTitlePageCleanerTest {
    
    @Test
    void cleanHeaderPages() throws IOException {
        ServiceConfig.initialize("conf/*.yaml");
    
        
        //String testfile = "data/130020902567.pdf";
        //String testfile = "data/130020159147.pdf";
        //String testfile = "data/130020902524.pdf";
        String testfile = "data/130023138892-color.pdf";
        
        //String testfile = "data/115808025307_bw.pdf";
        //String testfile = "data/130021589854-color.pdf";
        try (PDDocument pdDocument = PdfUtils.openDocument(new FileInputStream(testfile))) {
            int numPagesBefore = pdDocument.getNumberOfPages();
            PdfTitlePageCleaner.cleanHeaderPages(pdDocument);
            int numPagesAfter = pdDocument.getNumberOfPages();
            //TODO assert based on pages removed...
            
            try (InputStream requestedPDF = PdfUtils.dumpDocument(pdDocument, testfile)) {
                Files.copy(requestedPDF, Path.of("test.pdf"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        
    }
}
