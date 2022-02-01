package dk.kb.pdfservice.alma;

import dk.kb.alma.client.AlmaInventoryClient;
import dk.kb.alma.client.exceptions.AlmaKnownException;
import dk.kb.alma.gen.bibs.Bib;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.InternalServiceObjection;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceObjection;
import org.apache.commons.lang3.tuple.Pair;

public class AlmaLookupClient {
    
    
    public static Bib getBibFromMMS(String mmsID) {
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        Bib bib = almaInventoryClient.getBib(mmsID);
        return bib;
    }
    
    
    public static Pair<Bib,Item> getBib(String actualBarcode) throws NotFoundServiceObjection {
    
        //TODO perhaps perform an SRU search, possible for the entire URL here
        //TODO an SRU search would be slower, but it would get the MARC21 directly rather than through a second call
    
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        try {
            Item item = almaInventoryClient.getItem(actualBarcode);
    
            String mmsID = item.getBibData().getMmsId();
            Bib bib = almaInventoryClient.getBib(mmsID);
            return Pair.of(bib, item);
        } catch (AlmaKnownException e){
            if (e.getErrorCode().equals("401689")){
                throw new NotFoundServiceObjection("Failed to find barcode "+actualBarcode+" in ALMA",e);
            } else {
                throw new InternalServiceObjection("Failed when trying to retrieve barcode "+actualBarcode+" from ALMA",e);
            }
        }
    }
}
