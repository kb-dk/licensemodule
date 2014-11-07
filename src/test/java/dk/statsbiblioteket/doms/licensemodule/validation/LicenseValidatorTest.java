package dk.statsbiblioteket.doms.licensemodule.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import dk.statsbiblioteket.doms.licensemodule.persistence.Attribute;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.H2StorageTest;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseContent;
import dk.statsbiblioteket.doms.licensemodule.persistence.Presentation;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserGroupDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserObjAttributeDTO;
import dk.statsbiblioteket.doms.licensemodule.solr.DomsSolrJClient;


public class LicenseValidatorTest {

	private static ConfiguredDomLicensePresentationType DOWNLOAD = new  ConfiguredDomLicensePresentationType(1, "Download","Download_dk", "Download_en");
	private static ConfiguredDomLicensePresentationType THUMBNAILS = new  ConfiguredDomLicensePresentationType(1, "Thumbnails" ,"Thumbnails_dk", "Thumbnails_en");


	@BeforeClass
	public static void beforeClass() throws Exception {
		//Create database before unittests. Only do in start of test  all tests only load data.   
		H2StorageTest.beforeClass();
		H2StorageTest.insertDefaultConfigurationTypes();
	}

	@Test
	public void testfilterUserObjAttributesToValidatedOnly() throws Exception {
		License license = H2StorageTest.createTestLicenseWithAssociations(1L);


		//Attribute: wayf.schacHomeOrganization and values: au.dk
		//UserObj: xxx_wayf.schacHomeOrganization and values: au.dk
		//result: Not match

		ArrayList<UserObjAttributeDTO> userObjAttributes = new ArrayList<UserObjAttributeDTO >(); 

		UserObjAttributeDTO newUserObjAtt = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt);
		Attribute attribute = license.getAttributeGroups().get(0).getAttributes().get(0);		
		newUserObjAtt.setAttribute("xxx_wayf.schacHomeOrganization");
		ArrayList<String> values = new ArrayList<String>();
		values.add("au.dk");
		newUserObjAtt.setValues(values);
		ArrayList<UserObjAttributeDTO> filtered = LicenseValidator.filterUserObjAttributesToValidatedOnly(attribute, userObjAttributes);
		assertEquals(0,filtered.size());


		//Attribute: wayf.schacHomeOrganization and values: au.dk
		//UserObj: wayf.schacHomeOrganization and values: au.dk
		//result: Match
		userObjAttributes = new ArrayList<UserObjAttributeDTO >();
		newUserObjAtt = new UserObjAttributeDTO();		
		userObjAttributes.add(newUserObjAtt);
		newUserObjAtt.setAttribute("wayf.schacHomeOrganization");
		values = new ArrayList<String>();
		values.add("au.dk");
		newUserObjAtt.setValues(values);
		filtered = LicenseValidator.filterUserObjAttributesToValidatedOnly(attribute, userObjAttributes);
		assertEquals(1,filtered.size());
		assertEquals("wayf.schacHomeOrganization", filtered.get(0).getAttribute());
		assertEquals(1, filtered.get(0).getValues().size());
		assertEquals("au.dk", filtered.get(0).getValues().get(0)); //only value au.dk				

		//Attribute: wayf.schacHomeOrganization and values: au.dk
		//UserObj: wayf.schacHomeOrganization and values: sb.dk test.dk
		//result: not match
		userObjAttributes = new ArrayList<UserObjAttributeDTO >();
		newUserObjAtt = new UserObjAttributeDTO();		
		userObjAttributes.add(newUserObjAtt);
		newUserObjAtt.setAttribute("wayf.schacHomeOrganization");
		values = new ArrayList<String>();
		values.add("sb.dk");
		values.add("test.dk");	
		newUserObjAtt.setValues(values);
		filtered = LicenseValidator.filterUserObjAttributesToValidatedOnly(attribute, userObjAttributes);
		assertEquals(0,filtered.size());

