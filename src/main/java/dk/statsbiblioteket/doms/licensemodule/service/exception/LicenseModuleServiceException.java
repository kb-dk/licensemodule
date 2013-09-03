package dk.statsbiblioteket.doms.licensemodule.service.exception;

public abstract class LicenseModuleServiceException extends Exception{

	private static final long serialVersionUID = 1L;

	public  LicenseModuleServiceException () {
	        super();
	    }

	    public LicenseModuleServiceException (String message) {
	        super(message);
	    }

	    public LicenseModuleServiceException (String message, Throwable cause) {
	        super(message, cause);
	    }

	    public LicenseModuleServiceException (Throwable cause) {
	        super(cause);
	    }
	
}
