package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.config.ServiceConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

class PdfTitlePageCleanerTest {
    
    @Test
    @Disabled("Depends on test data not in git repo")
    void cleanHeaderPages() throws IOException {
        ServiceConfig.initialize("conf/*.yaml");
        
        
        //String testfile = "data/130020902567.pdf";
        //String testfile = "data/130020159147.pdf";
        //String testfile = "data/130020902524.pdf";
        String testfile = "data/130023138892-color.pdf";
        
        //String testfile = "data/115808025307_bw.pdf";
        //String testfile = "data/130021589854-color.pdf";
        try (PDDocument pdDocument = PDDocument.load(new FileInputStream(testfile),
                                                     ServiceConfig.getMemoryUsageSetting())) {
            int numPagesBefore = pdDocument.getNumberOfPages();
            PdfApronPageCleaner.cleanApronPages(pdDocument);
            int numPagesAfter = pdDocument.getNumberOfPages();
            //TODO assert based on pages removed...
            
            
            pdDocument.save(testfile);
            
        }
        
    }
}
