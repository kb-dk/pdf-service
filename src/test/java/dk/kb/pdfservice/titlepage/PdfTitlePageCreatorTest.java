package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.config.ServiceConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfTitlePageCreatorTest {
    
    @Test
    void enforceLimits() throws IOException {
    
        ServiceConfig.initialize("conf/*.yaml");
        List<String> textBlocks = List.of("Wygand, August.",
                                          "Dero Königl. Majestät zu Dännemarck Norwegen, &c. &c. bestalten Raths, August Wygands, Entsetzter Vortrab, oder Kurtzer Anfang des künfftigen Beweises; Dass alles in ermeltem Vortrab enthalten (1.) die reine, lautere ... Warheit bleibe; (2.) Darin der ietzo prædominirenden Parthey des Hamburgischen Rahts nicht der tausendste Theil der in ihnen ... wohnenden Bossheit, noch weniger die bey dem gemeinen Gut in Hamburg vorgehende entsetzliche Diebereyen enthalten oder vorgestellet; Und (3.) das von ermelter Rahts-Parthey darwider ... publicirte so genannte Warnungs-Edict eine verlogene, ... Büttels-feuerwürdige Schand-Charteque sey, ...",
                                          "",
                                          "",
                                          "[S.l.],; 1696",
                                          "[72] bl.");
    
        List<String> resultingLines = PdfTitlePageCreator.enforceLimits(textBlocks, ApronType.B);
        assertEquals("Dero Königl. Majestät zu Dännemarck Norwegen, &c. &c. bestalten Raths, August Wygands, Entsetzter Vortrab, oder Kurtzer Anfang des künfftigen Beweises; Dass alles in ermeltem Vortrab enthalten (1.) die reine, lautere ... Warheit bleibe; (2.) Darin der ietzo prædominirenden Parthey des Hamburgischen Rahts nicht der tausendste Theil der in ihnen ... wohnenden Bossheit, noch weniger die bey dem gemeinen Gut in Hamburg vorgehende entsetzliche Diebereyen enthalten oder vorgestellet; Und (3.) das von ermelter Rahts-Parthey darwider ...",resultingLines.get(1));
        //System.out.println(resultingLines);
    }
}
