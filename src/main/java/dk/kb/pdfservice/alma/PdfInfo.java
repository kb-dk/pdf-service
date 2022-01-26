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
    private final String placeAndYear;
    private final String size;
    private final LocalDate publicationDate;
    private final boolean isWithinCopyright;
    private final ApronType apronType;
    
    
    public PdfInfo(
            @JsonProperty("authors")String authors,
            @JsonProperty("title")String title,
            @JsonProperty("alternativeTitle")String alternativeTitle,
            @JsonProperty("udgavebetegnelse")String udgavebetegnelse,
            @JsonProperty("placeAndYear")String placeAndYear,
            @JsonProperty("size")String size,
            @JsonProperty("apronType")ApronType apronType,
            @JsonProperty("publicationDate")LocalDate publicationDate,
            @JsonProperty("isWithinCopyright")boolean isWithinCopyright) {
        this.authors           = authors;
        this.title             = title;
        this.alternativeTitle  = alternativeTitle;
        this.udgavebetegnelse = udgavebetegnelse;
        this.placeAndYear     = placeAndYear;
        this.size            = size;
        this.apronType       = apronType;
        this.publicationDate = publicationDate;
        this.isWithinCopyright = isWithinCopyright;
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
    
    public String getPlaceAndYear() {
        return placeAndYear;
    }
    
    public String getSize() {
        return size;
    }
    
    public LocalDate getPublicationDate() {
        return publicationDate;
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
               ", place='" + placeAndYear + '\'' +
               ", size='" + size + '\'' +
               ", publicationDate=" + publicationDate +
               ", isWithinCopyright=" + isWithinCopyright +
               ", documentType=" + apronType +
               '}';
    }
    
    
}
