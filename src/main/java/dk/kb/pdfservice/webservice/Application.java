package dk.kb.pdfservice.webservice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import dk.kb.pdfservice.api.impl.PdfServiceApiServiceImpl;


public class Application extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(
                JacksonJsonProvider.class,
                PdfServiceApiServiceImpl.class,
                ServiceExceptionMapper.class
        ));
    }


}
