package dk.statsbiblioteket.doms.licensemodule.service.exception;

import javax.ws.rs.core.Response;


public abstract class LicenseModuleServiceException extends Exception {
    private static final long serialVersionUID = 27182818L;
    private final Response.Status responseStatus;
	
	public Response.Status getResponseStatus() {
		return responseStatus;
	}
	
	public LicenseModuleServiceException(Response.Status responseStatus)
	{
        super();
		this.responseStatus = responseStatus;
	}
    
    public LicenseModuleServiceException(String message, Response.Status responseStatus) {
        super(message);
		this.responseStatus = responseStatus;
	}
    
    public LicenseModuleServiceException(String message, Throwable cause, Response.Status responseStatus) {
        super(message, cause);
		this.responseStatus = responseStatus;
	}
    
    public LicenseModuleServiceException(Throwable cause, Response.Status responseStatus) {
        super(cause);
		this.responseStatus = responseStatus;
	}
    
}
