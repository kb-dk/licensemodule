package dk.statsbiblioteket.doms.licensemodule.validation;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;
import dk.statsbiblioteket.doms.licensemodule.Util;
import dk.statsbiblioteket.doms.licensemodule.persistence.Attribute;
import dk.statsbiblioteket.doms.licensemodule.persistence.AttributeGroup;
import dk.statsbiblioteket.doms.licensemodule.persistence.AttributeValue;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseCache;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseContent;
import dk.statsbiblioteket.doms.licensemodule.persistence.Presentation;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.CheckAccessForIdsOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserQueryOutputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUsersLicensesInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserGroupDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserObjAttributeDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.ValidateAccessInputDTO;
import dk.statsbiblioteket.doms.licensemodule.solr.SolrServerClient;

public class LicenseValidator {

	private static final Logger log = LoggerFactory.getLogger(LicenseValidator.class);		
	public static final String LOCALE_DA = "da";
	public static final String LOCALE_EN = "en";
	public static final String NO_ACCESS = "(recordID:NoAccess)";

	
	
    public static final List locales=
			     Collections.unmodifiableList(Arrays.asList(new String[] {LOCALE_DA,LOCALE_EN}));
	
	// The following 3 methods are the API

	//TODO shitload of javadoc
	public static ArrayList<License> getUsersLicenses(GetUsersLicensesInputDTO input) throws Exception{
		//validate
		if (input.getAttributes() == null || input.getAttributes().size() == 0){
			log.error("No attributes defined in input.");
			throw new IllegalArgumentException("No attributes defined in input");
		}
        validateLocale(input.getLocale());

		// First filter by valid date
		ArrayList<License> allLicenses = LicenseCache.getAllLicense();		
		ArrayList<License> dateFilteredLicenses = filterLicenseByValidDate(allLicenses, System.currentTimeMillis());		

		//Find licenses that give access (not checking groups) for the dateFiltered licenses
		ArrayList<License> accessLicenses = findLicensesValidatingAccess(input.getAttributes(),  dateFilteredLicenses);

		return accessLicenses;
	}

	//TODO shitload of javadoc
	public static ArrayList<UserGroupDTO> getUsersGroups(GetUserGroupsInputDTO input) throws Exception{
		//validate
		if (input.getAttributes() == null || input.getAttributes().size() == 0){
			log.error("No attributes defined in input.");
			throw new IllegalArgumentException("No attributes defined in input");
		}

		validateLocale(input.getLocale());
		
		// First filter by valid date
		ArrayList<License> allLicenses = LicenseCache.getAllLicense();		

		ArrayList<License> dateFilteredLicenses = filterLicenseByValidDate(allLicenses, System.currentTimeMillis());		
		
		//Find licenses that give access (not checking groups) for the dateFiltered licenses
		ArrayList<License> accessLicenses = findLicensesValidatingAccess(input.getAttributes(),  dateFilteredLicenses);

		ArrayList<UserGroupDTO> filteredGroups = filterGroupsWithPresentationtype(accessLicenses);
		        
	    LicenseValidator.fixLocale(filteredGroups, input.getLocale());       
	    
		return filteredGroups;
	}

