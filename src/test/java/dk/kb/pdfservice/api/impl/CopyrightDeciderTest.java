package dk.kb.pdfservice.api.impl;

import dk.kb.pdfservice.alma.CopyrightLogic;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CopyrightDeciderTest {
    
    @Test
    void parseDateNamed1() {
        LocalDate date1 = CopyrightLogic.parseDate("17. maj 2012");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateNamed2() {
        LocalDate date1 = CopyrightLogic.parseDate("17. jun 2012");
        assertEquals("2012-06-17",date1.toString());
    }
    
    @Test
    void parseDateNamed3() {
        LocalDate date1 = CopyrightLogic.parseDate("17. JUNE 2012");
        assertEquals("2012-06-17",date1.toString());
    }
    
    @Test
    void parseDateNamed4() {
        LocalDate date1 = CopyrightLogic.parseDate("17. juni 2012");
        assertEquals("2012-06-17",date1.toString());
    }
    
    @Test
    void parseDateNamed5() {
        LocalDate date1 = CopyrightLogic.parseDate("17 maj 2012");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateD4_D4() {
        LocalDate date1 = CopyrightLogic.parseDate("1748-1765");
        assertEquals("1748-01-01",date1.toString());
    }
    
    @Test
    void parseDateIso1() {
        LocalDate date1 = CopyrightLogic.parseDate("2012/5/17");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateIso2() {
        LocalDate date1 = CopyrightLogic.parseDate("2012-5-17");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateIso3() {
        LocalDate date1 = CopyrightLogic.parseDate("2012.5.17");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateIso4() {
        LocalDate date1 = CopyrightLogic.parseDate("2012-05-7");
        assertEquals("2012-05-07",date1.toString());
    }
    
    @Test
    void parseDateCommon1() {
        LocalDate date1 = CopyrightLogic.parseDate("17 05 2012");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateCommon2() {
        LocalDate date1 = CopyrightLogic.parseDate("17-05-2012");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateCommon3() {
        LocalDate date1 = CopyrightLogic.parseDate("17.5.2012");
        assertEquals("2012-05-17",date1.toString());
    }
    
    @Test
    void parseDateCommon4() {
        LocalDate date1 = CopyrightLogic.parseDate("7/05/2012");
        assertEquals("2012-05-07",date1.toString());
    }
    
    @Test
    void parseDateCommon5() {
        LocalDate date1 = CopyrightLogic.parseDate("7 /05 /2012");
        assertEquals("2012-05-07",date1.toString());
    }
    
    @Test
    void parseDateD4() {
        LocalDate date1 = CopyrightLogic.parseDate("2012");
        assertEquals("2012-01-01",date1.toString());
    }
    
}
