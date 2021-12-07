package dk.kb.pdfservice.titlepage;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class PdfTitlePageInserter {
    
    public static Logger log = LoggerFactory.getLogger(PdfTitlePageInserter.class);
    
    public static InputStream mergeFrontPageWithPdf(InputStream apronFile,
                                                    InputStream resultingPdf)
            throws IOException {
        final var completePDF = new ByteArrayOutputStream();
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSource(apronFile);
        pdfMergerUtility.addSource(resultingPdf);
        pdfMergerUtility.setDestinationStream(completePDF);
        //TODO Configurable memory settings
        //Just use 100MBs and unlimited temp files
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 100));
        completePDF.flush(); //just in case it is not done automatically
        
        return completePDF.toInputStream();
        
    }
    
    
}