	//TODO shitload of javadoc
	public static CheckAccessForIdsOutputDTO checkAccessForIds(CheckAccessForIdsInputDTO input) throws Exception{
       
	    if  (input.getIds() == null || input.getIds().size() == 0){
	        throw new IllegalArgumentException("No ID's in input");	        
	    }
	    
		//Get the query. This also validates the input 
		GetUserQueryInputDTO inputQuery = new GetUserQueryInputDTO();		
		inputQuery.setAttributes(input.getAttributes());
		inputQuery.setPresentationType(input.getPresentationType());
		GetUserQueryOutputDTO query = getUserQuery(inputQuery);
		
		
		CheckAccessForIdsOutputDTO output = new  CheckAccessForIdsOutputDTO();
		output.setPresentationType(input.getPresentationType());
		output.setQuery(query.getQuery());

		ArrayList<SolrServerClient> servers = LicenseModulePropertiesLoader.SOLR_SERVERS;
	
		  // merge (union) results.   
        Set<String> filteredIdsSet = new HashSet<String>();
		
		//Next step: use Future's to make multithreaded when we get more servers. 
		//But currently these requests are less 10 ms
		for (SolrServerClient server: servers){
	        ArrayList<String> filteredIds =server.filterIds(input.getIds(), query.getQuery());
	        log.debug("#filtered id for server ("+input.getPresentationType()+") "+ server.getSolrServer().getBaseURL() +" : "+filteredIds.size());
		    filteredIdsSet.addAll(filteredIds);
		}
		//Now we have to remove remove ID's not asked for that are here because of multivalue field. (set intersection)
	    filteredIdsSet.retainAll(input.getIds());
						
		output.setAccessIds(new ArrayList<String>(filteredIdsSet));
		//Sanity check!
		if (output.getAccessIds().size() > input.getIds().size()){
			throw new IllegalArgumentException("Security problem: More Id's in output than input. Check for query injection.");
		}
		
		log.debug("#query IDs="+input.getIds().size() + " returned #filtered IDs="+output.getAccessIds().size());
		return output;		
	}

	//TODO shitload of javadoc
	public static GetUserQueryOutputDTO getUserQuery(GetUserQueryInputDTO input) throws Exception{
		//validate
		if (input.getAttributes() == null){
			log.error("No attributes defined in input.");
			input.setAttributes(new ArrayList<UserObjAttributeDTO>());
			
		}		

		if (input.getPresentationType() == null){
			log.error("No presentationtype defined in input.");
			throw new IllegalArgumentException("No presentationtype defined in input.");
		}


		//Will throw exception if not matched		
        matchPresentationtype(input.getPresentationType());		


		// First filter by valid date
		ArrayList<License> allLicenses = LicenseCache.getAllLicense();		
		ArrayList<License> dateFilteredLicenses = filterLicenseByValidDate(allLicenses, System.currentTimeMillis());		

		//Find licenses that give access (not checking groups) for the dateFiltered licenses
		ArrayList<License> accessLicenses = findLicensesValidatingAccess(input.getAttributes(),  dateFilteredLicenses);

		ArrayList<String> types = new ArrayList<String>();
		types.add(input.getPresentationType());
		
		ArrayList<String> filterGroups = filterGroups(accessLicenses,  types);

		//Now we have to find all MUST-groups the user is missing 
		ArrayList<ConfiguredDomLicenseGroupType> configuredMUSTDomLicenseGroupTypes = LicenseCache.getConfiguredMUSTDomLicenseGroupTypes();
		GetUserQueryOutputDTO output = new GetUserQueryOutputDTO();
		output.setUserLicenseGroups(filterGroups);

		ArrayList<String> missingMustGroups = new ArrayList<String>();
		//First add all must groups then remove those that user has access too
		for (ConfiguredDomLicenseGroupType current : configuredMUSTDomLicenseGroupTypes){
			missingMustGroups.add(current.getKey());
		}

		for (String current : filterGroups){
			missingMustGroups.remove(current); 
		}
		output.setUserNotInMustGroups(missingMustGroups);	

		String query = generateQueryString(filterGroups, missingMustGroups);
		output.setQuery(query);
		return output;
	}


