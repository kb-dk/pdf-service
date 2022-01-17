package dk.kb.pdfservice.webservice.exception;

import dk.kb.pdfservice.config.ServiceConfig;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

/*
 * Superclass for Exceptions that has a specific HTTP response code.
 * </p><p>
 * Note that this class has 2 "modes": Plain text message or custom response object,
 * intended for use with OpenAPI-generated Dto response objects.
 */
public class ServiceObjection extends WebApplicationException {
    private static final long serialVersionUID = 27182819L;
    private final Response.Status responseStatus;
    
    private MediaType mimeType = MediaType.TEXT_PLAIN_TYPE.withCharset(StandardCharsets.UTF_8.name());
    private Object entity = null;
    
    public ServiceObjection(Response.Status responseStatus) {
        super();
        this.responseStatus = responseStatus;
    }
    
    public ServiceObjection(String message, Response.Status responseStatus) {
        super(message);
        this.responseStatus = responseStatus;
    }
    
    public ServiceObjection(String message, Throwable cause, Response.Status responseStatus) {
        super(message, cause);
        this.responseStatus = responseStatus;
    }
    
    public ServiceObjection(Throwable cause, Response.Status responseStatus) {
        super(cause);
        this.responseStatus = responseStatus;
    }
    
    /**
     * Custom message object.
     *
     * @param mimeType       the MIME type used for the HTTP response headers.
     * @param entity         the entity to translate into the HTTP response body (normally an OpenAPI generated Dto).
     * @param responseStatus HTTP response code.
     */
    public ServiceObjection(MediaType mimeType, Object entity, Response.Status responseStatus) {
        super();
        this.responseStatus = responseStatus;
        this.mimeType       = mimeType;
        this.entity         = entity;
    }
    
    /**
     * Custom message object.
     *
     * @param mimeType       the MIME type used for the HTTP response headers.
     * @param entity         the entity to translate into the HTTP response body (normally an OpenAPI generated Dto).
     * @param cause          the originating Exception.
     * @param responseStatus HTTP response code.
     */
    public ServiceObjection(MediaType mimeType, Object entity, Throwable cause, Response.Status responseStatus) {
        super(cause);
        this.responseStatus = responseStatus;
        this.mimeType       = mimeType;
        this.entity         = entity;
    }
    
    public Response.Status getResponseStatus() {
        return responseStatus;
    }
    
    public MediaType getMimeType() {
        return mimeType;
    }
    
    public Object getEntity() {
        return entity == null ? getMessage() : entity;
    }
    
    @Override
    public String getMessage() {
        return super.getMessage()+"\n" + ServiceConfig.getErrorMessage();
    }
}
