package dk.kb.pdfservice.alma;

import dk.kb.pdfservice.config.ServiceConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MarcClientTest {
    
    @Test
    void getPdfInfo() throws IOException {
        ServiceConfig.initialize("conf/*.yaml");
    
        MarcClient.getPdfInfo("130021854531");
    }
}