	//TODO shitload of javadoc
	public static boolean validateAccess(ValidateAccessInputDTO input) throws Exception{

		//validate
		if (input.getAttributes() == null || input.getAttributes().size() == 0){
			log.error("No attributes defined in input.");
			throw new IllegalArgumentException("No attributes defined in input");
		}


		ConfiguredDomLicensePresentationType presentationType = matchPresentationtype(input.getPresentationType());

		//Validate presentationType exists
		if (presentationType == null){
			log.warn("Unknown presentationtype in validateAccess:"+input.getPresentationType());			
			throw new IllegalArgumentException("Unknown presentationtype in validateAccess:"+input.getPresentationType());
		}

		ArrayList<ConfiguredDomLicenseGroupType> groups = buildGroups(input.getGroups());
		//Validate groups. Same size or one was not matched.
		if (groups.size() != input.getGroups().size()){
			log.warn("At least 1 unknown group  in validateAccess:"+input.getGroups());			
			throw new IllegalArgumentException("At least 1 unknown group  in validateAccess:"+input.getGroups());
		}		

		ArrayList<ConfiguredDomLicenseGroupType> mustGroups = filterMustGroups(groups);
		if (mustGroups.size() > 0){
			log.debug("At least 1 MUST groups found in input, number of MUST-groups:"+mustGroups.size());			
		}

		// First filter by valid date
		ArrayList<License> allLicenses = LicenseCache.getAllLicense();		
		ArrayList<License> dateFilteredLicenses = filterLicenseByValidDate(allLicenses, System.currentTimeMillis());		

		//Find licenses that give access (not checking groups) for the dateFiltered licenses
		ArrayList<License> accessLicenses = findLicensesValidatingAccess(input.getAttributes(),  dateFilteredLicenses);

		if (accessLicenses.size()==0){
			log.debug("No licenses validate access-part");
			return false;
		}

		//two situations. At least one MUST group involved, or no MUST groups.
		if (mustGroups.size() == 0){
			log.debug("Case: no MUST group");		
			//Simple situation. Just need to find 1 license having one of the groups with allowed presentationtype
			ArrayList<License> validatedLicenses = filterLicensesWithGroupNamesAndPresentationTypeNoMustGroup(accessLicenses, groups, presentationType);
			return (validatedLicenses.size() >0); //OK since at least 1 license found        	           
		}
		else{
			// ALL groups+presentationtype must be in at least 1 license
			//Only MUST groups are checked
			log.debug("Case: at least 1 MUST group");
			//notice only the mustGroups are used
			ArrayList<License> validatedLicenses = filterLicensesWithGroupNamesAndPresentationTypeMustGroup(accessLicenses, mustGroups , presentationType);
			return (validatedLicenses.size() >0); //OK since at least 1 license found
		}
	}

	// Helper method below


	//Collect all dom group names. Why does the caller(API) not want to know about the presentationtypes?
	public static ArrayList<String> filterGroups(ArrayList<License> licenses) {
		HashSet<String> groups = new HashSet<String>();
		for (License current : licenses){
			for (LicenseContent currentContent : current.getLicenseContents()){
				groups.add(currentContent.getName());
			}
		}		
		return new ArrayList<String>(groups);
	}


	//Get all dom-groups and for each dom-group find the union of presentationtypes
	//Sort by dom group name
	public static ArrayList<UserGroupDTO> filterGroupsWithPresentationtype(ArrayList<License> licenses){
		TreeMap<String, UserGroupDTO> groups = new TreeMap<String, UserGroupDTO>();
		for (License currentLicense : licenses){
			for (LicenseContent currentGroup : currentLicense.getLicenseContents()){
				String name = currentGroup.getName();
				UserGroupDTO group = groups.get(name);
				if (group == null){
					group = new UserGroupDTO();        	
					group.setPresentationTypes(new ArrayList<String>());
					group.setGroupName(name);
					groups.put(name,group);
				}
				for (Presentation currentPresentation: currentGroup.getPresentations()){
					String presentation_key = currentPresentation.getKey();
					if (group.getPresentationTypes().contains(presentation_key)){
						//Already added
					}
					else{
						group.getPresentationTypes().add(presentation_key);
					}
				}
			}
		}
		return new ArrayList<UserGroupDTO>(groups.values());
	}


	//Collect all dom group names that have one of the given presentationtypes
	public static ArrayList<String> filterGroups(ArrayList<License> licenses, ArrayList<String> presentationTypes) {
		HashSet<String> groups = new  HashSet<String>();
		for (License current : licenses){
			for (LicenseContent currentContent : current.getLicenseContents()){
				for (Presentation currentPresentation: currentContent.getPresentations()){
					if (presentationTypes.contains(currentPresentation.getKey())){
						groups.add(currentContent.getName()); 	

					}				      
				}				
			}
		}		
		return new ArrayList<String>(groups);
	}

