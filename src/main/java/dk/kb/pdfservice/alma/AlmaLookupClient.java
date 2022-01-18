package dk.kb.pdfservice.alma;

import dk.kb.alma.client.AlmaInventoryClient;
import dk.kb.alma.gen.bibs.Bib;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.InternalServiceObjection;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceObjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.yaz4j.exception.ZoomException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public class AlmaLookupClient {
    
    private final static Logger log = LoggerFactory.getLogger(AlmaLookupClient.class);
    
    public static Bib getBibFromMMS(String mmsID) {
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        Bib bib = almaInventoryClient.getBib(mmsID);
        return bib;
    }
    
    
    public static Element getMarc21(String sourcePdfFile) {
        
        //TODO perhaps perform an SRU search, possible for the entire URL here
        //TODO an SRU search would be slower, but it would get the MARC21 directly rather than through a second call
    
        String actualBarcode = sourcePdfFile.split("[-._]", 2)[0];
    
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        Item item = almaInventoryClient.getItem(actualBarcode);
        
        String mmsID = item.getBibData().getMmsId();
        Bib bib = almaInventoryClient.getBib(mmsID);
        Element marc21 = bib.getAnies().get(0);
        return marc21;
        
    }
    
    
    public static Element getMarc21_2(String actualBarcode) {
        AlmaZ93Client almaZ93Client = new AlmaZ93Client(ServiceConfig.getAlmaZ9350Host(),
                                                        ServiceConfig.getAlmaZ9350Port(),
                                                        ServiceConfig.getAlmaZ9350Database());
        try {
            List<Document> result = almaZ93Client.search(actualBarcode);
            if (result.isEmpty()) {
                throw new NotFoundServiceObjection("Failed to find barcode " + actualBarcode);
            }
            log.debug("Found {} hits for {}", result.size(), actualBarcode);
            Document first = result.get(0);
            Element documentElement = first.getDocumentElement();
            return documentElement;
        } catch (ZoomException | ParserConfigurationException | IOException | SAXException e) {
            throw new InternalServiceObjection(e);
        }
    }
}
