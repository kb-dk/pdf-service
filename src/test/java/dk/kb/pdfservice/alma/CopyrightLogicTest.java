package dk.kb.pdfservice.alma;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

public class CopyrightLogicTest {
    
    @Test
    void parseDate1() {
    
        LocalDate date = CopyrightLogic.parseDate("Dat. 26.2.1997/lb");
    
        Assertions.assertEquals(LocalDate.of(1997,2,26),date);
    }
    
    @Test
    void parseDate2() {
        
        LocalDate date = CopyrightLogic.parseDate("kjh1961/62-1986.");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    
    @Test
    void parseDate_DD_MonthName_YYYY_1() {
        //DD_MonthName_YYYY
        LocalDate date = CopyrightLogic.parseDate("  jk 03-Maj-1961");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate_DD_MonthName_YYYY_2() {
        //DD_MonthName_YYYY
        LocalDate date = CopyrightLogic.parseDate("O3-Maj-1961");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate_DD_MonthName_YYYY_3() {
        //DD_MonthName_YYYY
        LocalDate date = CopyrightLogic.parseDate("3-Maj/1961");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    
    @Test
    void parseDate_DD_MonthName_YYYY_4() {
        //DD_MonthName_YYYY
        LocalDate date = CopyrightLogic.parseDate("3- Maj-/1961fkr");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    
    
    @Test
    void parseDate4_DD_MM_YYYY_1() {
        //DD_MM_YYYY
        
        LocalDate date = CopyrightLogic.parseDate("03-05-1961 AD");
    
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate4_DD_MM_YYYY_2() {
        //DD_MM_YYYY
        
        LocalDate date = CopyrightLogic.parseDate("03-5-1961");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate4_DD_MM_YYYY_3() {
        //DD_MM_YYYY
        
        LocalDate date = CopyrightLogic.parseDate("3-05-1961");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate4_DD_MM_YYYY_4() {
        //DD_MM_YYYY
        
        LocalDate date = CopyrightLogic.parseDate("8i 03./ -5./ -1961");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    
    @Test
    void parseDate_YYYY_MM_DD_1() {
        
        //YYYY_MM_DD
        LocalDate date = CopyrightLogic.parseDate("1961-05-03");
    
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate_YYYY_MM_DD_2() {
        
        //YYYY_MM_DD
        LocalDate date = CopyrightLogic.parseDate("1961-5-03");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate_YYYY_MM_DD_3() {
        
        //YYYY_MM_DD
        LocalDate date = CopyrightLogic.parseDate("1961-05-3");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    @Test
    void parseDate_YYYY_MM_DD_4() {
        
        //YYYY_MM_DD
        LocalDate date = CopyrightLogic.parseDate("1961./-5./-03");
        
        Assertions.assertEquals(LocalDate.of(1961,5,3),date);
    }
    
    
    @Test
    void parseDate7_YYYY_YYYY_1() {
        // YYYY_YYYY
        
        LocalDate date = CopyrightLogic.parseDate("Det kan være den kommer fra intervallet 1961-1986 eller et andet tidspunkt");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    @Test
    void parseDate7_YYYY_YYYY_2() {
        // YYYY_YYYY   [mellem 1771 og 1784].
        
        LocalDate date = CopyrightLogic.parseDate("31.1961-1986/04/21.");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    @Test
    void parseDate7_YYYY_YYYY_3() {
        // YYYY_YYYY
        
        LocalDate date = CopyrightLogic.parseDate("1961-1986");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    @Test
    void parseDate7_YYYY_1() {
        // YYYY
        
        LocalDate date = CopyrightLogic.parseDate("9I743k1961k86.");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    
    @Test
    void parseDate7_YYYY_2() {
        // YYYY
        
        LocalDate date = CopyrightLogic.parseDate("1961/005/2");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    @Test
    void parseDate7_YYYY_3() {
        // YYYY
        
        LocalDate date = CopyrightLogic.parseDate("1961/O5/O2");
        
        Assertions.assertEquals(LocalDate.of(1961,1,1),date);
    }
    
    
    
    
    @Test
    void parseDate7_YYY_1() {
        // YYYY
        
        LocalDate date = CopyrightLogic.parseDate("sdfds[181-?]22");
        
        Assertions.assertEquals(LocalDate.of(1819,1,1),date);
    }
    
    @Test
    void parseDate7_YY_1() {
        // YYYY
        
        LocalDate date = CopyrightLogic.parseDate("sdfds[18--?]22");
        
        Assertions.assertEquals(LocalDate.of(1899,1,1),date);
    }
    
    
    @Test
    void parseDate7_SA_1() {
        // YYYY
        
        LocalDate date = CopyrightLogic.parseDate("sdfd[s.a.]22");
        
        Assertions.assertEquals(LocalDate.of(LocalDate.now(ZoneId.systemDefault()).getYear(), 1, 1), date);
    }
    
    @Test
    void parseDate_mellem_YYYY_og_YYYY_1() {
        // mellem YYYY og YYYY

        LocalDate date = CopyrightLogic.parseDate("mellem 1771 og 1784");

        Assertions.assertEquals(LocalDate.of(1771, 1, 1), date);
    }


    
}