	public static ArrayList<License> filterLicenseByValidDate(ArrayList<License> licenses, long date){
		ArrayList<License> filtered = new ArrayList<License>();
		for (License currentLicense : licenses){

			long validFromLong = Util.convertDateFormatToLong( currentLicense.getValidFrom());
			long validToLong = Util.convertDateFormatToLong( currentLicense.getValidTo());

			if (validFromLong <= date &&  date < validToLong ){ // interval: [start,end[ 
				filtered.add(currentLicense);	
			}

		}		
		return filtered;		
	}



	//TODO shitload oof javadoc
	/*
	 * This method only validates the access-part (tab #1 on the gui). For every licence every attributegroup
	 * is validated. If an attributegroup validates, the license is added to the return list.
	 * 
	 */
	public static ArrayList<License> findLicensesValidatingAccess(ArrayList<UserObjAttributeDTO> attributes, ArrayList<License> allLicenses){		
		ArrayList<License> licenses = new  ArrayList<License>();

		//Iterate all licenses and test accesss
		boolean validatedForAtLeastOneLicense = false;
		for (License currentLicense : allLicenses){
			boolean licenseAllreadyAdded=false;
			//for each license check all attributegroups
			ArrayList<AttributeGroup> groups = currentLicense.getAttributeGroups();
			for (AttributeGroup currentGroup : groups){			 
				boolean allAttributeGroupPartsMatched = true;
				for (Attribute currentAttribute : currentGroup.getAttributes()){
					ArrayList<UserObjAttributeDTO> filtered = filterUserObjAttributesToValidatedOnly(currentAttribute, attributes);
					if (filtered.size() == 0){ //Found attributegroup-part did not validate
						allAttributeGroupPartsMatched = false; //Could break, but finding all attributegroup-parts matches is useful for debug purpose
					}					
				}

				if (allAttributeGroupPartsMatched && ! licenseAllreadyAdded){				
					validatedForAtLeastOneLicense=true;
					licenseAllreadyAdded=true;
					licenses.add(currentLicense);
					log.debug("For license:"+ currentLicense.getLicenseName() + " VALIDATED for attributegroup number:"+currentGroup.getNumber());					

				}
				else{
					log.debug("For license:"+ currentLicense.getLicenseName() + " FAILED VALIDATE for attributegroup number:"+currentGroup.getNumber());
				}
			}
		}
		log.info("Validate completed, access="+validatedForAtLeastOneLicense);
		return licenses;
	}

	//Filter so only the UserObjAttribute that match the license are returned. Values that does not match are also removed.
	protected static ArrayList<UserObjAttributeDTO> filterUserObjAttributesToValidatedOnly(Attribute licenseattribute,  ArrayList<UserObjAttributeDTO> userAttributes){
		String name = licenseattribute.getAttributeName();
		ArrayList<AttributeValue> values = licenseattribute.getValues();		
		ArrayList<UserObjAttributeDTO> filteredUserObjAttributes = new ArrayList<UserObjAttributeDTO>();

		for (UserObjAttributeDTO currentUserObjAttribute : userAttributes){
			UserObjAttributeDTO newFilteredObjAttribute = new UserObjAttributeDTO(); //will be added to list returned if match		     
			ArrayList<String> newFilteredObjAttributeValues = new ArrayList<String>(); 
			newFilteredObjAttribute.setValues(newFilteredObjAttributeValues);
			if (currentUserObjAttribute.getAttribute().equals(name)){ //We have an attribute match, see if any values match				 
				newFilteredObjAttribute.setAttribute(name); //Name match, but does any values also match?
				for (String currentObjAttributeValue : currentUserObjAttribute.getValues()){ 
					if (containsName(values,currentObjAttributeValue)){//Value match, add to filtered						
						newFilteredObjAttributeValues.add(currentObjAttributeValue);						  
					}					 
				}            	 
			}
			if (newFilteredObjAttributeValues.size() >0){ //we actually found attribute name and at least 1 value
				filteredUserObjAttributes.add(newFilteredObjAttribute); 
			}
		}

		return filteredUserObjAttributes;		
	}



