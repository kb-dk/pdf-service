package dk.kb.pdfservice.alma;

import dk.kb.alma.gen.bibs.Bib;
import dk.kb.util.other.StringListUtils;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarcClient {
    
    
    @Nonnull
    public static PdfInfo getPdfInfo(String actualBarcode) {
        Bib bib = AlmaLookupClient.getBib(actualBarcode);
        //Portfolios portFolios = almaInventoryClient.getBibPortfolios(mmsID);
        
        Element marc21 = bib.getAnies().get(0);
        String authors = getAuthors(marc21);
        String title = getTitle(marc21);
        String alternativeTitle = getAlternativeTitle(marc21);
        String udgavebetegnelse = getUdgavebetegnelse(marc21);
        String place = getPlace(marc21);
        String size = getSize(marc21);
        
        
        final LocalDate publicationDate = CopyrightLogic.getPublicationDate(bib, marc21);
        boolean isWithinCopyright = CopyrightLogic.isWithinCopyright(publicationDate);
        DocumentType documentType = getDocumentType(marc21, isWithinCopyright);
        
        PdfInfo pdfInfo = new PdfInfo(authors,
                                      title,
                                      alternativeTitle,
                                      udgavebetegnelse,
                                      place,
                                      size,
                                      documentType,
                                      publicationDate,
                                      isWithinCopyright);
        return pdfInfo;
    }
    
    private static String getAuthors(Element marc21) {
        
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
        
        List<String> tag700ad = getStrings(marc21, "700", "a","d");
        
        return Stream.of(tag100a, tag245c, tag700ad)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
    }
    
    
    private static String getTitle(Element marc21) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?
        
        List<String> tag245ab = getStrings(marc21, "245", "a","b");
        
        return Stream.of(tag245ab)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
    }
    
    
    private static String getAlternativeTitle(Element marc21) {
        //Alternativ Titel:  hentes fra Marc21 246a -  hvis ikke noget = ingenting
        List<String> tag246a = getStrings(marc21, "246", "a");
        return Stream.of(tag246a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
        
    }
    
    private static String getUdgavebetegnelse(Element marc21) {
        //* Udgavebetegnelse:
        //  * 250a
        //  * hvis ikke noget = ingenting (ikke relevant for teatermanuskripter)
        List<String> tag250a = getStrings(marc21, "250", "a");
        return Stream.of(tag250a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining("; "));
    }
    
    private static String getPlace(Element marc21) {
        
        //  * hentes fra Marc21 260a + b + c eller 500a (hvis man kan afgrænse til *premiere*,
        //          da man ved overførslen til ALMA slog mange felter sammen til dette felt),
        //   * 710a (udfaset felt, der stadig er data i) eller
        //   * felt 96 (i 96 kan dog også være rettighedsbegrænsninger indskrevet).
        
        //Alle 260a felter
        List<String> tag260abc = getStrings(marc21, "260", "a","b","c");
        
        
        //Alle 500a felter der starter med premiere (case insensive)
        List<String> tag500a = getStrings(marc21, "500", "a")
                .stream()
                .filter(string -> string.toLowerCase(Locale.getDefault()).startsWith("premiere"))
                .collect(Collectors.toList());
        
        //Alle 710a felter
        List<String> tag710a = getStrings(marc21, "710", "a");
        
        //Alle 096a felter
        List<String> tag096a = getStrings(marc21, "096", "a");
        
        final List<String> allFields = Stream.of(tag260abc,
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
        
        //Sammensæt med ; som adskillelse, fordi jeg syntes det ser pænere ud end ,
        return String.join("; ", uniqueFields);
    }
    
    
    private static String getSize(Element marc21) {
        // Forlæggets fysiske størrelse:
        // hentes fra Marc21 300a
        List<String> tag300a = getStrings(marc21, "300", "a");
        return Stream.of(tag300a)
                     .flatMap(Collection::stream)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining(", "));
    }
    
    
    private static DocumentType getDocumentType(Element marc21, boolean isWithinCopyright) {
        
        List<String> tag997a = getStrings(marc21, "997", "a");
        
        if (tag997a.contains("DOD")) {
            return DocumentType.A;
        }
        
        if (tag997a.contains("DRA")) {
            if (isWithinCopyright) {
                return DocumentType.C;
            } else {
                return DocumentType.B;
            }
        }
        
        if (tag997a.contains("KBD")) {
            if (isWithinCopyright) {
                return DocumentType.A;
            } else {
                return DocumentType.B;
            }
        }
        
        //TODO What should DocumentType default to?
        //As A starts with the words "The work may be copyrighted" it seems to natural choice
        return DocumentType.Unknown;
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
