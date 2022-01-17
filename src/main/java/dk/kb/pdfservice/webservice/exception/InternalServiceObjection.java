package dk.kb.pdfservice.webservice.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * Custom web-exception class (500)
 */
public class InternalServiceObjection extends ServiceObjection {
    
    //Constant fields for the OpenApi
    public static final String description = "InternalServiceException";
    public static final String responseCode = "500";
    
    
    private static final long serialVersionUID = 27182820L;
    private static final Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR; //500
    
    public InternalServiceObjection() {
        super(responseStatus);
    }
    
    public InternalServiceObjection(String message) {
        super(message, responseStatus);
    }
    
    public InternalServiceObjection(String message, Throwable cause) {
        super(message, cause, responseStatus);
    }
    
    public InternalServiceObjection(Throwable cause) {
        super(cause, responseStatus);
    }
    
    
}


