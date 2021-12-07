package dk.kb.pdfservice.alma;

import dk.kb.alma.client.AlmaInventoryClient;
import dk.kb.alma.gen.bibs.Bib;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.config.ServiceConfig;

public class AlmaLookupClient {
    
    
    public static Bib getBibFromMMS(String mmsID) {
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        Bib bib = almaInventoryClient.getBib(mmsID);
        return bib;
    }
    
    
    public static Bib getBib(String actualBarcode) {
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        Item item = almaInventoryClient.getItem(actualBarcode);
        
        String mmsID = item.getBibData().getMmsId();
        Bib bib = almaInventoryClient.getBib(mmsID);
        return bib;
    }
}
