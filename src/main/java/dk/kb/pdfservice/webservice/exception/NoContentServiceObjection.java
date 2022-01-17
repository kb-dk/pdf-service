package dk.kb.pdfservice.webservice.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * Custom web-exception class (400)
 */
public class NoContentServiceObjection extends ServiceObjection {
    
    //Constant fields for the OpenApi
    public static final String description = "NoContentServiceException";
    public static final String responseCode = "204";
    
    private static final long serialVersionUID = 27182825L;
    private static final Response.Status responseStatus = Response.Status.NO_CONTENT; // 204
    
    public NoContentServiceObjection() {
        super(responseStatus);
    }
    
    public NoContentServiceObjection(String message) {
        super(message, responseStatus);
    }
    
    public NoContentServiceObjection(String message, Throwable cause) {
        super(message, cause, responseStatus);
    }
    
    public NoContentServiceObjection(Throwable cause) {
        super(cause, responseStatus);
    }
    
}
