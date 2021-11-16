package dk.kb.pdfservice.webservice;

import dk.kb.pdfservice.webservice.exception.ServiceException;


import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/*
 * Catches {@link ServiceException}s and adjusts the response accordingly.
 */
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {
    
    @Override
    public Response toResponse(ServiceException exception) {
        
        Response.Status responseStatus = exception.getResponseStatus();
        Object entity = exception.getEntity();
        
        return entity != null ?
               Response.status(responseStatus)
                       .entity(entity)
                       //TODO select mimetype more intelligently
                       .type(exception.getMimeType())
                       .build() :
               Response.status(responseStatus).build();
    }
}
