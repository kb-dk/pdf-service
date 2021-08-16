package dk.kb.pdfservice.api;

// import dk.kb.pdfservice.model.ErrorDto;

import java.io.File;

import java.net.URI;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import javax.validation.constraints.*;

/**
 * pdf-service
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform.
 *
 */
@Path("/")
@Api(value = "/", description = "")
public interface PdfServiceApi  {
    /**
     * Request a theater manuscript summary.
     *
     */
    @GET
    @Path("/getManuscript")
    @Produces({ "application/pdf" })
    @ApiOperation(value = "Request a theater manuscript summary.", tags={ "pdf-service",  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A pdf with attached page", response = File.class) })
    public javax.ws.rs.core.StreamingOutput getManuscript(@QueryParam("barcode") @NotNull  String barcode, @QueryParam("pdflink") @NotNull  String pdflink);
    /**
     * Request a Pdf file from link.
     *
     */
    @GET
    @Path("/getPdf")
    @Produces({ "application/pdf" })
    @ApiOperation(value = "Request a Pdf file from link.", tags={ "pdf-service",  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A pdf with attached page", response = File.class) })
    public javax.ws.rs.core.StreamingOutput getPdf(@QueryParam("barcode") @NotNull  String barcode, @QueryParam("pdflink2") @NotNull  String pdflink2);
    /**
     * Request a theater manuscript summary.
     *
     */
    @GET
    @Path("/getRawManuscript")
    @Produces({ "application/xml" })
    @ApiOperation(value = "Request a theater manuscript summary.", tags={ "pdf-service",  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A pdf with attached page", response = String.class) })
    public String getRawManuscript(@QueryParam("barcode") @NotNull  String barcode);
    /**
     * Ping the server to check if the server is reachable.
     *
     */
    @GET
    @Path("/ping")
    @Produces({ "text/plain", "application/json" })
    @ApiOperation(value = "Ping the server to check if the server is reachable.", tags={ "pdf-service" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 406, message = "Not Acceptable", response = ErrorDto.class),
            @ApiResponse(code = 500, message = "Internal Error", response = String.class) })
    public String ping();

    /**
     * This method just redirects gets to WEBAPP/api to the swagger UI /WEBAPP/api/api-docs?url=WEBAPP/api/openapi.yaml
     */
    @GET
    @Path("/")
    default public Response redirect(@Context MessageContext request){
        String path = request.get("org.apache.cxf.message.Message.PATH_INFO").toString();
        if (path != null && !path.endsWith("/")){
            path = path + "/";
        }
        return Response.temporaryRedirect(URI.create("api-docs?url=" + path + "openapi.yaml")).build();
    }
}

