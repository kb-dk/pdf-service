package dk.kb.pdfservice.cachingtransformerfactory;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

/**
 * Usage java -Djavax.xml.transform.TransformerFactory=dk.kb.pdfservice.cachingtransformerfactory.SingletonCachingTransformerFactory
 */
public class SingletonCachingTransformerFactory extends TransformerFactory {
    
    public static final CachingTransformerFactory INSTANCE = new CachingTransformerFactory();
    
    public static CachingTransformerFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        return getInstance().newTransformer();
    }
    
    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {
        return getInstance().newTransformer();
    }
    
    @Override
    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        return getInstance().newTemplates(source);
    }
    
    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset)
            throws TransformerConfigurationException {
        return getInstance().getAssociatedStylesheet(source, media, title, charset);
    }
    
    @Override
    public URIResolver getURIResolver() {
        return getInstance().getURIResolver();
    }
    
    @Override
    public void setURIResolver(URIResolver resolver) {
        getInstance().setURIResolver(resolver);
    }
    
    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        getInstance().setFeature(name, value);
    }
    
    @Override
    public boolean getFeature(String name) {
        return getInstance().getFeature(name);
    }
    
    @Override
    public void setAttribute(String name, Object value) {
        getInstance().setAttribute(name, value);
    }
    
    @Override
    public Object getAttribute(String name) {
        return getInstance().getAttribute(name);
    }
    
    @Override
    public ErrorListener getErrorListener() {
        return getInstance().getErrorListener();
    }
    
    @Override
    public void setErrorListener(ErrorListener listener) {
        getInstance().setErrorListener(listener);
    }
}
