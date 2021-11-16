package dk.kb.pdfservice.api.impl;

import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;

public class MarcClient {
    
    private Element marc21;
    
    public MarcClient(Element marc21) {
        this.marc21 = marc21;
    }
    
    public Optional<String> getString(String tag, String subfield) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        
        return Optional.ofNullable(xpath.selectString(marc21,
                                                      "/record/datafield[@tag='"
                                                      + tag
                                                      + "']/subfield[@code='"
                                                      + subfield
                                                      + "']",
                                                      null));
    }
    
    public List<String> getStrings(String tag, String subfield) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        
        return xpath.selectStringList(marc21, "/record/datafield[@tag='" + tag + "']/subfield[@code='" + subfield + "']/text()");
    }
    
}
