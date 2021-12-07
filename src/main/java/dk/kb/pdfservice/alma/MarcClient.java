package dk.kb.pdfservice.alma;

import dk.kb.alma.gen.bibs.Bib;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.List;
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
        PdfInfo pdfInfo = new PdfInfo(authors,
                                      title,
                                      alternativeTitle,
                                      udgavebetegnelse,
                                      place,
                                      size,
                                      publicationDate,
                                      isWithinCopyright);
        return pdfInfo;
    }
    
    private static String getSize(Element marc21) {
        // Forlæggets fysiske størrelse:
        // hentes fra Marc21 300a
        return getString(marc21, "300", "a").orElse("");
    }
    
    private static String getPlace(Element marc21) {
        //  * hentes fra Marc21 260a + b + c eller 500a (hvis man kan afgrænse til *premiere*, da man ved overførslen til ALMA slog mange felter sammen til dette felt),
        //  * 710 (udfaset felt, der stadig er data i) eller
        //  * felt 96 (i 96 kan dog også være rettighedsbegrænsninger indskrevet).
        
        String tag260a = getString(marc21, "260", "a").orElse(null);
        if (tag260a != null) {
            String tag260b = getString(marc21, "260", "b").orElse(null);
            String tag260c = getString(marc21, "260", "c").orElse(null);
            return Stream.of(tag260a, tag260b, tag260c).filter(Objects::nonNull).collect(Collectors.joining(" "));
        } else {
            Optional<String> place = getStrings(marc21, "500", "a")
                    .stream()
                    .filter(str -> str.startsWith("Premiere ")).findFirst();
            
            return place.orElse("No place specified");
        }
    }
    
    private static String getUdgavebetegnelse(Element marc21) {
        //* Udgavebetegnelse:
        //  * 250a
        //  * hvis ikke noget = ingenting (ikke relevant for teatermanuskripter)
        String tag250a = getString(marc21, "250", "a").orElse(null);
        return Stream.of(tag250a).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    private static String getAlternativeTitle(Element marc21) {
        return getString(marc21, "246", "a").orElse("");
    }
    
    private static String getTitle(Element marc21) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?
        
        String tag245a = getString(marc21, "245", "a").orElse(null);
        String tag245b = getString(marc21, "245", "b").orElse(null);
        return Stream.of(tag245a, tag245b).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    private static String getAuthors(Element marc21) {
        //* Forfatter(e):
        //  * hentes fra Marc21 100a,
        //  * 700a + d ( kan forekomme flere gange),
        //  * 245c - komma separeret og opdelt i flere linier hvis længere end ?
        //  * [] - hvis ingen værdi(er).
        String tag100a = getString(marc21, "100", "a").orElse(null);
        String tag700a = getString(marc21, "700", "a").orElse(null);
        String tag245c = getString(marc21, "245", "c").orElse(null);
        if (tag100a == null && tag700a == null && tag245c == null) {
            return "[]";
        }
        return Stream.of(tag100a, tag700a, tag245c)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining(", "));
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
    
}
