package dk.kb.pdfservice.alma;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CopyrightLogicTest {
    
    @Test
    void parseDate() {
    
        LocalDate date = CopyrightLogic.parseDate("Dat. 26.2.1997/lb");
    
        Assertions.assertEquals(LocalDate.of(1997,2,26),date);
    }
}
