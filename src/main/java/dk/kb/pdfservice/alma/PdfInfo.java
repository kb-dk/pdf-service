package dk.kb.pdfservice.alma;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Data transfer class from MarcClient
 *
 * @see MarcClient
 */
public class PdfInfo {
    
    private final String authors;
    private final String title;
    private final String alternativeTitle;
    private final String udgavebetegnelse;
    private final String volume;
    private final String placeAndYear;
    private final String size;
    private final LocalDate publicationDate;
    private final String publicationDateString;
    private final boolean isWithinCopyright;
    private final ApronType apronType;
    private final String keywords;
    
    
    public PdfInfo(
            @JsonProperty("authors") String authors,
            @JsonProperty("title") String title,
            @JsonProperty("alternativeTitle") String alternativeTitle,
            @JsonProperty("udgavebetegnelse") String udgavebetegnelse,
            @JsonProperty("volume") String volume,
            @JsonProperty("placeAndYear") String placeAndYear,
            @JsonProperty("size") String size,
            @JsonProperty("apronType") ApronType apronType,
            @JsonProperty("publicationDate") LocalDate publicationDate,
            @JsonProperty("publicationDateString") String publicationDateString,
            @JsonProperty("isWithinCopyright") boolean isWithinCopyright,
            @JsonProperty("subjects")String keywords) {
        this.authors               = authors;
        this.title                 = title;
        this.alternativeTitle      = alternativeTitle;
        this.udgavebetegnelse      = udgavebetegnelse;
        this.volume                = volume;
        this.placeAndYear          = placeAndYear;
        this.size                  = size;
        this.apronType             = apronType;
        this.publicationDate       = publicationDate;
        this.publicationDateString = publicationDateString;
        this.isWithinCopyright     = isWithinCopyright;
        this.keywords              = keywords;
        
    }
    
    public String getAuthors() {
        return authors;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAlternativeTitle() {
        return alternativeTitle;
    }
    
    public String getUdgavebetegnelse() {
        return udgavebetegnelse;
    }
    public String getVolume() {
        return volume;
    }
    
    public String getPlaceAndYear() {
        return placeAndYear;
    }
    
    public String getSize() {
        return size;
    }
    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    
    public String getPublicationDateString() {
        return publicationDateString;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public boolean isWithinCopyright() {
        return isWithinCopyright;
    }
    
    public ApronType getApronType() {
        return apronType;
    }
    
    @Override
    public String toString() {
        return "PdfInfo{" +
               "authors='" + authors + '\'' +
               ", title='" + title + '\'' +
               ", alternativeTitle='" + alternativeTitle + '\'' +
               ", udgavebetegnelse='" + udgavebetegnelse + '\'' +
               ", volume='" + volume + '\'' +
               ", placeAndYear='" + placeAndYear + '\'' +
               ", size='" + size + '\'' +
               ", publicationDate=" + publicationDate +
               ", publicationDateString='" + publicationDateString + '\'' +
               ", isWithinCopyright=" + isWithinCopyright +
               ", apronType=" + apronType +
               ", keywords='" + keywords + '\'' +
               '}';
    }
}
