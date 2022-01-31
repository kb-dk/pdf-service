package dk.kb.pdfservice.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SizeUtilsTest {
    
    @Test
    void toBytes() {
        String GB2 = SizeUtils.toHumanReadable(2053383057);
        long bytesBack = SizeUtils.toBytes(GB2);
        assertEquals(2050846883, bytesBack);
    }
    
    @Test
    void toHumanReadable() {
        String GB2 = SizeUtils.toHumanReadable(2053383057);
        assertEquals("1.91 GB",GB2);
    }
    
    @Test
    void testConvergenge() {
        long value = 2053383057;
        
        while (true) {
            long newValue = SizeUtils.toBytes(SizeUtils.toHumanReadable(value));
            if (newValue == value){
                break;
            } else {
                value = newValue;
            }
        }
        assertEquals("1.75 GB", SizeUtils.toHumanReadable(value));
    }
}
