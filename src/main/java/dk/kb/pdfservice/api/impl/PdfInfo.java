package dk.kb.pdfservice.api.impl;

import java.time.LocalDate;

public class PdfInfo {
    
    private final String authors;
    private final String title;
    private final String alternativeTitle;
    private final String udgavebetegnelse;
    private final String place;
    private final String size;
    private final LocalDate publicationDate;
    private final boolean isWithinCopyright;
    
    public PdfInfo(String authors,
                   String title,
                   String alternativeTitle,
                   String udgavebetegnelse,
                   String place, String size, LocalDate publicationDate, boolean isWithinCopyright) {
        this.authors
                               = authors;
        this.title             = title;
        this.alternativeTitle  = alternativeTitle;
        this.udgavebetegnelse  = udgavebetegnelse;
        this.place             = place;
        this.size              = size;
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
    
    public String getPlace() {
        return place;
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
    
    @Override
    public String toString() {
        return "PdfInfo{" +
               "authors='" + authors + '\'' +
               ", title='" + title + '\'' +
               ", alternativeTitle='" + alternativeTitle + '\'' +
               ", udgavebetegnelse='" + udgavebetegnelse + '\'' +
               ", place='" + place + '\'' +
               ", size='" + size + '\'' +
               ", publicationDate=" + publicationDate +
               ", isWithinCopyright=" + isWithinCopyright +
               '}';
    }
}
