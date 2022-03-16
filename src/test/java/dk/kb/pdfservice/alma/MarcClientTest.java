package dk.kb.pdfservice.alma;

import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.util.json.JSON;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class MarcClientTest {
    
    @Test
    void getPdfInfo() throws IOException {
        ServiceConfig.initialize("conf/*.yaml",Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());
    
        System.out.println(JSON.toJson(MarcClient.getPdfInfo("130021854531")));
    }
}
