package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.alma.ApronType;
import dk.kb.pdfservice.alma.PdfInfo;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.util.json.JSON;
import org.apache.fop.apps.FOPException;
import org.junit.jupiter.api.Test;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfTitlePageCreatorTest {
    
    @Test
    void checkBadCharsInMetadata() throws IOException, FOPException, TransformerException {
        
        //Also test from http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130009869108.pdf
        
        
       
        ServiceConfig.initialize("conf/*.yaml");
        String json1 = "{\n"
                            + "  \"alternativeTitle\" : \"\",\n"
                            + "  \"apronType\" : \"A\",\n"
                            + "  \"authors\" : \"Pape, Joan Carol. Chr.; Joan. Carol. Christiano Pape, Illustri et Experientissimo Dno. Balth Joh. de Buchwald.\",\n"
                            + "  \"placeAndYear\" : \"Hafnia :; Typis Directoris Sacr. Reg. Majestatis & Univ. Typogr. Joh. Georg Höpffneri,; 1754\",\n"
                            + "  \"publicationDate\" : \"1754-01-01\",\n"
                            + "  \"size\" : \"IV, 56 s.\",\n"
                            + "  \"title\" : \"Specimen inaugurale medicum de rationali aithiologia rheumatismi et arthritidis, una cum Methodica Nosologia Utriusqve affectus,..\",\n"
                            + "  \"udgavebetegnelse\" : \"\",\n"
                            + "  \"withinCopyright\" : false\n"
                            + "}\n";
        String json2 =  "{\n"
                        + "  \"alternativeTitle\" : \"Entsetzter Vortrab, oder Kurtzer Anfang des künfftigen Beweises, dasz alles in ermeltem Vortrab enthalten die reine, lautere Warheit bleibe ...\",\n"
                        + "  \"apronType\" : \"C\",\n"
                        + "  \"authors\" : \"Wygand, August.\",\n"
                        + "  \"placeAndYear\" : \"[S.l.],; 1696\",\n"
                        + "  \"publicationDate\" : \"1696-01-01\",\n"
                        + "  \"size\" : \"[72] bl.\",\n"
                        + "  \"title\" : \"Dero Königl. Majestät zu Dännemarck Norwegen, &c. &c. bestalten Raths, August Wygands, Entsetzter Vortrab, oder Kurtzer Anfang des künfftigen Beweises; Dass alles in ermeltem Vortrab enthalten (1.) die reine, lautere ... Warheit bleibe; (2.) Darin der ietzo prædominirenden Parthey des Hamburgischen Rahts nicht der tausendste Theil der in ihnen ... wohnenden Bossheit, noch weniger die bey dem gemeinen Gut in Hamburg vorgehende entsetzliche Diebereyen enthalten oder vorgestellet; Und (3.) das von ermelter Rahts-Parthey darwider ... publicirte so genannte Warnungs-Edict eine verlogene, ... Büttels-feuerwürdige Schand-Charteque sey, ...\",\n"
                        + "  \"udgavebetegnelse\" : \"\",\n"
                        + "  \"withinCopyright\" : false\n"
                        + "}";
        
        String json3 = "{\n"
                       + "  \"alternativeTitle\" : \"קרית ארבע ; חלק ראשון :מדבר בטבע הולכי ד' ובעלי חיי העצומים תבניתם טבעם ומחיתן ...מהבחור אליקים בן ... איסרל זאלדין.\",\n"
                       + "  \"apronType\" : \"B\",\n"
                       + "  \"authors\" : \"me-ha-baḥur ʾElyaqim ben ... ʾIserl Zoldin.\",\n"
                       + "  \"placeAndYear\" : \"[Kbh.] :; [Eget Forlag],; 1786-1787\",\n"
                       + "  \"publicationDate\" : \"1786-01-01\",\n"
                       + "  \"size\" : \"19 bl.\",\n"
                       + "  \"title\" : \"Qiryatʾ arbaʿ : ḥeleq riʾshon medaber be-ṭevaʿ holkhe 4 u-vaʿale ḥaye ha-ʿatsumim tavnitam ṭivʿam u-mḥitan gam yesupar bo mi-mivḥar ha-beruʾ[i]m u-me-rov godlo\",\n"
                       + "  \"udgavebetegnelse\" : \"\",\n"
                       + "  \"withinCopyright\" : false\n"
                       + "}";
        PdfInfo pdfInfo = JSON.fromJson(json1, PdfInfo.class);
        try (InputStream apronPage = PdfApronCreator.produceApronPage(pdfInfo)) {
            Files.copy(apronPage, Path.of("test.pdf"), StandardCopyOption.REPLACE_EXISTING);
        }
        //Höpffneri
    
    
        List<String> info = List.of(pdfInfo.getAuthors(), pdfInfo.getTitle(), pdfInfo.getAlternativeTitle(),
                                    pdfInfo.getUdgavebetegnelse(), pdfInfo.getPlaceAndYear(), pdfInfo.getSize());
        List<String> result = PdfApronCreator.enforceLimits(info, pdfInfo.getApronType(),
                                                            ServiceConfig.getApronMetadataTableFontSize(),
                                                            ServiceConfig.getApronMetadataTableWidthCm());
        //Check that the system does not break itself on weird chars
        assertEquals("Hafnia :; Typis Directoris Sacr. Reg. Majestatis & Univ. Typogr. Joh. Georg Höpffneri,; 1754",  result.get(4));
    }
    
    @Test
    void enforceLimits() throws IOException {

        //http://localhost:8080/pdf-service/api/getPdf/130019369456-color.pdf
        ServiceConfig.initialize("conf/*.yaml");
        List<String> textBlocks = List.of("Wygand, August.",
                                          "Dero Königl. Majestät zu Dännemarck Norwegen, &c. &c. bestalten Raths, August Wygands, Entsetzter Vortrab, oder Kurtzer Anfang des künfftigen Beweises; Dass alles in ermeltem Vortrab enthalten (1.) die reine, lautere ... Warheit bleibe; (2.) Darin der ietzo prædominirenden Parthey des Hamburgischen Rahts nicht der tausendste Theil der in ihnen ... wohnenden Bossheit, noch weniger die bey dem gemeinen Gut in Hamburg vorgehende entsetzliche Diebereyen enthalten oder vorgestellet; Und (3.) das von ermelter Rahts-Parthey darwider ... publicirte so genannte Warnungs-Edict eine verlogene, ... Büttels-feuerwürdige Schand-Charteque sey, ...",
                                          "",
                                          "",
                                          "[S.l.],; 1696",
                                          "[72] bl.");
    
        List<String> resultingLines = PdfApronCreator.enforceLimits(textBlocks, ApronType.B,
                                                                    ServiceConfig.getApronMetadataTableFontSize(),
                                                                    ServiceConfig.getApronMetadataTableWidthCm());
        assertEquals("Dero Königl. Majestät zu Dännemarck Norwegen, &c. &c. bestalten Raths, August Wygands, Entsetzter Vortrab, oder Kurtzer Anfang des künfftigen Beweises; Dass alles in ermeltem Vortrab enthalten (1.) die reine, lautere ... Warheit bleibe; (2.) Darin der ietzo prædominirenden Parthey des Hamburgischen Rahts nicht der tausendste Theil der in ihnen ... wohnenden Bossheit, noch weniger die bey dem gemeinen Gut in Hamburg vorgehende entsetzliche Diebereyen enthalten oder vorgestellet; Und (3.) das von ermelter Rahts-Parthey darwider ...",resultingLines.get(1));
        //System.out.println(resultingLines);
    }
    
    
    
}
