package dk.kb.pdfservice.webservice.exception;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * Custom web-exception class (404)
 */
public class NotFoundServiceObjection extends ServiceObjection {
    
    //Constant fields for the OpenApi
    public static final String description = "NotFoundServiceException";
    public static final String responseCode = "404";
    
    private static final long serialVersionUID = 27182821L;
    private static final Response.Status responseStatus = Response.Status.NOT_FOUND; //404
    
    public NotFoundServiceObjection() {
        super(responseStatus);
    }
    
    public NotFoundServiceObjection(String message) {
        super(message, responseStatus);
    }
    
    public NotFoundServiceObjection(String message, Throwable cause) {
        super(message, cause, responseStatus);
    }
    
    public NotFoundServiceObjection(Throwable cause) {
        super(cause, responseStatus);
    }
    
}

