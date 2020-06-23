package dk.statsbiblioteket.doms.licensemodule.service;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.MonitorCache;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsAndLicensesInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsAndLicensesOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUsersLicensesInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUsersLicensesOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.LicenseOverviewDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.MonitoringOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserGroupDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.ValidateAccessInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.ValidateAccessOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.exception.InternalServiceException;
import dk.statsbiblioteket.doms.licensemodule.service.exception.InvalidArgumentServiceException;
import dk.statsbiblioteket.doms.licensemodule.service.exception.LicenseModuleServiceException;
import dk.statsbiblioteket.doms.licensemodule.validation.LicenseValidator;

//No path except the context root+servletpath for the application. Example http://localhost:8080/licensemodule/services 
//servlet path is defined in web.xml and is  /services 

@Path("/") 
public class LicenseModuleResource {
	private static final Logger log = LoggerFactory.getLogger(LicenseModuleResource.class);		                                                         
			
	@POST	
	@Path("checkAccessForIds")
	@Consumes({MediaType.TEXT_XML,MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})   
    @Produces({MediaType.TEXT_XML,MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public CheckAccessForIdsOutputDTO checkAccessForIds(CheckAccessForIdsInputDTO input)
			                        throws LicenseModuleServiceException  {        			
	    MonitorCache.registerNewRestMethodCall("checkAccessForIds");
	    log.info("checkAccessForIds called");
		//Ignore illegal presentationtype and just return empty DTO.. (popular request...)
		try{
		  ConfiguredDomLicensePresentationType presentationType = LicenseValidator.matchPresentationtype(input.getPresentationType());
		}
		catch(IllegalArgumentException e){
	        log.error("Unknown presentationtype:"+input.getPresentationType());
			CheckAccessForIdsOutputDTO output =  new CheckAccessForIdsOutputDTO();
			output.setAccessIds(new ArrayList<String>());
			output.setPresentationType(input.getPresentationType());
			output.setQuery("(NoAccess:NoAccess)"); //query that returns nothing
		    return output;
		}
		
		try {						
			CheckAccessForIdsOutputDTO output = LicenseValidator.checkAccessForIds(input);			 		
	        return output;
		} catch (Exception e) {
			log.error("Error in checkAccessForIds:",e);
		    throw handleServiceExceptions(e);
		}			
	}		
	
	@POST	
	@Path("validateAccess")	
    @Consumes({MediaType.TEXT_XML,MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})   
    @Produces({MediaType.TEXT_XML,MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ValidateAccessOutputDTO validateAccess(ValidateAccessInputDTO input)
			                        throws LicenseModuleServiceException  {        			
	    MonitorCache.registerNewRestMethodCall("validateAccess");
	    
	    log.info("validateAccess called");

		try {
 		   boolean access =  LicenseValidator.validateAccess(input);			
	      return new ValidateAccessOutputDTO(access);	    		   
		} catch (Exception e) {
			throw handleServiceExceptions(e);
		}			
	}		
					
	

	@POST	
	@Path("getUserLicenses")	
	@Consumes({MediaType.TEXT_XML,MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})	
	@Produces({MediaType.TEXT_XML,MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public GetUsersLicensesOutputDTO getUserLicenses(GetUsersLicensesInputDTO input)
			                        throws LicenseModuleServiceException  {        			
	    MonitorCache.registerNewRestMethodCall("getUserLicenses");
	    log.info("getUserLicenses called");
	
		ArrayList<LicenseOverviewDTO> list = new ArrayList<LicenseOverviewDTO>();
		GetUsersLicensesOutputDTO output = new GetUsersLicensesOutputDTO();
		output.setLicenses(list);
		try {			
			ArrayList<License> licenses = LicenseValidator.getUsersLicenses(input);
	      
			for (License current: licenses){
				LicenseOverviewDTO  item = new LicenseOverviewDTO();
				if ("en".equals(input.getLocale())){		        			        	
		        item.setName(current.getLicenseName_en());
				item.setDescription(current.getDescription_en());		       
				
				}
		        else{
		        	item.setName(current.getLicenseName());
					item.setDescription(current.getDescription_dk());
		        	
		        }
				item.setValidFrom(current.getValidFrom());
				item.setValidTo(current.getValidTo());				
			 list.add(item);
			}					
			return output;	   
		} catch (Exception e) {
			throw handleServiceExceptions(e);
		}			
	}		
	
	
	@POST	
	@Path("getUserLicenseQuery")	
	@Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})	
	@Produces({MediaType.TEXT_XML,MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public String getUserLicenseQuery(GetUserQueryInputDTO input)
			                        throws LicenseModuleServiceException  {        			
	    MonitorCache.registerNewRestMethodCall("getUserLicenseQuery");
	    log.info("getUserLicenseQuery called");
		try {
			GetUserQueryOutputDTO output = LicenseValidator.getUserQuery(input);			
	        /*
			log.info("-------------------getUserLicenseQuery----------------");
	        log.info("input (presentationtype): "+input.getPresentationType());
	        log.info("input (attributes): "+input.getAttributes());	        
			log.info("output (User license query):"+output.getQuery());
			log.info("output (groups)" + output.getUserLicenseGroups());
			*/
	      return output.getQuery();  
		} catch (Exception e) {
			throw handleServiceExceptions(e);
		}			
	}		
	
	@POST	
	@Path("getUserGroups")	
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML})  
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML})
	public GetUserGroupsOutputDTO getUserGroups(GetUserGroupsInputDTO input)
			                        throws LicenseModuleServiceException  {        			
	    MonitorCache.registerNewRestMethodCall("getUserGroups");	
     log.info("getUserGroups called");
		try {						
		    ArrayList<UserGroupDTO> groups = LicenseValidator.getUsersGroups(input);            		    		 
		    GetUserGroupsOutputDTO output = new GetUserGroupsOutputDTO();
		    output.setGroups(groups);
		     		
	      return output;				
		} catch (Exception e) {
			throw handleServiceExceptions(e);
		}			
	}		
	
		
	
	@POST			
	@Path("getUserGroupsAndLicenses")	
	@Consumes(MediaType.APPLICATION_JSON)	
	@Produces(MediaType.APPLICATION_JSON)
	public GetUserGroupsAndLicensesOutputDTO getUserGroupsAndLicensesJSON(GetUserGroupsAndLicensesInputDTO input)
            throws LicenseModuleServiceException  {        						
    
	    MonitorCache.registerNewRestMethodCall("getUserGroupsAndLicensesJSON");
	    
		GetUserGroupsInputDTO input1 = new GetUserGroupsInputDTO();
		input1.setAttributes(input.getAttributes());
		input1.setLocale(input.getLocale());
		
		GetUsersLicensesInputDTO input2 = new GetUsersLicensesInputDTO();
		input2.setAttributes(input.getAttributes());
		input2.setLocale(input.getLocale());
			
		GetUserGroupsOutputDTO userGroups = getUserGroups(input1);          
    	
		GetUsersLicensesOutputDTO userLicenses = getUserLicenses(input2);	       
        GetUserGroupsAndLicensesOutputDTO  output = new GetUserGroupsAndLicensesOutputDTO();
        output.setAllPresentationTypes(LicenseValidator.getAllPresentationtypeNames(input.getLocale()));
        output.setAllGroups(LicenseValidator.getAllGroupeNames(input.getLocale()));
        output.setGroups(userGroups.getGroups());
        output.setLicenses(userLicenses.getLicenses());
        
	   return output;
	}		
	
	@GET
    @Path("system/monitoring")
    @Produces(MediaType.TEXT_XML)
    public MonitoringOutputDTO extractStatistics() throws LicenseModuleServiceException   {
        
        MonitorCache.registerNewRestMethodCall("extractStatistics");
        MonitoringOutputDTO  output = null;
        try {
            output =   new  MonitoringOutputDTO(); //storage.extractStatistics();                   
                                        
        } catch (Exception e) {
            throw handleServiceExceptions(e);
        }
           
        return  output;
    }
	
	//This avoids have each method trying to catch 2+ exceptions with a lot of waste of code-lines
	private LicenseModuleServiceException  handleServiceExceptions(Exception e){
		if (e instanceof LicenseModuleServiceException){
			return (LicenseModuleServiceException) e;  //Do nothing,  exception already correct
		}
		else if (e instanceof IllegalArgumentException){ //Should be deleted. Code should throw InvalidArgumentServiceException instead and this would not be needed      
          return new InvalidArgumentServiceException(e.getMessage());
      }
		else {//SQL, nullpointer and other unforseen exceptions.... should not happen.			
			log.error("ServiceException(HTTP 500) in LicenseModule:",e);
			return new InternalServiceException(e.getMessage());
		}
       
	}
	
	
}