		//
		//Attribute:wayf.eduPersonPrimaryAffiliation and values: student , staff
		//UserObj: wayf.eduPersonPrimaryAffiliation and values: staff
		//result: match

		attribute = license.getAttributeGroups().get(0).getAttributes().get(1);	
		userObjAttributes = new ArrayList<UserObjAttributeDTO >();
		newUserObjAtt = new UserObjAttributeDTO();		
		userObjAttributes.add(newUserObjAtt);
		newUserObjAtt.setAttribute("wayf.eduPersonPrimaryAffiliation");
		values = new ArrayList<String>();
		values.add("staff");		
		newUserObjAtt.setValues(values);
		filtered = LicenseValidator.filterUserObjAttributesToValidatedOnly(attribute, userObjAttributes);
		assertEquals(1,filtered.size());
		assertEquals("wayf.eduPersonPrimaryAffiliation", filtered.get(0).getAttribute());
		assertEquals(1, filtered.get(0).getValues().size());
		assertEquals("staff", filtered.get(0).getValues().get(0));			
	}

	@Test
	public void testValidateAccess1() throws Exception {
		/* Access must be true. This is match from attributeGroup 1 in the license
		 * 
		 * wayf.schacHomeOrganization  with values: au.dk
		 * wayf.eduPersonPrimaryAffiliation with values: staff
		 */				
		License license = H2StorageTest.createTestLicenseWithAssociations(1L);
		ArrayList<License> allLicenses= new ArrayList<License>();
		allLicenses.add(license);

		ArrayList<UserObjAttributeDTO> userObjAttributes = new ArrayList<UserObjAttributeDTO>(); 		
		UserObjAttributeDTO newUserObjAtt1 = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt1);
		UserObjAttributeDTO newUserObjAtt2 = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt2);

		newUserObjAtt1.setAttribute("wayf.schacHomeOrganization");
		newUserObjAtt2.setAttribute("wayf.eduPersonPrimaryAffiliation");

		ArrayList<String> values1 = new ArrayList<String>();
		values1.add("au.dk");
		newUserObjAtt1.setValues(values1);

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add("staff");
		newUserObjAtt2.setValues(values2);

		//Must validate		
		ArrayList<License> licenses = LicenseValidator.findLicensesValidatingAccess(userObjAttributes, allLicenses);
		assertEquals(1,licenses.size());
	}

	@Test
	public void testValidateAccess2() throws Exception {
		/* Access must be true. This is match from attributeGroup 1 in the license
		 * It is only matching part1 of 2 parts in attributegroup 1
		 * 
		 * wayf.schacHomeOrganization  with values: au.dk
		 * 
		 */				
		License license = H2StorageTest.createTestLicenseWithAssociations(1L);
		ArrayList<License> allLicenses= new ArrayList<License>();
		allLicenses.add(license);

		ArrayList<UserObjAttributeDTO> userObjAttributes = new ArrayList<UserObjAttributeDTO>(); 		
		UserObjAttributeDTO newUserObjAtt1 = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt1);

		newUserObjAtt1.setAttribute("wayf.schacHomeOrganization");


		ArrayList<String> values1 = new ArrayList<String>();
		values1.add("au.dk");
		newUserObjAtt1.setValues(values1);

		// Does not validate
		ArrayList<License> licenses = LicenseValidator.findLicensesValidatingAccess(userObjAttributes, allLicenses);
		assertEquals(0,licenses.size());

	}

	@Test
	public void testValidateAccess3() throws Exception {
		/* Access must be true. This is match from both attributeGroup 1 and 3 in the license. (so 'double' access)
		 * 
		 * wayf.schacHomeOrganization  with values: au.dk
		 * wayf.eduPersonPrimaryAffiliation with values: staff
		 * attribut_store.MediestreamFullAccess with values : yes
		 *
		 */				
		License license = H2StorageTest.createTestLicenseWithAssociations(1L);
		ArrayList<License> allLicenses= new ArrayList<License>();
		allLicenses.add(license);

		ArrayList<UserObjAttributeDTO> userObjAttributes = new ArrayList<UserObjAttributeDTO>(); 		
		UserObjAttributeDTO newUserObjAtt1 = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt1);
		UserObjAttributeDTO newUserObjAtt2 = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt2);
		UserObjAttributeDTO newUserObjAtt3 = new UserObjAttributeDTO();
		userObjAttributes.add(newUserObjAtt3);


		newUserObjAtt1.setAttribute("wayf.schacHomeOrganization");
		newUserObjAtt2.setAttribute("wayf.eduPersonPrimaryAffiliation");
		newUserObjAtt3.setAttribute("attribut_store.MediestreamFullAccess");

		ArrayList<String> values1 = new ArrayList<String>();
		values1.add("au.dk");
		newUserObjAtt1.setValues(values1);

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add("staff");
		newUserObjAtt2.setValues(values2);


		ArrayList<String> values3 = new ArrayList<String>();
		values3.add("yes");
		newUserObjAtt3.setValues(values3);

		//Must validate		
		ArrayList<License> licenses = LicenseValidator.findLicensesValidatingAccess(userObjAttributes, allLicenses);
		assertEquals(1,licenses.size());
	}

	@Test
	public void testFilterMustGroups() throws Exception {

		//1 group that does not exist in DB
		ArrayList<String> groups = new ArrayList<String>(); 
		groups.add("does not exist");		
		try{
			LicenseValidator.buildGroups(groups);
			fail();
		}	
		catch (IllegalArgumentException e){
			//expected
		}


		//2 groups that does exist, but are not must groups
		groups = new ArrayList<String>(); 
		groups.add("Pligtafleveret170Aar");
		groups.add("DRRadio");		
		ArrayList<ConfiguredDomLicenseGroupType> buildGroups = LicenseValidator.buildGroups(groups);
		assertEquals(2,buildGroups.size());
		ArrayList<ConfiguredDomLicenseGroupType> filtered = LicenseValidator.filterMustGroups(buildGroups);
		assertEquals(0, filtered.size());


		//2 groups that does exist,one is a MUST group
		groups = new ArrayList<String>(); 
		groups.add("Pligtafleveret170Aar");
		groups.add("IndividueltForbud");		
		buildGroups = LicenseValidator.buildGroups(groups);
		assertEquals(2,buildGroups.size());
		filtered = LicenseValidator.filterMustGroups(buildGroups);

		assertEquals(1, filtered.size());
		assertEquals("IndividueltForbud", filtered.get(0).getKey());		

		//3 groups that does exist,two are a MUST groups
		groups = new ArrayList<String>(); 
		groups.add("Pligtafleveret170Aar");
		groups.add("IndividueltForbud");		
		groups.add("Klausuleret");

		buildGroups = LicenseValidator.buildGroups(groups);
		assertEquals(3,buildGroups.size());
		filtered = LicenseValidator.filterMustGroups(buildGroups);		
		assertEquals(2, filtered.size());
	}

	@Test
	public void testFilterLicensesWithGroupNamesAndPresentationTypeNoMustGroup() throws Exception {

		ArrayList<License> licenses = new ArrayList<License>(); 
		licenses.add(H2StorageTest.createTestLicenseWithAssociations(1L));

		//'Reklamefilm' not marked for this license
		ConfiguredDomLicenseGroupType group1 = new ConfiguredDomLicenseGroupType(1L,"Reklamefilm","Reklamefilm","Reklamefilm_en","","","",false);
		ArrayList<ConfiguredDomLicenseGroupType> groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		ArrayList<License> filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeNoMustGroup(licenses, groups, DOWNLOAD);
		assertEquals(0,filtered.size());

		//'TV2 TV' is marked, but not for presentationtype images
		group1 = new ConfiguredDomLicenseGroupType(1L,"TV2","TV2 TV","TV2 TV_EN","","","",false);
		groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeNoMustGroup(licenses, groups, THUMBNAILS);
		assertEquals(0,filtered.size());


		//'TV2 TV' is marked with Download allowed
		group1 = new ConfiguredDomLicenseGroupType(1L,"TV2","TV2 TV","TV2 TV_en","","","",false);
		groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeNoMustGroup(licenses, groups, DOWNLOAD);
		assertEquals(1,filtered.size()); //license validated.
	}	



	@Test
	public void testGetUserGroupsWithPresentationTypes() throws Exception {
		//For this test notice the presentationtypes are loaded from the DB, only the names from input is used
		ArrayList<License> licenses = new ArrayList<License>(); 
		licenses.add(H2StorageTest.createTestSimpleMustGroupsLicenseWithAssociations());
		ArrayList<String> presentationTypes = new ArrayList<String>();
		presentationTypes.add("Download");

		ArrayList<String> groups = LicenseValidator.filterGroups(licenses,presentationTypes);
		assertEquals(2,groups.size());
		assertTrue(groups.contains("IndividueltForbud"));
		assertTrue(groups.contains("TV2"));	

		presentationTypes.add("Thumbnails"); //now both Download and Thumbnails

		groups = LicenseValidator.filterGroups(licenses,presentationTypes);
		assertEquals(3,groups.size());				
	}

	@Test
	public void testFilterLicensesWithGroupNamesAndPresentationTypeMustGroup() throws Exception {
		//For this test notice the presentationtypes are loaded from the DB, only the names from input is used
		ArrayList<License> licenses = new ArrayList<License>(); 
		licenses.add(H2StorageTest.createTestSimpleMustGroupsLicenseWithAssociations());


		//access, 1 must group which
		ConfiguredDomLicenseGroupType group1 = new ConfiguredDomLicenseGroupType(1L,"IndividueltForbud","Individuelt forbud", "Individuelt forbud_en","","","",true);				
		ArrayList<ConfiguredDomLicenseGroupType> groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		ArrayList<License> filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeMustGroup(licenses, groups, DOWNLOAD);
		assertEquals(1,filtered.size());

		//NOT access, 1 must group that it found, but not with presentationtype Download
		group1 = new ConfiguredDomLicenseGroupType(1L,"Klausuleret","Klausuleret_dk","Klausleret_en","","","",true);				
		groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeMustGroup(licenses, groups, DOWNLOAD);
		assertEquals(0,filtered.size());

		//access, 2 must groups which both have presentation type images
		group1 = new ConfiguredDomLicenseGroupType(1L,"IndividueltForbud","Individuelt forbud_dk","Individuelt forbud_en","","","",true);				
		ConfiguredDomLicenseGroupType group2 = new ConfiguredDomLicenseGroupType(2L,"Klausuleret","Klausuleret_dk","Klausuleret_en","","","",true);
		groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		groups.add(group2);
		filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeMustGroup(licenses, groups, THUMBNAILS);
		assertEquals(1,filtered.size());

		//NOT access, 2 must groups but one of the missing presentationtype Download
		group1 = new ConfiguredDomLicenseGroupType(1L,"Individuelt forbud","Individuelt forbud_dk","Klausuleret forbud_en","","","",true);				
		group2 = new ConfiguredDomLicenseGroupType(2L,"Klausuleret","Klausuleret_dk","Klausuleret_en","","","",true);
		groups = new ArrayList<ConfiguredDomLicenseGroupType>();
		groups.add(group1);		
		groups.add(group2);
		filtered = LicenseValidator.filterLicensesWithGroupNamesAndPresentationTypeMustGroup(licenses, groups, DOWNLOAD);
		assertEquals(0,filtered.size());
	}	

	@Test
	public void testMatchPresentationtype() throws Exception {

		try{
			LicenseValidator.matchPresentationtype("does not exist");
			fail();
		}
		catch (IllegalArgumentException e){
			//Expected
		}

		ConfiguredDomLicensePresentationType downloadType = LicenseValidator.matchPresentationtype("Download");
		assertEquals("Download", downloadType.getKey());			
	}

	@Test
	public void testGenerateQueryString() throws Exception {
		ArrayList<String> groups = new ArrayList<String>();
		ArrayList<String> missingMustGroups = new ArrayList<String>();

		//2 in each group
		groups.add("Reklamefilm");
		groups.add("DRRadio");
		missingMustGroups.add("IndividueltForbud");
		missingMustGroups.add("Klausuleret");

		String query = LicenseValidator.generateQueryString(groups, missingMustGroups);
		assertEquals("(((group:\"reklamefilm\") OR (group:\"DRRadio\")) -(group:\"individuelt\") -(group:\"klausuleret\"))", query);		

		//only 2 in accessgroups
		missingMustGroups = new ArrayList<String>(); 
		query = LicenseValidator.generateQueryString(groups, missingMustGroups);
		assertEquals("(((group:\"reklamefilm\") OR (group:\"DRRadio\")))", query);						


		//Test noaccess
		query = LicenseValidator.generateQueryString(new ArrayList<String>(), missingMustGroups);
		assertEquals(LicenseValidator.NO_ACCESS, query);
	}

	@Test
	public void testFilterGroupsWithPresentationtype() throws Exception {
		License l = H2StorageTest.createTestLicenseWithAssociations(1);
		ArrayList<License> list = new ArrayList<License>();
		list.add(l);
		//Easy, just 1 license
		//DR1: Thumbnails
		//TV2: Stream , Download

		ArrayList<UserGroupDTO> filtered = LicenseValidator.filterGroupsWithPresentationtype(list);
		assertEquals(2, filtered.size());
		UserGroupDTO group1 = filtered.get(0);
		UserGroupDTO group2 = filtered.get(1);     
		assertEquals("DR1",group1.getGroupName());
		assertEquals(1,group1.getPresentationTypes().size());
		assertEquals("TV2",group2.getGroupName());
		assertEquals(2,group2.getPresentationTypes().size());

		//add another license
		License l2 =  H2StorageTest.createTestLicenseWithAssociations(1);
		LicenseContent c = new LicenseContent("TV3");
		ArrayList<Presentation> p_list = new ArrayList<Presentation>();
		c.setPresentations(p_list);
		l2.getLicenseContents().add(c);
		l2.setLicenseName("l2");
		p_list.add(new Presentation("10_sec_stream"));
		l2.getLicenseContents().get(0).getPresentations().add(new Presentation("10_sec_stream")); // new for group TV 2 TV

		//Situation now
		//DR 1 TV: Thumbnails, 10_sec_stream
		//TV 2 TV: Stream , Download
		//TV3: 10_sec_stream
		list.add(l2);


		ArrayList<UserGroupDTO> filtered2 = LicenseValidator.filterGroupsWithPresentationtype(list);
		assertEquals(3, filtered2.size());

		UserGroupDTO group3 = filtered2.get(2);
		assertEquals("TV3",group3.getGroupName());

		assertEquals(1,group3.getPresentationTypes().size()); 
		
		group2 = filtered2.get(1);		
		assertEquals("TV2",group2.getGroupName());
		assertEquals(3,group2.getPresentationTypes().size());     
	}

	@Test
	public void testMakeAuthIdPart() throws Exception {

		ArrayList<String> ids = new ArrayList<String>(); 
		ids.add("testId1");
		ids.add("testId2");
		String solrIdsQuery = DomsSolrJClient.makeAuthIdPart(ids);
		//(recordID:"testId1" OR recordID:"testId2")
		assertEquals("(authID:\"testId1\" OR authID:\"testId2\")", solrIdsQuery); 

		//prevent Lucene query injection. Remove all " and / from the string
		ids = new ArrayList<String>(); 
		ids.add("test\"Id3\\");

		solrIdsQuery = DomsSolrJClient.makeAuthIdPart(ids);	
		//(recordID:"testog")
		assertEquals("(authID:\"testId3\")", solrIdsQuery);					
	}



}
