package dk.kb.pdfservice.alma;

import dk.kb.pdfservice.model.PdfMetadata;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfMetaDataMapper {

    Element marc21;

    public PdfMetaDataMapper(Element marc21) {
        this.marc21 = marc21;
    }

    public PdfMetadata mapToPdfMetadata() {


        PdfMetadata pdfMetadata = new PdfMetadata();
        pdfMetadata.setTitle(getTitle(marc21));

        System.out.println(toString(pdfMetadata));
        return pdfMetadata;
    }

    public static String toString(PdfMetadata pdfMetadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("PdfMetadata{");
        sb.append("title='").append(pdfMetadata.getTitle()).append('\'');
        sb.append("}");
        return sb.toString();
    }


    private static String getTitle(Element marc21) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?

        List<String> tag245ab = getMarcDataFieldRest(marc21, "245", "a");


        return  tag245ab.get(0);
    }



    protected static List<String> getMarcDataField(Element recordXml, String datafield, String subfield) {
        XPathSelector xpath = XpathUtils.createXPathSelector(new String[]{"marc", "http://www.loc.gov/MARC21/slim"});
        List<String> values = xpath.selectStringList(recordXml, "/marc:record/marc:datafield[@tag='" + datafield + "']/marc:subfield[@code='" + subfield + "']/text()");
        return values;
    }

    protected static List<String> getMarcDataFieldRest(Element recordXml, String datafield, String subfield) {

        XPathSelector xpath = XpathUtils.createXPathSelector();
        List<String> values = xpath.selectStringList(recordXml, "//*[local-name()='datafield'][@tag='" + datafield + "']/*[local-name()='subfield'][@code='" + subfield + "']/text()");
        return values;
    }

    public Element getMarc21() {
        if (marc21 == null) {
            throw new IllegalStateException("No MARC21 data available. Did you call setMarc21?");
        }
        return marc21;
    }

    public void setMarc21(Element marc21) {
        this.marc21 = marc21;
    }
}
