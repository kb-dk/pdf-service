package dk.kb.pdfservice.titlepage;

import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.TTFFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;


/*
Test urls

http://localhost:8080/pdf-service/api/getPdf/130009869108.pdf
http://localhost:8080/pdf-service/api/getPdf/130019369456-color.pdf
 */
public class FontThings {
    
    public static void main(String[] args) throws IOException {
        TTFFile ttfFile = new TTFFile(false, true);
    
        File file = new File("pdf-service/conf/fonts/DejaVuSans.ttf").getAbsoluteFile();
        InputStream stream = new FileInputStream(file);
        try {
            FontFileReader reader = new FontFileReader(stream);
            String header = OFFontLoader.readHeader(reader);
            boolean supported = ttfFile.readFont(reader, header, "name");
            if (!supported) {
                return;
            }
        } finally {
            stream.close();
        }
        int stringWidth = 18173;
        String text = "Pape, Joan Carol. Chr.;";
        
    
        /*
        stringWidth = 2613.0
text = {String@4381} "Pape,"

stringWidth = 5059.0
text = {String@4386} "Pape, Joan"

stringWidth = 8004.0
text = {String@4388} "Pape, Joan Carol."


stringWidth = 10449.0
text = {String@4390} "Pape, Joan Carol. Chr.;"
         */
        Map<Integer, OFMtxEntry> table = ttfFile.getMtx()
                                                .stream()
                                                .collect(Collectors.toMap(ofMtxEntry -> ofMtxEntry.getIndex(),
                                                                          ofMtxEntry -> ofMtxEntry));
        int width = ttfFile.convertTTFUnit2PDFUnit(text.codePoints().map(codepoint -> table.get(codepoint).getWx()).sum())*10/14;
        System.out.println(width);
        //for (short i = ttfFile.getFirstChar(); i <= 2*ttfFile.getLastChar(); i++) {
        //
        //    int witch = ttfFile.getCharWidth(i);
        //    System.out.println("Char "+Integer.toHexString(i)+"="+(char)i+" = "+witch);
        //}
        
    }
}
