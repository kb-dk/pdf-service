package dk.kb.pdfservice.alma;

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
    private final ApronType documentType;
    
    
    public PdfInfo(String authors,
                   String title,
                   String alternativeTitle,
                   String udgavebetegnelse,
                   String placeAndYear,
                   String size,
                   ApronType documentType,
                   LocalDate publicationDate,
                   boolean isWithinCopyright) {
        this.authors           = authors;
        this.title             = title;
        this.alternativeTitle  = alternativeTitle;
        this.udgavebetegnelse = udgavebetegnelse;
        this.placeAndYear     = placeAndYear;
        this.size             = size;
        this.documentType      = documentType;
        this.publicationDate   = publicationDate;
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
        return documentType;
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
               ", documentType=" + documentType +
               '}';
    }
}
