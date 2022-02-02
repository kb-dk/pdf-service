package dk.kb.pdfservice.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dk.kb.pdfservice.api.impl.PdfServiceApiServiceImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Application extends javax.ws.rs.core.Application {
    @Override
    public Set<Object> getSingletons() {
        
        return Set.of(getJsonProviderWithDateTimes());
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(
                PdfServiceApiServiceImpl.class,
                ServiceObjectionMapper.class
                                          ));
    }
    
    
    public static JacksonJaxbJsonProvider getJsonProviderWithDateTimes() {
        // see https://github.com/FasterXML/jackson-modules-java8
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.findAndRegisterModules(); //we add the module explicitly instead
    
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
    
        //final JavaTimeModule javaTimeModule = new JavaTimeModule();
        //mapper.registerModule(javaTimeModule);
        return new JacksonJaxbJsonProvider(mapper,
                                           JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }
    
}