	//For the MUST group situation. All groups must be matched (not necessary by same license, all groups having the given presentationtype)  	
	public static ArrayList<License> filterLicensesWithGroupNamesAndPresentationTypeMustGroup(ArrayList<License> licenses,
			ArrayList<ConfiguredDomLicenseGroupType> groups, ConfiguredDomLicensePresentationType presentationType){

		//Iterator over groups first, since each must be found
		HashSet<License> filteredSet = new HashSet<License>(); 
		int groupsFound = 0;
		for (ConfiguredDomLicenseGroupType currentGroup : groups){
			String groupKey = currentGroup.getKey();

			for (License currentLicense : licenses){
				boolean found = Util.domGroupsContainsGroupWithLicense(currentLicense.getLicenseContents(), groupKey, presentationType.getKey());                 
				if (found){ 
					groupsFound++; //Can only happen once for each group due to the break below from inner loop
					filteredSet.add(currentLicense); 				
					break; // Group found, break inner loop			   
				}
			}			
		}
		if (groupsFound == groups.size()){ //All groups was matched
			return new ArrayList<License>(filteredSet);
		}

		return new ArrayList<License>(); //Empty list.

	}


	//For the no must group situation. Just one of the groups has to be matched
	//return when first license validate
	public static ArrayList<License> filterLicensesWithGroupNamesAndPresentationTypeNoMustGroup(ArrayList<License> licenses,
			ArrayList<ConfiguredDomLicenseGroupType> groups, ConfiguredDomLicensePresentationType presentationType){
		ArrayList<License> filtered= new  ArrayList<License>();
		for (License currentLicense : licenses){		
			for (ConfiguredDomLicenseGroupType currentGroup : groups){
				String groupKey = currentGroup.getKey();
				if (Util.domGroupsContainsGroupWithLicense(currentLicense.getLicenseContents(), groupKey, presentationType.getKey())){			     			        	 
					filtered.add(currentLicense);

					return filtered;
				} 
			}			 
		}
		return filtered; //Will be empty list
	}




	//Will remove all non MUST-groups. 
	public static ArrayList<ConfiguredDomLicenseGroupType> filterMustGroups(ArrayList<ConfiguredDomLicenseGroupType> groups){
		ArrayList<ConfiguredDomLicenseGroupType> filteredGroups = new ArrayList<ConfiguredDomLicenseGroupType>();

		for (ConfiguredDomLicenseGroupType currentGroup : groups){
			//TODO performence tuning, use cachedMap of GroupTypes.		
			if ( currentGroup.isMustGroup() ){
				filteredGroups.add(currentGroup);
			}				   
		}			
		return filteredGroups;
	}

	//Maps the groups(String names) to the configured objects. 
	public static ArrayList<ConfiguredDomLicenseGroupType> buildGroups(ArrayList<String> groups){
		ArrayList<ConfiguredDomLicenseGroupType> filteredGroups = new ArrayList<ConfiguredDomLicenseGroupType>();
		ArrayList<ConfiguredDomLicenseGroupType> configuredGroups = LicenseCache.getConfiguredDomLicenseGroupTypes();

		HashMap<String,ConfiguredDomLicenseGroupType> configuredGroupsNamesMap = new HashMap<String, ConfiguredDomLicenseGroupType>();
		for (ConfiguredDomLicenseGroupType current : configuredGroups){
			configuredGroupsNamesMap.put(current.getKey(), current);  
		}				   

		for (String currentGroup : groups){
			if (configuredGroupsNamesMap.containsKey(currentGroup)){
				filteredGroups.add(configuredGroupsNamesMap.get(currentGroup));
			}
			else{
				log.error("Group not found in Group configuration:"+currentGroup);
				throw new IllegalArgumentException("Unknown group:"+currentGroup);
			}


		}		
		return filteredGroups;
	}

	public static ConfiguredDomLicensePresentationType matchPresentationtype(String presentationTypeName){

		ArrayList<ConfiguredDomLicensePresentationType> configuredTypes = LicenseCache.getConfiguredDomLicenseTypes();
		for (ConfiguredDomLicensePresentationType currentType : configuredTypes){
			if (currentType.getKey().equals(presentationTypeName)){
				return currentType;
			}

		}				
		throw new IllegalArgumentException("Unknown presentationType:"+presentationTypeName);		
	}


