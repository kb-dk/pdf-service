package dk.kb.pdfservice.config;


import dk.kb.pdfservice.model.ApronType;

import java.util.Objects;

public class ApronMapping {
    
    private final String field;
    
    private final String fieldValue;
    
    private final ApronType apronWithinCopyright;
    
    private final ApronType apronOutsideCopyright;
    
    public ApronMapping(String field,
                        String fieldValue,
                        ApronType apronWithinCopyright,
                        ApronType apronOutsideCopyright) {
        this.field                 = field;
        this.fieldValue            = fieldValue;
        this.apronWithinCopyright  = apronWithinCopyright;
        this.apronOutsideCopyright = apronOutsideCopyright;
    }
    
    public String getField() {
        return field;
    }
    
    public String getFieldValue() {
        return fieldValue;
    }
    
    public ApronType getApronWithinCopyright() {
        return apronWithinCopyright;
    }
    
    public ApronType getApronOutsideCopyright() {
        return apronOutsideCopyright;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApronMapping that = (ApronMapping) o;
        return Objects.equals(getField(), that.getField())
               && Objects.equals(getFieldValue(),
                                 that.getFieldValue())
               && getApronWithinCopyright() == that.getApronWithinCopyright()
               && getApronOutsideCopyright() == that.getApronOutsideCopyright();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getField(), getFieldValue(), getApronWithinCopyright(), getApronOutsideCopyright());
    }
    
    @Override
    public String toString() {
        return "ApronMapping{" +
               "field='" + field + '\'' +
               ", fieldValue='" + fieldValue + '\'' +
               ", apronWithinCopyright=" + apronWithinCopyright +
               ", apronOutsideCopyright=" + apronOutsideCopyright +
               '}';
    }
}
