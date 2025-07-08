package dk.kb.pdfservice.alma;

import dk.kb.pdfservice.model.PdfMetadata;
import dk.kb.util.other.StringListUtils;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Element;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfMetaDataMapper {

    private Element marc21;
    private RecordType recordType;

    public PdfMetaDataMapper(Element marc21) {
        this.marc21 = marc21;
        setRecordType();
    }

    public PdfMetadata mapToPdfMetadata() {


        PdfMetadata pdfMetadata = new PdfMetadata();
        pdfMetadata.setTitle(getTitle(marc21));
        pdfMetadata.setAuthors(getAuthors(marc21));
        pdfMetadata.setAlternativeTitle(getAlternativeTitle(marc21));
        pdfMetadata.setUdgavebetegnelse(getUdgavebetegnelse(marc21));
        pdfMetadata.setPlaceAndYear(getPlaceAndYear(marc21));

        System.out.println(toString(pdfMetadata));
        return pdfMetadata;
    }

    public static String toString(PdfMetadata pdfMetadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("PdfMetadata{");
        sb.append("authors='").append(pdfMetadata.getAuthors()).append('\'');
        sb.append(", title='").append(pdfMetadata.getTitle()).append('\'');
        sb.append(", alternativeTitle='").append(pdfMetadata.getAlternativeTitle()).append('\'');
        sb.append(", udgavebetegnelse='").append(pdfMetadata.getUdgavebetegnelse()).append('\'');
        sb.append(", placeAndYear=").append(pdfMetadata.getPlaceAndYear()).append('\'');
        sb.append(", volume=").append(pdfMetadata.getVolume()).append('\'');
        sb.append(", publicationDate=").append(pdfMetadata.getPublicationDate()).append('\'');
        sb.append(", publicationDateString='").append(pdfMetadata.getPublicationDateString()).append('\'');

        sb.append("}");
        return sb.toString();
    }

    private static String getAuthors(Element marc21) {

        //https://sbprojects.statsbiblioteket.dk/pages/viewpage.action?pageId=103877561

        /*
        Forfatter(e):
            hentes fra Marc21 100 a,
            245 c  komma separeret
            og 700 a + d ( kan forekomme flere gange),?
            og opdelt i flere linier hvis længere end ?-
            [ ] - hvis ingen værdi(er).

            Her kan forekomme dublet oplysninger p.g.a. registrerings praksis gennem tiderne
         */

        List<String> tag100a = getMarcDataField(marc21, "100", "a");
        if (! tag100a.isEmpty()) {
            return tag100a.get(0);
        }
        List<String> tag245c = getMarcDataField(marc21, "245", "c");
        if (! tag100a.isEmpty()) {
            return tag100a.get(0);
        }
        List<String> tag700ad = getMarcDataField(marc21, "700", "a");
        tag700ad.addAll(getMarcDataField(marc21, "700", "b"));

        return Stream.of(tag700ad)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
    }

    private static String getTitle(Element marc21) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?

        List<String> tag245ab = getMarcDataField(marc21, "245", "a");
        tag245ab.addAll(getMarcDataField(marc21, "245", "b"));

        tag245ab = joinIfEndingInColon(tag245ab);

        return Stream.of(tag245ab)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(string -> string.replaceAll("\\s+/\\s*$", ""))
                .collect(Collectors.joining("; "));
    }

    private static String getAlternativeTitle(Element marc21) {
        //Alternativ Titel:  hentes fra Marc21 246a -  hvis ikke noget = ingenting
        List<String> tag246a = getMarcDataField(marc21, "246", "a");
        //Alternative titel for
        // http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130009869108.pdf
        // https://kbdk-kgl-psb.primo.exlibrisgroup.com/discovery/fulldisplay?docid=alma99122802669405763&context=U&vid=45KBDK_KGL:KGL&lang=da
        // ligger i
        // 740a
        List<String> tag740a = getMarcDataField(marc21, "740", "a");
        return Stream.of(tag246a, tag740a)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));

    }

    private static String getUdgavebetegnelse(Element marc21) {
        //* Udgavebetegnelse:
        //  * 250a
        //  * hvis ikke noget = ingenting (ikke relevant for teatermanuskripter)
        List<String> tag250a = getMarcDataField(marc21, "250", "a");
        return Stream.of(tag250a)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
    }

    private String getPlaceAndYear(Element marc21) {

        //  * hentes fra Marc21 260a + b + c eller 500a (hvis man kan afgrænse til *premiere*,
        //          da man ved overførslen til ALMA slog mange felter sammen til dette felt),
        //   * 710a (udfaset felt, der stadig er data i) eller
        //   * felt 96 (i 96 kan dog også være rettighedsbegrænsninger indskrevet).

        //Alle 260a felter
        List<String> tag260abc = getMarcDataField(marc21, "260", "a");
        tag260abc.addAll(getMarcDataField(marc21, "260", "b"));
        tag260abc.addAll(getMarcDataField(marc21, "260", "c"));

        List<String> tag240a = ifTeater(recordType, () ->getMarcDataField(marc21, "240", "a"));

        //Alle 500a felter der starter med premiere (case insensive)
        List<String> tag500a = getMarcDataField(marc21, "500", "a");

//                ifTeater(recordType, () ->getMarcDataField(marc21, "500", "a")
//                .stream()
//                .filter(string -> string.toLowerCase(Locale.getDefault()).startsWith("premiere"))
//                .collect(Collectors.toList()));

        //Alle 710a felter
        List<String> tag710a = ifTeater(recordType, () ->getMarcDataField(marc21, "710", "a"));

        //Alle 096a felter
        List<String> tag096a = ifTeater(recordType, () ->getMarcDataField(marc21, "096", "a"));

        final List<String> allFields = Stream.of(tag260abc,
                        tag240a,
                        tag500a,
                        tag710a,
                        tag096a)
                //Producer en lang liste af ALLE de ovennævnte felter
                .flatMap(Collection::stream)
                //Fjerner tomme felter
                .filter(Objects::nonNull)
                //Fjerner afsluttende ., fordi de ser dumme ud
                .map(string -> string
                        .replaceFirst("\\.$", "")
                        .trim())
                //Samler resultatet som en liste
                .collect(Collectors.toList());

        //Magi der fjerner tekster, der er indeholdt i andre tekster. F.eks har post 130022785800-color.pdf
        // "Premiere 12.04.2018 på Folketeatret, Snoreloftet" og "Folketeatret"
        //Fordi "Folketeatret" allerede står i den længere tekst, bliver den fjernet fra listen
        final List<String> uniqueFields = StringListUtils.removeSubstrings(allFields);

        //Addendum til ovenstående eksempel: Der stod faktisk "Folketeatret.", og det indgår IKKE
        // i den længere tekst. Det er en anden grund til at jeg fjerner afsluttende .

        //Sammensæt med " " som adskillelse
        return String.join(" ", uniqueFields);
    }

    /**
     * If an element ends with :, join it with the next element
     * <p>
     * This is to ensure that we do not join them with ; later on, producing
     * some :; thing, which looks stupid
     *
     * @param stringList the list of strings
     * @return A list of strings, with less than or equal number of elements in stringList
     */
    private static List<String> joinIfEndingInColon(List<String> stringList) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < stringList.size(); i++) {
            String element = stringList.get(i).trim();
            if (i < stringList.size() - 1) { //not last element
                if (element.endsWith(":")) {
                    //Increment i so we skip this element in the next iteration
                    String nextElement = stringList.get(++i).trim();

                    //Join with next element
                    result.add(element + " " + nextElement);
                } else {
                    result.add(element);
                }
            } else {
                result.add(element);
            }
        }
        return result;
    }

    public <T extends List<String>> List<String> ifTeater(RecordType recordType, Supplier<T> supplier) {
        if (recordType == RecordType.Teater){
            return supplier.get();
        } else {
            return Collections.emptyList();
        }
    }


    public void setRecordType() {
        List<String> tag999a = getMarcDataField(marc21, "999", "a");
        if (tag999a.contains("Teatermanus")) {
            recordType = RecordType.Teater;
        }  else {
            recordType = RecordType.Common;
        }
    }


    protected static List<String> getMarcDataField(Element recordXml, String datafield, String subfield) {

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