	//Return true of any of the AttributeValues in the list has value =valueToFind
	private static boolean containsName( ArrayList<AttributeValue> values, String valueToFind){

		for (AttributeValue current : values){
			if (current.getValue().equals(valueToFind)){
				return true;
			}
		}
		return false;
	}

	public static String generateQueryString(ArrayList<String> accessGroups, ArrayList<String> missingMustGroups){
		if (accessGroups.size() == 0){
			log.info("User does not have access to any group");
			return NO_ACCESS;
		}

		ArrayList<ConfiguredDomLicenseGroupType> accessGroupsType = buildGroups(accessGroups);
		ArrayList<ConfiguredDomLicenseGroupType> missingMustGroupsType = buildGroups(missingMustGroups);

		StringBuilder query = new StringBuilder(); 


		query.append("(("); //Outer around everything
		for (int i = 0; i<accessGroupsType.size(); i++){			
		    String queryPart = accessGroupsType.get(i).getQuery();
		    if (StringUtils.isBlank(queryPart)){ //Hack to allow empty queries.
	               continue; //Skip
	        }
		    		    
		    if (i >0){
				query.append("OR ");
			}

			query.append("(");			
			query.append(queryPart);

			query.append(")");
			if (i <accessGroupsType.size()-1){
				query.append(" ");
			}
		}  
		query.append(")");


		if (missingMustGroupsType.size() == 0){
			return query.toString()+")"; //closing outer
		}

		for (int i = 0; i<missingMustGroupsType.size(); i++){
		    String queryPart = missingMustGroupsType.get(i).getQuery();
		    if (StringUtils.isBlank(queryPart)){ //Hack to allow empty queries.
		       continue; //Skip
		    }
		    
		    query.append(" -(");
			query.append(queryPart);
			query.append(")");
		}

        query.append(")");//closing outer

		return query.toString();
	}

	public static ArrayList<String> getAllPresentationtypeNames(String locale){
		ArrayList<String> allTypes = new ArrayList<String>(); 
	
		ArrayList<ConfiguredDomLicensePresentationType> configuredTypes = LicenseCache.getConfiguredDomLicenseTypes();
		
		for (ConfiguredDomLicensePresentationType current : configuredTypes){
			if (LOCALE_EN.equals(locale)){
				allTypes.add(current.getValue_en());				
			}
			else if (LOCALE_DA.equals(locale)){
				allTypes.add(current.getValue_dk());
			}			
		}		
		return allTypes;
	}
	
	
	public static ArrayList<String> getAllGroupeNames(String locale){
		ArrayList<String> allGroups = new ArrayList<String>(); 
	
		ArrayList<ConfiguredDomLicenseGroupType> configuredGroups = LicenseCache.getConfiguredDomLicenseGroupTypes();
		
		for (ConfiguredDomLicenseGroupType current : configuredGroups){
			if (LOCALE_EN.equals(locale)){
				allGroups.add(current.getValue_en());				
			}
			else if (LOCALE_DA.equals(locale)){
				allGroups.add(current.getValue_dk());
			}			
		}		
		return allGroups;
	}
	public static void validateLocale(String locale){
		if (locale == null){
			return; // okie
		}
		if (!locales.contains(locale)){
			throw new IllegalArgumentException("Unknown locale:"+locale);
		}		
	}
	
	
   //recursive fix group name and presentationtype names to the locale
	public static void fixLocale( ArrayList<UserGroupDTO> input, String locale){
		if (locale == null){
		    locale = LOCALE_DA;
		}
	    
	    for (UserGroupDTO current : input){
			current.setGroupName(LicenseCache.getGroupName(current.getGroupName(), locale));
			ArrayList<String> presentationTypesNames = new ArrayList<String>();
			for (String name :  current.getPresentationTypes()){
				presentationTypesNames.add(LicenseCache.getPresentationtypeName(name, locale));
			}
			current.setPresentationTypes(presentationTypesNames);			 
		}		  
	}
}
