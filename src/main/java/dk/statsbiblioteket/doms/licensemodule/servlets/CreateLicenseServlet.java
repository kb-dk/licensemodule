package dk.statsbiblioteket.doms.licensemodule.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.facade.LicenseModuleFacade;
import dk.statsbiblioteket.doms.licensemodule.persistence.Attribute;
import dk.statsbiblioteket.doms.licensemodule.persistence.AttributeGroup;
import dk.statsbiblioteket.doms.licensemodule.persistence.AttributeValue;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseModuleStorage;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseCache;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseContent;
import dk.statsbiblioteket.doms.licensemodule.persistence.Presentation;
import dk.statsbiblioteket.doms.licensemodule.util.IpUtil;


public class CreateLicenseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(CreateLicenseServlet.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
		
		String licenseName = request.getParameter("licenseName");

		License license = (License)  request.getSession().getAttribute("license");
        long licenseId = 0;
        if (license != null){
        	licenseId=license.getId();
        }

		String event = request.getParameter("event");
		log.info("new event for License:"+licenseName +" event:"+event);

		try {				   
			license = buildLicenseFromRequest(licenseId,request);	
			request.getSession().setAttribute("license",license);			
			
			
			if ("addAttributeGroup".equals(event)){
				log.info("Adding new Attributegroup");
				ArrayList<AttributeGroup> attributeGroups = license.getAttributeGroups();				        
				AttributeGroup newGroup = new AttributeGroup(attributeGroups.size()+1); //increase counter
				ArrayList<Attribute> newAttributes = new ArrayList<Attribute>();		        
				Attribute newAttribute = new Attribute(); 
				newAttribute.setAttributeName("not defined yet");		        
				ArrayList<AttributeValue> newValues = new ArrayList<AttributeValue>();		        
				AttributeValue newValue= new AttributeValue("");
				newValues.add(newValue);		        		        
				newAttributes.add(newAttribute);
				newAttribute.setValues(newValues);		        
				newGroup.setAttributes(newAttributes);		        		        
				request.setAttribute("message", "Attributegroup tilføjet");
				attributeGroups.add(newGroup);		        						
			}
			else if ("deleteAttributeGroup".equals(event)){
				int attributeGroupNumber= Integer.parseInt(request.getParameter("attributeGroupNumber"));
				log.info("Deleting Attributegroup:"+attributeGroupNumber);
				ArrayList<AttributeGroup> attributeGroups = license.getAttributeGroups();				        		        		        
			    attributeGroups.remove(attributeGroupNumber-1);
			    renumberAttributeGroups(attributeGroups);
			    request.setAttribute("message", "Attributegroup slettet");						        					
			}			
			else if ("addAttribute".equals(event)){
				int attributeGroupNumber= Integer.parseInt(request.getParameter("attributeGroupNumber"));
				Attribute newAttribute= new Attribute();
				ArrayList<AttributeValue> newValues = new ArrayList<AttributeValue>();
				AttributeValue newValue= new AttributeValue("");
				newValues.add(newValue);
				newAttribute.setValues(newValues);												
				license.getAttributeGroups().get(attributeGroupNumber-1).getAttributes().add(newAttribute);				
			}
			else if ("addValue".equals(event)){			    				
				int attributeGroupNumber= Integer.parseInt(request.getParameter("attributeGroupNumber"));
				int attributeNumber=  Integer.parseInt(request.getParameter("attributeNumber"));
				log.info("Adding new value for attributegroup:"+ attributeGroupNumber+ " and attributeNumber:"+attributeNumber);
				license.getAttributeGroups().get(attributeGroupNumber-1).getAttributes().get(attributeNumber).getValues().add(new AttributeValue(""));							

			}
			else if ("save".equals(event)){			    				
				log.info("save");							
		        boolean validateMainFields = license.validateMainFields();
		        boolean validateAttributesValues = license.validateAttributesAndValuesNotNull();
		        
		        if (!validateMainFields){
		        	request.setAttribute("message","License name/description too short or validTo/validFrom is invalid");	 
		        	returnFormPage(request, response);
		        	return;
		        }
		        
		        if (!validateAttributesValues){
		        	request.setAttribute("message","Empty attribute or value found.");	 
		        	returnFormPage(request, response);
		        	return;
		        }
						        
		        LicenseModuleFacade.persistLicense(IpUtil.getClientIpAddress(request),license);
				returnConfigurationPage(request, response);
				return;
			}
			else if ("delete".equals(event)){			    				
				log.info("delete license");		
				//LicenseModuleFacade.persistLicense(license); Why was this here??
				LicenseModuleFacade.deleteLicense(IpUtil.getClientIpAddress(request),licenseId, true);
				returnConfigurationPage(request, response);
				return;
			}			
			else{
				log.error("unknown event:"+event);			
			}



		} catch (Exception e) {
			log.error("Unexpected error :"+e);
			request.setAttribute("message", e.getMessage());
			returnFormPage(request, response);
			return;		
		}
	    
		returnFormPage(request, response);
		return;	
	}

	private void returnFormPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		RequestDispatcher dispatcher = request.getRequestDispatcher("license.jsp");
		dispatcher.forward(request, response);		
		return;
	}

	
	private void returnConfigurationPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		RequestDispatcher dispatcher = request.getRequestDispatcher("configuration.jsp");
		dispatcher.forward(request, response);		
		return;
	}
	
	private License buildLicenseFromRequest(long licenseId, HttpServletRequest request){
		License license = new License();
        license.setId(licenseId);
		//Main fields
		String licenseName = request.getParameter("licenseName");
		String licenseName_en = request.getParameter("licenseName_en");
		String description = request.getParameter("description");
		String description_en = request.getParameter("description_en");
		String validFrom = request.getParameter("validFrom");
		String validTo = request.getParameter("validTo");

		license.setLicenseName(licenseName);
		license.setLicenseName_en(licenseName_en);
		license.setDescription_dk(description);
		license.setDescription_en(description_en);
		license.setValidFrom(validFrom);
		license.setValidTo(validTo);

		//Attribute groups
		int numberOfAttributeGroups = countAttributeGroups(request);
		//iterate over groups
		ArrayList<AttributeGroup> groups = new ArrayList<AttributeGroup>(); 
		license.setAttributeGroups(groups);
		for (int i=1;i<=numberOfAttributeGroups;i++){ //groups start from 1
			AttributeGroup group = new AttributeGroup(i);
			groups.add(group);			
			ArrayList<Attribute> attributes = new  ArrayList<Attribute>();  			
			group.setAttributes(attributes);
			int numberOfAttributes = countAttributesForGroup(request,i);
			for (int j=0;j<numberOfAttributes;j++){ 
				Attribute attribute = new Attribute();
				String attributeName = request.getParameter("attributegroup_"+i+"_attribute"+j);
				attribute.setAttributeName(attributeName);
				attributes.add(attribute); 
				ArrayList<AttributeValue> values = getValuesForAttribute(request, i, j);
				attribute.setValues(values);                   
			}			
		}		

		//Licenses
		ArrayList<LicenseContent> licenseContents = new ArrayList<LicenseContent>(); 
		license.setLicenseContents(licenseContents);
		 ArrayList<ConfiguredDomLicenseGroupType> configuredDomLicenseGroupTypes = LicenseCache.getConfiguredDomLicenseGroupTypes();
		for (int i = 0;i<configuredDomLicenseGroupTypes.size();i++){
			String domGroupCheck= request.getParameter("domsGruppe_"+i);
			if (domGroupCheck != null){ //Checkbox is checked
				String name = configuredDomLicenseGroupTypes.get(i).getKey(); 
				LicenseContent licenseContent= new LicenseContent();
				licenseContent.setName(name);
				licenseContents.add(licenseContent);

				//set presentationtypes for this dom group

				ArrayList<Presentation> presentationTypes = new ArrayList<Presentation>();                 
				licenseContent.setPresentations(presentationTypes);
				for (int j = 0;j<LicenseCache.getConfiguredDomLicenseTypes().size();j++){
					String licenseTypeCheck= request.getParameter("domsGruppe_"+i+"_license_"+j);
					if (licenseTypeCheck != null){
						String licenseTypeKey= LicenseCache.getConfiguredDomLicenseTypes().get(j).getKey();						
						Presentation presentationType = new Presentation();
						presentationType.setKey(licenseTypeKey);
						presentationTypes.add(presentationType);            		            	
					}
				}
			}

		}
		//log.info("number of groups:"+domGroups.size());

		return license;


	}


	//Attributegroups count start from 1 and not 0 to match its attribute 'number'	
	private int countAttributeGroups(HttpServletRequest request){
		int i=1; //group number starting from 1.
		while (request.getParameter("attributegroup_"+i+"_attribute0") != null){		
			i++;
		}

		return (i-1); //Count starting from 1
	}

	private int countAttributesForGroup(HttpServletRequest request, int groupNumber){

		int i=0;			
		while (request.getParameter("attributegroup_"+groupNumber+"_attribute"+i) != null){ 			
			i++;
		}

		return (i);  
	}	

	private void renumberAttributeGroups(ArrayList<AttributeGroup> groups){
		int groupNumber=1;
		for (AttributeGroup current: groups){
			current.setNumber(groupNumber++);
		}
		
		
	}
	
	private ArrayList<AttributeValue> getValuesForAttribute(HttpServletRequest request, int groupNumber, int attributeNumber){
		ArrayList<AttributeValue> values = new ArrayList<AttributeValue>();

		int i=0;

		String value = request.getParameter("attributegroup_"+groupNumber+"_attribute"+attributeNumber+"_value"+i); 
		while (value != null){					  
			values.add(new AttributeValue(value));		    
			i++;
			value = request.getParameter("attributegroup_"+groupNumber+"_attribute"+attributeNumber+"_value"+i);
		}

		return values;
	}

}
