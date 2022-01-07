package dk.kb.pdfservice.utils;

import java.util.function.Supplier;

public class Objections {
    
    public static  <E extends Exception,K> K object(Supplier<E> objectionSupplier) throws E {
        throw objectionSupplier.get();
        
    }
}
