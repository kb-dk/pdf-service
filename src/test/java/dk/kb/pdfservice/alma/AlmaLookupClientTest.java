package dk.kb.pdfservice.alma;

import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Optional;


class AlmaLookupClientTest {
    
    @Test
    @Disabled("This is really just PoC for the Z9350 client")
    //TODO make this into a real test
    void getMarc21_2() throws TransformerException, IOException {
        ServiceConfig.initialize("conf/pdf-service-*.yaml");
        
        //Element domApi = AlmaLookupClient.getMarc21("130018854342");
        //System.out.println(XML.domToString(domApi));
        //
        System.out.println();
        
        Element domZ9350 = AlmaLookupClient.getMarc21_2("130018854342");
        //System.out.println(XML.domToString(domZ9350));
    
        
        //Optional<String> auther1 = MarcClient.getString(domApi, "100", "a");
    
        //System.out.println(auther1);
        
        Optional<String> auther2 = MarcClient.getString(domZ9350, "700", "a");
        
        System.out.println(auther2);
    
        XPathSelector xpath = XpathUtils.createXPathSelector();
    
        System.out.println(XML.domToString(xpath.selectNode(domZ9350,"//record")));
        System.out.println(xpath.selectString(domZ9350,"//record/datafield[@tag='700']/subfield[@code='a']/text()"));
    }
}
