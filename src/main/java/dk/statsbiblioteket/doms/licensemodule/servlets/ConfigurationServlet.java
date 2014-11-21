package dk.statsbiblioteket.doms.licensemodule.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.H2Storage;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseCache;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUsersLicensesInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserObjAttributeDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.ValidateAccessInputDTO;
import dk.statsbiblioteket.doms.licensemodule.validation.LicenseValidator;

public class ConfigurationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ConfigurationServlet.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");

		String event = request.getParameter("event");
		log.info("New event for ConfigurationServlet:" + event);

		H2Storage storage = H2Storage.getInstance();

		try {
			// tab 0 is list licenses

			if ("save_presentationtype".equals(event)) {
				request.setAttribute("tab", "1");
				String key = request.getParameter("key_presentationtype");
				String value = request.getParameter("value_presentationtype");
				String value_en = request.getParameter("value_en_presentationtype");
				log.debug("Saving new presentationtype:" + key);
				storage.persistDomLicensePresentationType(key,value,value_en);
			} else if ("save_grouptype".equals(event)) {
				request.setAttribute("tab", "2");
				String key = request.getParameter("key_grouptype");
				String value_dk = request.getParameter("value_grouptype");
				String value_en = request.getParameter("value_en_grouptype");
				String description = request.getParameter("value_groupdescription");
				String description_en = request.getParameter("value_en_groupdescription");
				String query = request.getParameter("value_groupquery");
				String isMustGroupStr = request.getParameter("mustGroupCheck");
				boolean isMustGroup = false;
				log.debug("Saving new grouptype:" + key);
				if (isMustGroupStr != null) { // Checkbox is checked
					isMustGroup = true;
				}
				storage.persistDomLicenseGroupType(key,value_dk,value_en,description,description_en,query, isMustGroup);

			} else if ("save_attributetype".equals(event)) {

				request.setAttribute("tab", "3");
				String value = request.getParameter("value_attributetype");
				log.debug("Saving new attributetype:" + value);
				storage.persistDomAttributeType(value);

			} else if ("validate".equals(event)) {
				log.debug("validate called");
				request.setAttribute("tab", "4");
				String validation_attribute_values = request.getParameter("validation_attribute_values");				                                                           		
				String validation_groups = request.getParameter("validation_groups");
				String validation_presentationtype = request.getParameter("validation_presentationtype");

				request.setAttribute("validation_attribute_values", validation_attribute_values);
				request.setAttribute("validation_groups", validation_groups);
				request.setAttribute("validation_presentationtype", validation_presentationtype);


				String result = decomposeValidateAccess(validation_attribute_values,validation_groups,validation_presentationtype);
				request.setAttribute("validation_result", result);		
			}
			else if ("validateQuery".equals(event)) {
				log.debug("validateQuery called");
				request.setAttribute("tab", "5");
				String validationQuery_attribute_values = request.getParameter("validationQuery_attribute_values");				                                                           		
				String validationQuery_presentationtype = request.getParameter("validationQuery_presentationtype");

				request.setAttribute("validationQuery_attribute_values", validationQuery_attribute_values);
				request.setAttribute("validationQuery_presentationtype", validationQuery_presentationtype);

				String result = decomposeValidateQuery(validationQuery_attribute_values,validationQuery_presentationtype);
				request.setAttribute("validationQuery_result", result);		
			}			
			else if ("checkAccessIds".equals(event)) {
				log.debug("checkAccessIds called");
				request.setAttribute("tab", "6");
				String checkAccessIds_attribute_values = request.getParameter("checkAccessIds_attribute_values");				                                                           		
				String checkAccessIds_presentationtype = request.getParameter("checkAccessIds_presentationtype");
				String checkAccessIds_ids = request.getParameter("checkAccessIds_ids");
				
				request.setAttribute("checkAccessIds_attribute_values", checkAccessIds_attribute_values);
				request.setAttribute("checkAccessIds_presentationtype", checkAccessIds_presentationtype);
				request.setAttribute("checkAccessIds_ids", checkAccessIds_ids);

				String result = decomposCheckAccessIds(checkAccessIds_attribute_values,checkAccessIds_presentationtype,checkAccessIds_ids);
				request.setAttribute("checkAccessIds_result", result);		
			}			
			else if ("deletePresentationType".equals(event)) {
				log.debug("deletePresentationType called");
				request.setAttribute("tab", "1");
				String typeName = request.getParameter("typeName");				                                                           				
				storage.deleteDomPresentationType(typeName);
			}
			else if ("deleteGroupType".equals(event)) {
				log.debug("deleteGroup called");
				request.setAttribute("tab", "2");
				String typeName = request.getParameter("typeName");				                                                           				
				storage.deleteDomLicenseGroupType(typeName);
			}
			else if ("deleteAttributeType".equals(event)) {
				log.debug("deleteAttributeType called");
				request.setAttribute("tab", "3");
				String typeName = request.getParameter("typeName");				                                                           				
				storage.deleteDomAttributeType(typeName);
			}
			else if ("updateGroup".equals(event)) {
				log.debug("updateGroup called");
				request.setAttribute("tab", "2");

				String id = request.getParameter("id");
				//String key = request.getParameter("key");//Not used. Update by ID.
				String value = request.getParameter("value_grouptype");
				String value_en = request.getParameter("value_en_grouptype");
				String description = request.getParameter("value_groupdescription");
				String description_en = request.getParameter("value_en_groupdescription");
				String query = request.getParameter("value_groupquery");
				String isMustGroupStr = request.getParameter("mustGroupCheck");
				boolean isMustGroup = false;

				if (isMustGroupStr != null) { // Checkbox is checked
					isMustGroup = true;
				}
				log.debug("Updating license group with id:" + id);
				storage.updateDomLicenseGroupType(Long.parseLong(id),value, value_en,description,description_en, query, isMustGroup);
			}						
			else if ("updatePresentationType".equals(event)) {
				log.debug("updatePresentationType called");
				request.setAttribute("tab", "1");
				String id = request.getParameter("id");
				//String key = request.getParameter("key");//Not used. Update by ID.
				String value = request.getParameter("value_presentationtype");
				String value_en = request.getParameter("value_en_presentationtype");							
				log.debug("Updating presentatintype with id:" + id);
				storage.updateDomPresentationType(Long.parseLong(id),value, value_en);
			}						
			else {								
				log.error("Unknown event:" + event);
				request.setAttribute("message", "Unknown event:"+event);
			}

		} catch (Exception e) {//various server errors
			log.error("unexpected error", e);
			request.setAttribute("message", e.getMessage());
			returnFormPage(request, response);
			return;
		}

		returnFormPage(request, response);
		return;
	}

	private void returnFormPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("configuration.jsp");
		dispatcher.forward(request, response);
		return;
	}

	private String decomposeValidateAccess (String validation_attribute_values, String validation_groups, String validation_presentationtype) throws Exception{
		StringBuilder infoMessage = new StringBuilder();   
		//parse input first.
		ValidateAccessInputDTO input = new ValidateAccessInputDTO();
		ConfiguredDomLicensePresentationType presentationType = null;
		ArrayList<UserObjAttributeDTO> attributes;
		try{

			attributes = createUserObjFromFormData(validation_attribute_values);
			ArrayList<String> groups = createGroupsFromFormData(validation_groups);		
			ArrayList<String> presentationTypes = createPresentationTypesFromFormData(validation_presentationtype);
			if (presentationTypes.size() !=1){
				infoMessage.append("Der skal angives een presentationstype");
				return infoMessage.toString();	
			}
			presentationType = LicenseValidator.matchPresentationtype(presentationTypes.get(0));
			input.setAttributes(attributes);
			input.setGroups(groups);
			input.setPresentationType(presentationTypes.get(0));

		}
		catch(Exception e){
			infoMessage.append("Input validerings fejl fra web-form:"+e.getMessage());
			return infoMessage.toString();
		}        


		//The following logic is taken from LicenseValidator.validateAccess().
		//I see no other way that to repeat it when I want to the decomposition.

		ArrayList<ConfiguredDomLicenseGroupType> groupsType = null;
		ArrayList<ConfiguredDomLicenseGroupType> mustGroups = null; 
		try{
			boolean validated = LicenseValidator.validateAccess(input);
			infoMessage.append("Resultat af validateAccess() kald:"+validated +" \n");
			infoMessage.append("Detaljer: \n");
			groupsType = LicenseValidator.buildGroups(input.getGroups());
			mustGroups = LicenseValidator.filterMustGroups(groupsType);
			if (mustGroups.size() > 0){
				infoMessage.append("MUST-grupper i input:"+mustGroups +"\n");	
			}
			else{
				infoMessage.append("Der blev ikke fundet MUST-grupper i input.\n");				
			}

			ArrayList<License> allLicenses = LicenseCache.getAllLicense();
			infoMessage.append("Samlet antal licenser i databasen:"+allLicenses.size()+"\n");		

			ArrayList<License> dateFilteredLicenses = LicenseValidator.filterLicenseByValidDate(allLicenses, System.currentTimeMillis());	
			infoMessage.append("Samlet antal licenser inden for perioden:"+dateFilteredLicenses.size()+"\n");

			ArrayList<License> accessLicenses = LicenseValidator.findLicensesValidatingAccess(input.getAttributes(),dateFilteredLicenses);
			infoMessage.append("Følgende licenser opfylder access-krav(uden check af grupper):"+accessLicenses+"\n");
			if (accessLicenses.size()==0){
				infoMessage.append("Ingen licenser opfylder access-krav(uden check af grupper) \n");	
				return infoMessage.toString();
			}


			//Test method getUsersLicenseGroups
			GetUsersLicensesInputDTO inputGroups = new GetUsersLicensesInputDTO();
			inputGroups.setAttributes(attributes);						

			if (mustGroups.size() == 0){
				log.error("presentationtype:"+presentationType);
				ArrayList<License> validatedLicenses = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeNoMustGroup(accessLicenses, groupsType, presentationType);

				if (validatedLicenses.size() == 0){				
					infoMessage.append("Ingen licenser opfylder gruppe betingelsen. \n");		
				}
				else{				
					infoMessage.append("Følgende license opfylder gruppe-betingelsen:"+validatedLicenses +"\n");				
				}								        	          
			}
			else{
				ArrayList<License> validatedLicenses = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeMustGroup(accessLicenses, mustGroups , presentationType);
				if (validatedLicenses.size() == 0){
					infoMessage.append("Access-krav licenserne opfylder ikke alle MUST-gruppe betingelser.\n");		
				}
				else{
					infoMessage.append("Følgende licenser opfylder tilsammen MUST-gruppe betingelser:"+validatedLicenses +"\n");				
				}
			}		
			//infoMessage.append("Generated Query:"+userGroupsDTO.getQueryString());
		}

		catch(Exception e){
			infoMessage.append("Fejl under validateAccess kald:"+e.getMessage());	
			return infoMessage.toString();
		}
		return infoMessage.toString();
	}


	private String decomposeValidateQuery (String validation_attribute_values,  String validation_presentationtypes) throws Exception{
		StringBuilder infoMessage = new StringBuilder();   
		//parse input first.
		GetUserQueryInputDTO input = new GetUserQueryInputDTO();
		ArrayList<UserObjAttributeDTO> attributes;
		try{

			attributes = createUserObjFromFormData(validation_attribute_values);		
			ArrayList<String> presentationTypes = createPresentationTypesFromFormData(validation_presentationtypes);
			if (presentationTypes.size() == 0){
				infoMessage.append("Der skal angives een presentationstype");
				return infoMessage.toString();	
			}
			input.setAttributes(attributes);

			input.setPresentationType(presentationTypes.get(0));

		}
		catch(Exception e){
			infoMessage.append("Input validerings fejl fra web-form:"+e.getMessage());
			return infoMessage.toString();
		}        


		//The following logic is taken from LicenseValidator.getUserQuery
		//I see no other way that to repeat it when I want to the decomposition.

		try{
			GetUserQueryOutputDTO output = LicenseValidator.getUserQuery(input);
			infoMessage.append("Detaljer: \n");
			infoMessage.append("Brugeren opfylder følgende grupper:"+output.getUserLicenseGroups() +"\n");
			infoMessage.append("Brugeren mangler følgende MUST grupper:"+output.getUserNotInMustGroups() +"\n");	
			infoMessage.append("Query:"+output.getQuery() +"\n");
		}
		catch(Exception e){
			infoMessage.append("Fejl under validateQuery kald:"+e.getMessage());	
			return infoMessage.toString();
		}
		return infoMessage.toString();
	}

	private String decomposCheckAccessIds(String checkAccessIds_attribute_values,  String checkAccessIds_presentationtype, String checkAccessIds_ids) throws Exception{
		StringBuilder infoMessage = new StringBuilder();   
		CheckAccessForIdsInputDTO input = new CheckAccessForIdsInputDTO();
		ArrayList<UserObjAttributeDTO> attributes;
		try{
			attributes = createUserObjFromFormData(checkAccessIds_attribute_values);		
			input.setAttributes(attributes);
			input.setPresentationType(checkAccessIds_presentationtype);
            input.setIds(createIdsFormData(checkAccessIds_ids));			
			
		}
		catch(Exception e){
			infoMessage.append("Input validerings fejl fra web-form:"+e.getMessage());
			return infoMessage.toString();
		}        

		try{
			CheckAccessForIdsOutputDTO output = LicenseValidator.checkAccessForIds(input);
			infoMessage.append("Detaljer: \n");			
			infoMessage.append("Presentationtype:"+output.getPresentationType() +" \n");
			infoMessage.append("Access query part:"+output.getQuery() +" \n");
			infoMessage.append("#Ids:"+output.getAccessIds().size() +" \n");
			infoMessage.append("Ids:"+output.getAccessIds() +" \n");						
		}
		catch(Exception e){
			infoMessage.append("Fejl under checkAccessIds kald:"+e.getMessage());	
			return infoMessage.toString();
		}
		return infoMessage.toString();
	}

	

	private ArrayList<String> createGroupsFromFormData(String validation_groups){
		ArrayList<String> groups = new ArrayList<String>();
		String[] tmp = StringUtils.split(validation_groups, ",");
		if (tmp.length == 0){
			throw new IllegalArgumentException("Der skal være angivet mindst en gruppe i attributegrupper feltet");
		}
		for (String group : tmp){
			groups.add(group.trim());
		}		  
		return groups;		
	}

	private ArrayList<String> createIdsFormData(String validation_ids){
		ArrayList<String> ids = new ArrayList<String>();
		String[] tmp = StringUtils.split(validation_ids, ",");
		if (tmp.length == 0){
			throw new IllegalArgumentException("Der skal være angivet mindst et recordId");
		}
		for (String id : tmp){
			ids.add(id.trim());
		}		  
		return ids;		
	}
	
	private ArrayList<String> createPresentationTypesFromFormData(String validation_presentationTypes){
		ArrayList<String> presentationTypes = new ArrayList<String>();
		String[] tmp = StringUtils.split(validation_presentationTypes, ",");
		if (tmp.length == 0){
			throw new IllegalArgumentException("Der skal være angivet mindst en presentationtype");
		}
		for (String group : tmp){
			presentationTypes.add(group.trim());
		}		  
		return presentationTypes;		
	}

	private ArrayList<UserObjAttributeDTO> createUserObjFromFormData(String validation_attribute_values){
		String[] lines = StringUtils.split(validation_attribute_values, "\n");
		if (lines.length == 0){
			throw new IllegalArgumentException("Der skal være mindst 1 linie i attribut/values tekstboksen");
		}

		ArrayList<UserObjAttributeDTO> attributes = new ArrayList<UserObjAttributeDTO>(); 
		// every line on the form attributename: value1 , value2, value 3 ,...
		for (String line : lines){
			UserObjAttributeDTO attribute = new UserObjAttributeDTO();
			attributes.add(attribute);
			String[] tmp = StringUtils.split(line, ":");
			if (tmp.length != 2){
				throw new IllegalArgumentException("Attribute/value linie kan ikke parses. Der skal være et : efter attributename for linie:"+line);
			}
			attribute.setAttribute(tmp[0].trim());
			String[] values = StringUtils.split(tmp[1], ",");
			if (values.length == 0){
				throw new IllegalArgumentException("Der skal være mindst en attributevalue for linie:"+line);
			}

			ArrayList<String> valueList = new ArrayList<String>(); 
			for (String value : values){
				valueList.add(value.trim());
			}
			attribute.setValues(valueList);	   	

		}
		return attributes;
	}

}
