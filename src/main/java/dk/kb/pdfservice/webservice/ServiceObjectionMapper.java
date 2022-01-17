package dk.kb.pdfservice.webservice;

import dk.kb.pdfservice.webservice.exception.ServiceObjection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

/*
 * Catches {@link ServiceException}s and adjusts the response accordingly.
 */
@Provider
public class ServiceObjectionMapper implements ExceptionMapper<ServiceObjection> {
    
    @Override
    public Response toResponse(ServiceObjection exception) {
        
        Response.Status responseStatus = exception.getResponseStatus();
        Object entity = exception.getEntity();
        
        if (entity != null) {
            return Response.status(responseStatus)
                           .entity(entity)
                           .language(Locale.getDefault())
                           .type(exception.getMimeType())
                           .build();
        } else {
            return Response.status(responseStatus)
                           .language(Locale.getDefault())
                           .type(exception.getMimeType())
                           .build();
        }
        
    }
}
