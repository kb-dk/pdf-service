package dk.kb.pdfservice.api.impl;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Closeable;
import java.io.IOException;

public class ClosablePair<K, V> extends Pair<K, V> implements Closeable {
    
    private final Pair<K, V> pair;
    
    
    public ClosablePair(Pair<K, V> pair) {
        this.pair = pair;
    }
    
    public static <K, V> ClosablePair<K, V> of(Pair<K, V> pair) {
        return new ClosablePair<>(pair);
    }
    
    public static <K, V> ClosablePair<K, V> of(K k, V v) {
        return new ClosablePair<>(Pair.of(k, v));
    }
    
    
    @Override
    public void close() throws IOException {
        K k = pair.getKey();
        if (k instanceof Closeable) {
            Closeable closableK = (Closeable) k;
            closableK.close();
        }
        
        V v = pair.getValue();
        if (v instanceof Closeable) {
            Closeable closableV = (Closeable) v;
            closableV.close();
        }
    }
    
    @Override
    public K getLeft() {
        return pair.getLeft();
    }
    
    @Override
    public V getRight() {
        return pair.getRight();
    }
    
    @Override
    public V setValue(V value) {
        return pair.setValue(value);
    }
}
