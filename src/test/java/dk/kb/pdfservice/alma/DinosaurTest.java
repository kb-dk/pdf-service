package dk.kb.pdfservice.alma;


import dk.kb.util.xml.XML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.yaz4j.Connection;
import org.yaz4j.PrefixQuery;
import org.yaz4j.Record;
import org.yaz4j.ResultSet;
import org.yaz4j.exception.ZoomException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

public class DinosaurTest {
    @Test
    public void test() {
        try (Connection con = new Connection("lx2.loc.gov/LCDB", 0)) {
            con.setSyntax("usmarc");
            con.connect();
            ResultSet set = con.search(new PrefixQuery("@attr 1=7 0253333490"));
            Record rec = set.getRecord(0);
            System.out.println(rec.render());
        } catch (ZoomException ze) {
            Assertions.fail(ze.getMessage());
        }
    }
    
    /**
     * Limb Servicen bruger z39.50 protokollen til fritekst søgning via:
     * z39.50 server
     * KB server
     * Syntax MARC21
     * Encoding utf-8
     * All fields
     * PROD:
     * Base address kbdk-kgl.alma.exlibrisgroup.com:1921/45KBDK_KGL
     * SANDKASSE
     * Base address kbdk-kgl-psb.alma.exlibrisgroup.com:1921/45KBDK_KGL
     * NB! Check at med en stregkode f.eks. 113904000025
     */
    @Test
    public void testAlma()
            throws ZoomException, IOException, ParserConfigurationException, SAXException, TransformerException {
        AlmaZ93Client client = new AlmaZ93Client("kbdk-kgl-psb.alma.exlibrisgroup.com",
        1921,
        "45KBDK_KGL");
        List<Document> results = client.search("/e-mat/dod/113904000025-color.pdf");
        for (Document result : results) {
            System.out.println(XML.domToString(result));
        }
        
    }
}
