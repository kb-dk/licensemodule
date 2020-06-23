package dk.statsbiblioteket.doms.licensemodule.integrationtest;

import java.util.ArrayList;

import javax.ws.rs.core.MediaType;

import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsAndLicensesInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUsersLicensesInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUsersLicensesOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserObjAttributeDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.ValidateAccessInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.ValidateAccessOutputDTO;
import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;



//This class is an integration class run manuel, change service URL to a devel service.
// Use to reproduce and find production bugs etc.
public class LicenseModuleRestWSTester {

  
  private static String serviceUrl = "http://localhost:9612/licensemodule/services";
   
  
  
	public static void main(String[] args) throws Exception {
		
		 //testValidateAccess();
		 //testGetUserLicenseQuery();
   	     //testCheckAccessForIds();
         //testGetUsersLicenses();
 		  //testGetUsersGroups();
		
		}
	
	private static WebClient getWebClient() {
	      ArrayList<Object> providers = new ArrayList<Object>();
	        providers.add( new JacksonJaxbJsonProvider() );
	        WebClient client = WebClient.create(serviceUrl,providers);
	        return client;
	}

	@SuppressWarnings("all")
	private static void testValidateAccess() throws Exception {
		// Test Validate Access
		ValidateAccessInputDTO input = new ValidateAccessInputDTO();

		ArrayList<UserObjAttributeDTO> userObjAttributes = createTestUserObjAttributeDTO();
		input.setAttributes(userObjAttributes);

		ArrayList<String> groups = new ArrayList<String>();
		groups.add("PublicDomain");		
		input.setGroups(groups);
		input.setPresentationType("Download");
				
	    WebClient client = getWebClient();
			    	   
	    ValidateAccessOutputDTO post = client.path("validateAccess").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(input,ValidateAccessOutputDTO.class);	    
	    System.out.println(post.isAccess()); //true of false
	}

	
	
	@SuppressWarnings("all")
	private static void testGetUserLicenseQuery() throws Exception {
		GetUserQueryInputDTO input = new GetUserQueryInputDTO();
		input.setPresentationType("Search");
		input.setAttributes(createTestUserObjAttributeDTO());				
		 WebClient client = getWebClient();
		String output = client.path("getUserLicenseQuery").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(input,String.class);

		//This query is use to filter results.
		System.out.println("query:" + output);
	}

	
	@SuppressWarnings("all")
	private static void testCheckAccessForIds() throws Exception {
		CheckAccessForIdsInputDTO input = new CheckAccessForIdsInputDTO();
		input.setPresentationType("Search");
		input.setAttributes(createTestUserObjAttributeDTO());
        ArrayList<String> ids = new ArrayList<String>();        
        ids.add("doms_radioTVCollection:uuid:371157ee-b120-4504-bfaf-364c15a4137c");//radio TV        
        ids.add("doms_radioTVCollection:uuid:c3386ed5-9b79-47a2-a648-8de53569e630");//radio TV
		ids.add("doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec"); //reklame
		ids.add("doms_newspaperCollection:uuid:18709dea-802c-4bd7-98e6-32ca3b285774-segment_6"); //aviser		
		input.setIds(ids);
				
	  WebClient client = getWebClient();
			
		CheckAccessForIdsOutputDTO output = client.path("checkAccessForIds").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).post(input,CheckAccessForIdsOutputDTO.class);
		System.out.println("query:" + output.getQuery());
		System.out.println("presentationtype:" + output.getPresentationType());
		System.out.println("number of IDs:" + output.getAccessIds().size());	
	}

	
	@SuppressWarnings("all")
	private static void testGetUsersLicenses() throws Exception {
		// GetUserLicensesOutputDTO getUserLicenses

		GetUsersLicensesInputDTO input = new GetUsersLicensesInputDTO();
		input.setAttributes(createTestUserObjAttributeDTO());
        input.setLocale("da");
		        
        WebClient client = getWebClient();
		GetUsersLicensesOutputDTO output = client.path("getUserLicenses").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).post(input,GetUsersLicensesOutputDTO.class);
					
		System.out.println("output, licensenames:" + output.getLicenses());
	}
	
	

	
	
	@SuppressWarnings("all")
	private static void testGetUsersGroups() throws Exception {
		GetUserGroupsInputDTO input = new GetUserGroupsInputDTO();
		input.setAttributes(createTestUserObjAttributeDTO());
        input.setLocale("da");        
        WebClient client = getWebClient();
		GetUserGroupsOutputDTO output = client.path("getUserGroups").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(input,GetUserGroupsOutputDTO.class);	
		System.out.println("output, groups:" + output.getGroups());
	}

	

	
	private static ArrayList<UserObjAttributeDTO> createTestUserObjAttributeDTO() {
		ArrayList<UserObjAttributeDTO> userObjAttributes = new ArrayList<UserObjAttributeDTO>();
	
		UserObjAttributeDTO newUserObjAtt = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt);
		newUserObjAtt.setAttribute("attribut_store.MediestreamFullAccess");
		ArrayList<String> values = new ArrayList<String>();
		values.add("true");
		values.add("yes");
		newUserObjAtt.setValues(values);
		
		
		UserObjAttributeDTO newUserObjAtt1 = new UserObjAttributeDTO();
        userObjAttributes.add(newUserObjAtt1);
        newUserObjAtt1.setAttribute("mail");
        ArrayList<String> values1 = new ArrayList<String>();
        values1.add("mvk@statsbiblioteket.dk");
        newUserObjAtt1.setValues(values1);
		        
        UserObjAttributeDTO newUserObjAtt2 = new UserObjAttributeDTO();
        userObjAttributes.add(newUserObjAtt2);
        newUserObjAtt2.setAttribute("SBIPRoleMapper");
        ArrayList<String> values2 = new ArrayList<String>();
        values2.add("aucampus");
        newUserObjAtt2.setValues(values2);
        
        
        return userObjAttributes;
		
	}
}
