package dk.statsbiblioteket.doms.licensemodule.integrationtest;

import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

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

//This class is an integration test and require a local webserver to be running with licesemodule.war installed.
//Used on database with testdata from unittest
public class LicenseModuleRestWSTester {

	public static void main(String[] args) throws Exception {
		
		// testValidateAccess();
		// testGetUserLicenseQuery();
		//testGetUsersLicenses();
		//testGetUsersLicensesJSON();
		//testGetUsersGroups();
		testCheckAccessForIds();
		//testCheckAccessForIdsJSON();
	//	testGetUsersGroupsJSON();
		//testGetUsersGroupsAndLicensesJSON();
		}
	

	@SuppressWarnings("all")
	private static void testValidateAccess() throws Exception {
		// Test Validate Access
		ValidateAccessInputDTO input = new ValidateAccessInputDTO();

		ArrayList<UserObjAttributeDTO> userObjAttributes = createUserObjAttributeDTO();
		input.setAttributes(userObjAttributes);

		ArrayList<String> groups = new ArrayList<String>();
		groups.add("IndividueltForbud");
		groups.add("Klausuleret");
		input.setGroups(groups);
		input.setPresentationType("images");

		JAXBContext context = JAXBContext.newInstance(ValidateAccessInputDTO.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(input, outputStream);

		// serialize to XML
		String inputXML = outputStream.toString();
		System.out.println("input xml:\n" + inputXML);

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(UriBuilder.fromUri("http://localhost:8080/licensemodule/services/").build());

		// Call with XML
		ValidateAccessOutputDTO output = service.path("validateAccess").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(inputXML).post(ValidateAccessOutputDTO.class);

		// Call with @XmlRootElement
		// output = service.path("validateAccess").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(input).post(ValidateAccessOutputDTO.class);

		
		context = JAXBContext.newInstance(ValidateAccessOutputDTO.class); 
		outputStream = new ByteArrayOutputStream();
		 m = context.createMarshaller();
		 
		 m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 	m.marshal(output, outputStream);

		// serialize to XML
		String outputXML = outputStream.toString();
		System.out.println(outputXML);

		
		// Access depends on the licenses in the DB.
		System.out.println("access :" + output.isAccess());
	}

	@SuppressWarnings("all")
	private static void testGetUserLicenseQuery() throws Exception {
		GetUserQueryInputDTO input = new GetUserQueryInputDTO();
		input.setPresentationType("images");
		input.setAttributes(createUserObjAttributeDTO());
		
		JAXBContext context = JAXBContext.newInstance(GetUserQueryInputDTO.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(input, outputStream);

		// serialize to XML
		String inputXML = outputStream.toString();
		System.out.println(inputXML);
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(UriBuilder.fromUri("http://localhost:8080/licensemodule/services/").build());

		// Call with XML
		String output = service.path("getUserLicenseQuery").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(inputXML).post(String.class);

		System.out.println("query:" + output);
	}


	@SuppressWarnings("all")
	private static void testCheckAccessForIds() throws Exception {
		CheckAccessForIdsInputDTO input = new CheckAccessForIdsInputDTO();
		input.setPresentationType("Search");
		input.setAttributes(createUserObjAttributeDTO());
        ArrayList<String> ids = new ArrayList<String>();        
        ids.add("doms_radioTVCollection:uuid:371157ee-b120-4504-bfaf-364c15a4137c");//radio TV        
        ids.add("doms_radioTVCollection:uuid:c3386ed5-9b79-47a2-a648-8de53569e630");//radio TV
		ids.add("doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec"); //reklame
		ids.add("doms_newspaperCollection:uuid:18709dea-802c-4bd7-98e6-32ca3b285774-segment_6"); //aviser		
		input.setIds(ids);
		
		JAXBContext context = JAXBContext.newInstance(CheckAccessForIdsInputDTO.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(input, outputStream);

		// serialize to XML
		String inputXML = outputStream.toString();
		System.out.println(inputXML);
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(UriBuilder.fromUri("http://devel06:9612/licensemodule/services/").build());

		// Call with XML
		CheckAccessForIdsOutputDTO output = service.path("checkAccessForIds").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(inputXML).post(CheckAccessForIdsOutputDTO.class);
		context = JAXBContext.newInstance(CheckAccessForIdsOutputDTO.class); 
		outputStream = new ByteArrayOutputStream();
		m = context.createMarshaller();
		 
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 	m.marshal(output, outputStream);

		// serialize to XML
		String outputXML = outputStream.toString();
		System.out.println(outputXML);
		
		
		System.out.println("query:" + output.getQuery());
		System.out.println("presentationtype:" + output.getPresentationType());
		System.out.println("number of IDs:" + output.getAccessIds().size());	
	}

	
	@SuppressWarnings("all")
	private static void testGetUsersLicenses() throws Exception {
		// GetUserLicensesOutputDTO getUserLicenses

		GetUsersLicensesInputDTO input = new GetUsersLicensesInputDTO();
		input.setAttributes(createUserObjAttributeDTO());
        input.setLocale("da");
		
		JAXBContext context = JAXBContext.newInstance(GetUsersLicensesInputDTO.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(input, outputStream);

		// serialize to XML
		String inputXML = outputStream.toString();
		System.out.println("input xml:\n" + inputXML);

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(UriBuilder.fromUri("http://localhost:8080/licensemodule/services/").build());
		// Call with XML
        //GetUsersLicensesOutputDTO output = service.path("getUserLicenses").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(inputXML).post(GetUsersLicensesOutputDTO.class);

		// Call with @XmlRootElement
		GetUsersLicensesOutputDTO output = service.path("getUserLicenses").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(input).post(GetUsersLicensesOutputDTO.class);
		context = JAXBContext.newInstance(GetUsersLicensesOutputDTO.class); 
		outputStream = new ByteArrayOutputStream();
		m = context.createMarshaller();
		 
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 	m.marshal(output, outputStream);

		// serialize to XML
		String outputXML = outputStream.toString();
		System.out.println(outputXML);
		
		
		System.out.println("output, licensenames:" + output.getLicenses());
	}

	@SuppressWarnings("all")
	private static void testGetUsersLicensesJSON() throws Exception {

		GetUsersLicensesInputDTO input = new GetUsersLicensesInputDTO();
		input.setAttributes(createUserObjAttributeDTO());
        input.setLocale("da");
	
    	Gson gson = new Gson();    
    	String json = gson.toJson(input);
    	System.out.println("input:"+json);    	
    	String responseJson= HttpClientPoster.postJSON("http://localhost:8080/licensemodule/services/getUserLicenses", json);
		System.out.println("output:"+responseJson);
    	
	}

	
	@SuppressWarnings("all")
	private static void testCheckAccessForIdsJSON() throws Exception {

		CheckAccessForIdsInputDTO input = new CheckAccessForIdsInputDTO();
		input.setPresentationType("images");
		input.setAttributes(createUserObjAttributeDTO());
        ArrayList<String> ids = new ArrayList<String>();        
        ids.add("doms_radioTVCollection:uuid:a5390b1e-69fb-47c7-b23e-7831eb59479d");//radio TV
		ids.add("doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec"); //reklame
		ids.add("doms_newspaperCollection:uuid:18709dea-802c-4bd7-98e6-32ca3b285774-segment_6"); //Aviser
        
		input.setIds(ids);
	
    	Gson gson = new Gson();    
    	String json = gson.toJson(input);
    	System.out.println("input:"+json);    	
    	String responseJson= HttpClientPoster.postJSON("http://devel06:9612/licensemodule/services/checkAccessForIds", json);
		System.out.println("output:"+responseJson);
    	
	}

	
	
	@SuppressWarnings("all")
	private static void testGetUsersGroups() throws Exception {


		GetUserGroupsInputDTO input = new GetUserGroupsInputDTO();
		input.setAttributes(createUserObjAttributeDTO());
        input.setLocale("da");
		JAXBContext context = JAXBContext.newInstance(GetUserGroupsInputDTO.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(input, outputStream);

		// serialize to XML
		String inputXML = outputStream.toString();
		System.out.println("input xml:\n" + inputXML);

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(UriBuilder.fromUri("http://localhost:8080/licensemodule/services/").build());
		// Call with XML
        //GetUsersLicensesOutputDTO output = service.path("getUserLicenses").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(inputXML).post(GetUsersLicensesOutputDTO.class);

		// Call with @XmlRootElement
		GetUserGroupsOutputDTO output = service.path("getUserGroups").type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML).entity(input).post(GetUserGroupsOutputDTO.class);
	
		
		context = JAXBContext.newInstance(GetUserGroupsOutputDTO.class); 
		outputStream = new ByteArrayOutputStream();
		 m = context.createMarshaller();
		 
		 m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 	m.marshal(output, outputStream);

		// serialize to XML
		String outputXML = outputStream.toString();
		System.out.println(outputXML);
		
		System.out.println("output, groups:" + output.getGroups());
	}
	
	
	@SuppressWarnings("all")
	private static void testGetUsersGroupsJSON() throws Exception {

		GetUserGroupsInputDTO input = new GetUserGroupsInputDTO();
		input.setAttributes(createUserObjAttributeDTO());
        input.setLocale("da");
	
    	Gson gson = new Gson();    
    	String json = gson.toJson(input);
    	System.out.println("input:"+json);    	
    	String responseJson= HttpClientPoster.postJSON("http://devel06:9612/licensemodule/services/getUserGroups", json);
		System.out.println("output:"+responseJson);
    	
	}

	@SuppressWarnings("all")
	private static void testGetUsersGroupsAndLicensesJSON() throws Exception {

		GetUserGroupsAndLicensesInputDTO input = new GetUserGroupsAndLicensesInputDTO();
		input.setAttributes(createUserObjAttributeDTO());
        input.setLocale("da");
	
    	Gson gson = new Gson();    
    	String json = gson.toJson(input);
    	System.out.println("input:"+json);    	
    	String responseJson= HttpClientPoster.postJSON("http://localhost:8080/licensemodule/services/getUserGroupsAndLicenses", json);
		System.out.println("output:"+responseJson);
    	
	}
	
	
	
	private static ArrayList<UserObjAttributeDTO> createUserObjAttributeDTO() {
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
