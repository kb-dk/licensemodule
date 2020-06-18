package dk.statsbiblioteket.doms.licensemodule.service.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<LicenseModuleServiceException> {
    @Override
    public Response toResponse(LicenseModuleServiceException exception) {
    
        Response.Status responseStatus = exception.getResponseStatus();
        String message = exception.getMessage();
        
        return (message != null)
                       ? Response.status(responseStatus)
                                 .entity(message)
                                 .type("text/plain")
                                 .build()
                       : Response.status(responseStatus).build();
    }
}
