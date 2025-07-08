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

}
