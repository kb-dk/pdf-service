package dk.kb.pdfservice.footer;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

public enum FontEnum {
    TIMES_ROMAN(PDType1Font.TIMES_ROMAN),
    TIMES_BOLD(PDType1Font.TIMES_BOLD),
    TIMES_ITALIC(PDType1Font.TIMES_ITALIC),
    TIMES_BOLD_ITALIC(PDType1Font.TIMES_BOLD_ITALIC),
    HELVETICA(PDType1Font.HELVETICA),
    HELVETICA_BOLD(PDType1Font.HELVETICA_BOLD),
    HELVETICA_OBLIQUE(PDType1Font.HELVETICA_OBLIQUE),
    HELVETICA_BOLD_OBLIQUE(PDType1Font.HELVETICA_BOLD_OBLIQUE),
    COURIER(PDType1Font.COURIER),
    COURIER_BOLD(PDType1Font.COURIER_BOLD),
    COURIER_OBLIQUE(PDType1Font.COURIER_OBLIQUE),
    COURIER_BOLD_OBLIQUE(PDType1Font.COURIER_BOLD_OBLIQUE),
    SYMBOL(PDType1Font.SYMBOL),
    ZAPF_DINGBATS(PDType1Font.ZAPF_DINGBATS);
    
    private final PDType1Font font;
    
    FontEnum(PDType1Font font) {
        this.font = font;
    }
    
    public PDType1Font getFont() {
        return font;
    }
}
