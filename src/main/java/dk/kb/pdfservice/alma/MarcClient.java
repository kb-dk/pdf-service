package dk.kb.pdfservice.alma;

import com.google.common.collect.Sets;
import dk.kb.alma.client.sru.Query;
import dk.kb.alma.client.utils.SRUtils;
import dk.kb.alma.gen.bibs.Bib;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.config.ApronMapping;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.model.ApronType;
import dk.kb.pdfservice.model.PdfMetadata;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceObjection;
import dk.kb.util.other.StringListUtils;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.transform.TransformerException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarcClient {
    
    public static final Logger log = LoggerFactory.getLogger(MarcClient.class);
    
    @Nonnull
    public static PdfMetadata getPdfInfo(String actualBarcode) throws NotFoundServiceObjection {
        Pair<Bib, Item> bibItem = AlmaLookupClient.getBib(actualBarcode);
        //Portfolios portFolios = almaInventoryClient.getBibPortfolios(mmsID);
        Bib bib = bibItem.getLeft();
        Item item = bibItem.getRight();
        
        Element marc21 = bib.getAnies()
                            .stream()
                            .filter(element -> Objects.equals(element.getLocalName(), "record"))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundServiceObjection("Failed to parse marc21 data for "+actualBarcode));
    
    
        RecordType recordType = getRecordType(marc21);
    
        String publicationDateString = CopyrightLogic.getPublicationDateString(bib, marc21, recordType);
        final LocalDate publicationDate = CopyrightLogic.getPublicationDate(publicationDateString);
        boolean isWithinCopyright = CopyrightLogic.isWithinCopyright(publicationDate);
        
        ApronType apronType = getApronType(marc21, isWithinCopyright);

        String authors = getAuthors(marc21, recordType);
        String title = getTitle(marc21, recordType);
        String alternativeTitle = getAlternativeTitle(marc21, recordType);
        String udgavebetegnelse = getUdgavebetegnelse(marc21, recordType);
        String placeAndYear = getPlaceAndYear(marc21, recordType);
        String size = getSize(marc21, recordType);
        String keywords = getKeywords(marc21, recordType);
        
        String volume = item.getItemData().getDescription();
        //bib.getSuppressFromExternalSearch()
        
        String primoLink = ServiceConfig.getPrimoLink(bib.getMmsId());
        
        return new PdfMetadata()
                .authors(authors)
                .title(title)
                .alternativeTitle(alternativeTitle)
                .udgavebetegnelse(udgavebetegnelse)
                .volume(volume)
                .placeAndYear(placeAndYear)
                .size(size)
                .apronType(apronType)
                .publicationDate(publicationDate)
                .publicationDateString(publicationDateString)
                .isWithinCopyright(isWithinCopyright)
                .keywords(keywords)
                .primoLink(primoLink);
        //PdfInfo pdfInfo = new PdfInfo(authors,
        //                              title,
        //                              alternativeTitle,
        //                              udgavebetegnelse,
        //                              volume,
        //                              placeAndYear,
        //                              size,
        //                              apronType,
        //                              publicationDate,
        //                              publicationDateString,
        //                              isWithinCopyright,
        //                              keywords,
        //                              primoLink);
        //return pdfInfo;
    }

    private static Query getQuery(String url) {

        String filename = url.substring(url.lastIndexOf('/') + 1);

        String barcode = filename.split("[-._]", 2)[0];

        return Query.containsWords(Query.barcode, barcode);
    }


    private static String getKeywords(Element marc21, RecordType recordType) {
    
        List<String> tag653a = getStrings(marc21, "653", "a");
        tag653a = joinIfEndingInColon(tag653a);
    
        return Stream.of(tag653a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
    }
    
    
    private static String getAuthors(Element marc21, RecordType recordType) {
        
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
        
        List<String> tag100a = getStrings(marc21, "100", "a");
        List<String> tag245c = getStrings(marc21, "245", "c");
        
        List<String> tag700ad = ifTeater(recordType, () -> getStrings(marc21, "700", "a", "d"));
        
        return Stream.of(tag100a, tag245c, tag700ad)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
    }
    
    public static <T extends List<String>> List<String> ifTeater(RecordType recordType, Supplier<T> supplier) {
        if (recordType == RecordType.Teater){
            return supplier.get();
        } else {
            return Collections.emptyList();
        }
    }
    
    
    private static String getTitle(Element marc21, RecordType recordType) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?
        
        List<String> tag245ab = getStrings(marc21, "245", "a", "b");
        tag245ab = joinIfEndingInColon(tag245ab);
        
        return Stream.of(tag245ab)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .map(string -> string.replaceAll("\\s+/\\s*$", ""))
                     .collect(Collectors.joining("; "));
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
    
    
    private static String getAlternativeTitle(Element marc21, RecordType recordType) {
        //Alternativ Titel:  hentes fra Marc21 246a -  hvis ikke noget = ingenting
        List<String> tag246a = getStrings(marc21, "246", "a");
        //Alternative titel for
        // http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130009869108.pdf
        // https://kbdk-kgl-psb.primo.exlibrisgroup.com/discovery/fulldisplay?docid=alma99122802669405763&context=U&vid=45KBDK_KGL:KGL&lang=da
        // ligger i
        // 740a
        List<String> tag740a = getStrings(marc21, "740", "a");
        return Stream.of(tag246a, tag740a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
        
    }
    
    private static String getUdgavebetegnelse(Element marc21, RecordType recordType) {
        //* Udgavebetegnelse:
        //  * 250a
        //  * hvis ikke noget = ingenting (ikke relevant for teatermanuskripter)
        List<String> tag250a = getStrings(marc21, "250", "a");
        return Stream.of(tag250a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
    }
    
    private static String getPlaceAndYear(Element marc21, RecordType recordType) {
        
        //  * hentes fra Marc21 260a + b + c eller 500a (hvis man kan afgrænse til *premiere*,
        //          da man ved overførslen til ALMA slog mange felter sammen til dette felt),
        //   * 710a (udfaset felt, der stadig er data i) eller
        //   * felt 96 (i 96 kan dog også være rettighedsbegrænsninger indskrevet).
        
        //Alle 260a felter
        List<String> tag260abc = getStrings(marc21, "260", "a", "b", "c");
    
        
        List<String> tag240a = ifTeater(recordType, () ->getStrings(marc21, "240", "a"));
    
        //Alle 500a felter der starter med premiere (case insensive)
        List<String> tag500a = ifTeater(recordType, () ->getStrings(marc21, "500", "a")
                .stream()
                .filter(string -> string.toLowerCase(Locale.getDefault()).startsWith("premiere"))
                .collect(Collectors.toList()));
        
        //Alle 710a felter
        List<String> tag710a = ifTeater(recordType, () ->getStrings(marc21, "710", "a"));
        
        //Alle 096a felter
        List<String> tag096a = ifTeater(recordType, () ->getStrings(marc21, "096", "a"));
        
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
    
    
    private static String getSize(Element marc21, RecordType recordType) {
        // Forlæggets fysiske størrelse:
        // hentes fra Marc21 300a
        List<String> tag300a = getStrings(marc21, "300", "a");
        return Stream.of(tag300a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining(", "));
    }
    
    private static RecordType getRecordType(Element marc21) {
        Set<String> theatre999aValues = new HashSet<>(ServiceConfig.getTheatreCriteria());
        Set<String> tag999a = new HashSet<>(getStrings(marc21, "999", "a"));
        if (!Sets.intersection(theatre999aValues,tag999a).isEmpty()){
            return RecordType.Teater;
        }
        return RecordType.Common;
    }
    
    
    private static ApronType getApronType(Element marc21, boolean isWithinCopyright) {
        
        Set<String> tag997a = new HashSet<>(getStrings(marc21, "997", "a"));
        Set<String> tag999a = new HashSet<>(getStrings(marc21, "999", "a"));
    
        //This is an ordered map, so you can trust the iteration order
        List<ApronMapping> mappings = ServiceConfig.getApronTypeMapping();
        Pair<ApronType,ApronType> defaultResult = Pair.of(ApronType.UNKNOWN, ApronType.UNKNOWN);
    
        Pair<ApronType,ApronType> result = null;
        outerloop:
        for (ApronMapping mapping : mappings) {
            switch (mapping.getField()) {
                case "997a":
                    if (tag997a.contains(mapping.getFieldValue())) {
                        result = Pair.of(mapping.getApronWithinCopyright(),mapping.getApronOutsideCopyright() );
                        break outerloop;
                    }
                    break;
                case "999a":
                    if (tag999a.contains(mapping.getFieldValue())) {
                        result = Pair.of(mapping.getApronWithinCopyright(),mapping.getApronOutsideCopyright() );
                        break outerloop;
                    }
                    break;
                case ServiceConfig.DEFAULT:
                    //Record the default value and continue.
                    //We will use the default value after this loop, if nothing matched before
                    defaultResult = Pair.of(mapping.getApronWithinCopyright(),mapping.getApronOutsideCopyright() );
                    continue outerloop;
                default:
                    log.warn("Found unknown keyType {} when parsing Apron Mapping config",mapping.getField());
            }
        }
        if (result == null){
            result = defaultResult;
        }
        if (isWithinCopyright){
            return result.getLeft();
        } else {
            return result.getRight();
        }
    }
    
    
    public static Optional<String> getString(Element marc21, String tag, String subfield) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        
        return Optional.ofNullable(xpath.selectString(marc21,
                                                      "/record/datafield[@tag='"
                                                      + tag
                                                      + "']/subfield[@code='"
                                                      + subfield
                                                      + "']",
                                                      null));
    }
    
    public static List<String> getStrings(Element marc21, String tag, String subfield) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        
        return xpath.selectStringList(marc21,
                                      "/record/datafield[@tag='"
                                      + tag
                                      + "']/subfield[@code='"
                                      + subfield
                                      + "']/text()");
    }
    
    public static List<String> getStrings(Element marc21, String tag, String... subfields) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        
        
        final String xpathExpr = "/record/datafield[@tag='"
                                 + tag
                                 + "']/subfield["
                                 + Arrays.stream(subfields)
                                         .map(subfield -> "@code='" + subfield + "'")
                                         .collect(Collectors.joining(" or "))
                                 + "]/text()";
        return xpath.selectStringList(marc21,
                                      xpathExpr);
    }
    
}
