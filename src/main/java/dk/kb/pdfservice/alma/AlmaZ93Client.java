package dk.kb.pdfservice.alma;

import dk.kb.util.xml.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xml.sax.SAXException;
import org.yaz4j.Connection;
import org.yaz4j.PrefixQuery;
import org.yaz4j.Record;
import org.yaz4j.ResultSet;
import org.yaz4j.exception.ZoomException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AlmaZ93Client {
    private final Logger log = LoggerFactory.getLogger(AlmaZ93Client.class);
    
    private final String host;
    private final int port;
    private final String databaseName;
    
    public AlmaZ93Client(String host, int port, String databaseName) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
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
    
    //    https://developers.exlibrisgroup.com/alma/integrations/z39-50/
    
    //    https://www.indexdata.com/resources/software/yaz4j/
    
    //    https://github.com/indexdata/yaz4j
    
    //    :) 18:10:08 abr@scallop:~$ sudo dnf install libyaz
    
    
    /**
     * “Search” Supported Attribute
     * Term (1016, 1017),
     * Author (1, 1003, 1004)
     * Subject (21),
     * Title (4),
     * ISBN (7),
     * ISSN (8)
     * Date (31),
     * Identifier (12),
     * OCLC Number (1211)
     * <p>
     * “Present” Supported Formats
     * OPAC
     * MARC21/USMARC
     * UNIMARC
     */
    public List<Document> search(String pdfFile)
            throws ZoomException, IOException, ParserConfigurationException, SAXException {
        try (Connection con = new Connection(host, port)) {
            con.setSyntax("unimarc");
            con.setDatabaseName(databaseName);
            con.connect();
            
            try (ResultSet set = con.search(new PrefixQuery("@attr 1=* " + pdfFile))) {
                List<Document> results = new ArrayList<>();
                for (int i = 0; i < set.getHitCount(); i++) {
                    try (Record rec = set.getRecord(i)) {
                        results.add(XML.fromXML(parseMarc(rec.getContent()), true));
                    }
                }
                return results;
            }
        }
    }
    
    public String parseMarc(byte[] marc) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(marc);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
                Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }
}
