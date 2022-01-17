package dk.kb.pdfservice.webservice.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * Custom web-exception class (400)
 */
public class InvalidArgumentServiceObjection extends ServiceObjection {
    
    //Constant fields for the OpenApi
    public static final String description = "InvalidArgumentServiceException";
    public static final String responseCode = "400";
    
    private static final long serialVersionUID = 27182823L;
    private static final Response.Status responseStatus = Response.Status.BAD_REQUEST; // 400
    
    public InvalidArgumentServiceObjection() {
        super(responseStatus);
    }
    
    public InvalidArgumentServiceObjection(String message) {
        super(message, responseStatus);
    }
    
    public InvalidArgumentServiceObjection(String message, Throwable cause) {
        super(message, cause, responseStatus);
    }
    
    public InvalidArgumentServiceObjection(Throwable cause) {
        super(cause, responseStatus);
    }
    
    
}
