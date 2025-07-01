package dk.kb.pdfservice.alma;

import dk.kb.alma.client.sru.Query;
import dk.kb.alma.client.utils.SRUtils;
import dk.kb.alma.gen.sru.Record;
import dk.kb.alma.gen.sru.Records;
import dk.kb.alma.gen.sru.SearchRetrieveResponse;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.util.json.JSON;
import dk.kb.util.other.StringListUtils;
import dk.kb.util.xml.XML;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;


import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class MarcClientTest {
    
    @Test
    void getPdfInfo() throws IOException {
        ServiceConfig.initialize("conf/*.yaml",Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());
    
        System.out.println(JSON.toJson(MarcClient.getPdfInfo("130021854531")));
    }

    @Test
    void getPdfInfoFromSRU() throws IOException {
        ServiceConfig.initialize("conf/*.yaml",Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());

        System.out.println(JSON.toJson(MarcClient.getPdfInfoFromSRU(">https://www.kb.dk/e-mat/dod/130008805998_color.pdf")));
    }


    @Test
    void sRUSearch() throws IOException {
        ServiceConfig.initialize("conf/*.yaml",Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());
//        SearchRetrieveResponse searchRetrieveResponse = ServiceConfig.getAlmaSRUClient().search(Query.containsWords(Query.barcode, "130008805998"));


        Iterator<Element> result = ServiceConfig.getAlmaSRUClient().search(Query.containsWords(Query.barcode, "130008805998"));
        List<Element> resultList = StringListUtils.asStream(result).collect(Collectors.toList());
        Element first = resultList.get(0);

        String mmsID = SRUtils.extractMMSid(first).get();
        String xml = null;
        try {
            xml = XML.domToString(first);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        System.out.println(mmsID);

/*        System.out.println(searchRetrieveResponse.getRecords().getRecords().get(0).getRecordData().getContent().get(1).toString() );
        Records records = searchRetrieveResponse.getRecords();
        Record record = records.getRecords().get(0);

        System.out.println("test: "+record.getRecordPacking());
        System.out.println("test: "+record.getRecordPosition());
        System.out.println("test: "+record.getRecordSchema());
        System.out.println("test: "+record.getRecordData().getContent().);*/
    }

}
