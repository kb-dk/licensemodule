package dk.statsbiblioteket.doms.licensemodule.persistence;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;
import dk.statsbiblioteket.doms.licensemodule.Util;
import dk.statsbiblioteket.doms.licensemodule.service.dto.GetUserGroupsInputDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserGroupDTO;
import dk.statsbiblioteket.doms.licensemodule.service.dto.UserObjAttributeDTO;
import dk.statsbiblioteket.doms.licensemodule.solr.AbstractSolrJClient;
import dk.statsbiblioteket.doms.licensemodule.validation.LicenseValidator;

/*
 * Unittest class for the H2Storage.
 * All tests creates and use H2 database in the directory: target/h2
 * 
 * The directory will be deleted before the first test-method is called.
 * Each test-method will delete all entries in the database, but keep the database tables.
 * 
 * Currently the directory is not deleted after the tests have run. This is useful as you can
 * open and open the database and see what the unit-tests did.
 */

public class LicenseModuleStorageTest {

	private static final Logger log = LoggerFactory.getLogger(LicenseModuleStorageTest.class);
	
    private static final String DRIVER = "org.h2.Driver";
    private static final String URL = "jdbc:h2:target/test-classes/h2/licensemodule";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

	private static final String CREATE_TABLES_DDL_FILE = "src/test/resources/H2_DDL_scripts/dom_licensemodule_create_db.ddl";
	private static final String DELETE_TABLES_DDL_FILE = "src/test/resources/H2_DDL_scripts/delete_from_all_tables.ddl";
	private static final String INSERT_DEFAULT_CONFIGURATION_DDL_FILE = "src/test/resources/H2_DDL_scripts/dom_licensemodule_default_configuration.ddl";
	private static LicenseModuleStorage storage = null;
	

    private static ConfiguredDomLicensePresentationType DOWNLOAD = new  ConfiguredDomLicensePresentationType(1, "Download","Download_dk", "Download_en");
    private static ConfiguredDomLicensePresentationType THUMBNAILS = new  ConfiguredDomLicensePresentationType(1, "Thumbnails" ,"Thumbnails_dk", "Thumbnails_en");



	
	
	/*
	 * Delete database file if it exists. Create database with tables
	 */

	 private static void createEmptyDBFromDDL() throws Exception {
	        //Delete if exists
	        doDelete(new File("target/test-classes/h2"));
	        Connection connection = null;

	        try {
	            Class.forName(DRIVER); // load the driver
	        } catch (ClassNotFoundException e) {
	            throw new SQLException(e);
	        }
	        connection = DriverManager.getConnection(URL, "", "");

	        File file = getFile(CREATE_TABLES_DDL_FILE);
	        log.info("Running DDL script:" + file.getAbsolutePath());

	        if (!file.exists()) {
	            log.error("DDL script not found:" + file.getAbsolutePath());
	            throw new RuntimeException("DDLscript file not found:" + file.getAbsolutePath());
	        }

	        String scriptStatement = "RUNSCRIPT FROM '" + file.getAbsolutePath() + "'";

	        connection.prepareStatement(scriptStatement).execute();

	        PreparedStatement shutdown = connection.prepareStatement("SHUTDOWN");
	        shutdown.execute();
	        connection.close();
	    }


	    @BeforeAll
	    public static void beforeClass() throws Exception {
	      LicenseModulePropertiesLoader.setSOLR_FILTER_FIELD("authID");
	        LicenseModuleStorage.initialize(DRIVER, URL, USERNAME, PASSWORD);	        
	        createEmptyDBFromDDL();	        	        
	    }

	    @AfterAll
	    public static void afterClass() throws Exception {
	        // No reason to delete DB data after test, since we delete it before each test.
	        // This way you can open the DB in a DB-browser after a unittest and see the result.
	        LicenseModuleStorage.shutdown();
	    }

	    /*
	     * Delete data from all tables before each unittest
	     * TODO remove delete when commit is handled in facade class
	     */


	    @BeforeEach
	    public  void before() throws Exception {	    	        
	      Connection connection = null;
	        try {
                Class.forName(DRIVER); // load the driver
            } catch (ClassNotFoundException e) {
                throw new SQLException(e);
            }
            connection = DriverManager.getConnection(URL, "", "");
	        
	        File file = getFile(DELETE_TABLES_DDL_FILE);
            log.info("Running DDL script:" + file.getAbsolutePath());

            if (!file.exists()) {
                log.error("DDL script not found:" + file.getAbsolutePath());
                throw new RuntimeException("DDLscript file not found:" + file.getAbsolutePath());
            }

            String scriptStatement = "RUNSCRIPT FROM '" + file.getAbsolutePath() + "'";

            connection.prepareStatement(scriptStatement).execute();

	        
	        storage = new LicenseModuleStorage();
	        
	    }

	    @AfterEach
	    public void after() throws Exception {
	        storage.rollback();
	        storage.close();
	    }

	
	public static void insertDefaultConfigurationTypes() throws Exception {
		File insert_ddl_file = new File(INSERT_DEFAULT_CONFIGURATION_DDL_FILE);
		storage.runDDLScript(insert_ddl_file);
	}

	@Test
	public void testInsertDomLicensePresentationType() throws Exception {
		String type1 = "unit_test_type1";
		String type1_en = "unit_test_type1_en";
		String type2 = "unit_test_type2";
		storage.persistDomLicensePresentationType("key1",type1,type1_en);
		storage.persistDomLicensePresentationType("key2",type2, "unit_test_type2_en");

		ArrayList<ConfiguredDomLicensePresentationType> list = storage.getDomLicensePresentationTypes();
		assertEquals(2, list.size());
		assertEquals("key1", list.get(0).getKey()); // They are returned in same order they saved (H2 db)
		assertEquals(type1_en, list.get(0).getValue_en()); // They are returned in same order they saved (H2 db)
		assertEquals("key2", list.get(1).getKey());
	}

	@Test
	public void testInsertDomLicenseGroupType() throws Exception {
		String type1Key = "unit_test_key1";
		String type2Key = "unit_test_key2";
		String type1 = "unit_test_type1";
		String type1_en = "unit_test_type1_en";
		String type1_description = "type1_description";
		String type1_description_en = "type1_description:en";
		String type1_query = "type1_query";
		String type2 = "unit_test_type2";
		storage.persistDomLicenseGroupType( type1Key,type1, type1_en,type1_description, type1_description_en,type1_query, false);
		storage.persistDomLicenseGroupType(type2Key,type2, "type_en","type2_description", "description_en","type2_query", false);

		ArrayList<ConfiguredDomLicenseGroupType> list = storage.getDomLicenseGroupTypes();
		assertEquals(2, list.size());
		assertEquals(type1Key, list.get(0).getKey()); // The are return in same order they saved (H2 db)

		assertEquals(type1_description, list.get(0).getDescription_dk());
		assertEquals(type1_query, list.get(0).getQuery());
		assertEquals(type2Key, list.get(1).getKey());

		// update and check it is updated
		ConfiguredDomLicenseGroupType toUpdate = list.get(0);
		String newDescription = "new Description";
		String value_dk = "value_dk";
		String value_en = "value_en";
		storage.updateDomLicenseGroupType(toUpdate.getId(),value_dk, value_en, newDescription, "new description (en)", "new query", true);
		list = storage.getDomLicenseGroupTypes();
		assertEquals(value_en, list.get(0).getValue_en());
		assertEquals(newDescription, list.get(0).getDescription_dk());
	}

	@Test
	public void testInsertDomAttributeType() throws Exception {
		String type1 = "unit_test_type1";
		String type2 = "unit_test_type2";
		storage.persistDomAttributeType(type1);
		storage.persistDomAttributeType(type2);

		ArrayList<ConfiguredDomAttributeType> list = storage.getDomAttributeTypes();
		assertEquals(2, list.size());
		assertEquals(type1, list.get(0).getValue()); // The are return in same order they saved (H2 db)
		assertEquals(type2, list.get(1).getValue());

	}

	@Test
	public void testInsertDefaultConfiguration() throws Exception {
		insertDefaultConfigurationTypes();

	}

	@Test
	public void testDeleteDomAttributeType() throws Exception {
		// create configurationstypes and license using some of the attributestypes
		insertDefaultConfigurationTypes();
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);

		ArrayList<ConfiguredDomAttributeType> list = storage.getDomAttributeTypes();
		assertEquals(11, list.size());
		storage.deleteDomAttributeType("wayf.mail");
		list = storage.getDomAttributeTypes();
		assertEquals(10, list.size()); // only 10 now

		// must not delete since it is used in a license
		try {
			storage.deleteDomAttributeType("wayf.schacHomeOrganization");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		storage.deleteLicense(1L, true);
		storage.deleteDomAttributeType("wayf.schacHomeOrganization"); // now we can delete
		list = storage.getDomAttributeTypes();
		assertEquals(9, list.size()); // only 9 now
	}

	@Test
	public void testDeleteDomGroupType() throws Exception {
		// create configurationstypes and license using some of the grouptypes
		insertDefaultConfigurationTypes();
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);

		ArrayList<ConfiguredDomLicenseGroupType> list = storage.getDomLicenseGroupTypes();
		assertEquals(9, list.size());
		storage.deleteDomLicenseGroupType("Pligtafleveret170Aar");//dom_licensemodule_default_configuration.ddl
		list = storage.getDomLicenseGroupTypes();
		assertEquals(8, list.size()); // only 8 now

		// must not delete since it is used in a license
		try {
			storage.deleteDomLicenseGroupType("TV2");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		storage.deleteLicense(1L, true);
		storage.deleteDomLicenseGroupType("TV2");
		list = storage.getDomLicenseGroupTypes();
		assertEquals(7, list.size()); // only 7 now
	}

	@Test
	public void testDeleteDomPresentationType() throws Exception {
		// create configurationstypes and license using some of the grouptypes
		insertDefaultConfigurationTypes();
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);

		ArrayList<ConfiguredDomLicensePresentationType> list = storage.getDomLicensePresentationTypes();
		assertEquals(5, list.size());
		storage.deleteDomPresentationType("10_sec_stream");
		list = storage.getDomLicensePresentationTypes();
		assertEquals(4, list.size()); // only 4 now

		// must not delete since it is used in a license
		try {
			storage.deleteDomPresentationType("Thumbnails");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		storage.deleteLicense(1L, true);
		storage.deleteDomPresentationType("Thumbnails");
		list = storage.getDomLicensePresentationTypes();
		assertEquals(3, list.size()); // only
	}

	@Test
	public void testGetAllListLicenseNames() throws Exception {
		License license = createTestLicenseWithAssociations(1L);
		license.setLicenseName("name1");
		license.setDescription_dk("description1");
		storage.persistLicense(license);
		license = createTestLicenseWithAssociations(2L);
		license.setLicenseName("name2");
		license.setDescription_dk("description2");
		storage.persistLicense(license);

		ArrayList<License> list = storage.getAllLicenseNames();
		assertEquals(2, list.size());
		license = list.get(0);
		// validate data
		assertEquals("name1", license.getLicenseName());
		assertEquals("description1", license.getDescription_dk());

		// TODO validate dates
	}

	@Test
	public void testPersistLicenseWithAssocations() throws Exception {
		insertDefaultConfigurationTypes();
		
		// Full persistence and load test of associations
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);
		ArrayList<License> list = storage.getAllLicenseNames();
		assertEquals(1, list.size());
		long id = list.get(0).getId();
		license = storage.getLicense(id);
		license.getAttributeGroups();
		// TODO test all associations
	}

	@Test
	public void testDeleteLicense() throws Exception {

		// Full persistence and load test of associations
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);
		// test it is created
		ArrayList<License> list = storage.getAllLicenseNames();
		assertEquals(1, list.size());

		storage.deleteLicense(list.get(0).getId(), true);

		list = storage.getAllLicenseNames();
		assertEquals(0, list.size());

		// DB inspection showed all associations was also deleted correct
	}

	@Test
	public void testPersistAttributeGroupsForLicense() throws Exception {

		long licenseId = 1L;
		License license = createTestLicenseWithAssociations(1L);
		storage.persistAttributeGroupsForLicense(licenseId, license.getAttributeGroups(), true);

		ArrayList<AttributeGroup> attributeGroups = storage.getAttributeGroupsForLicense(licenseId);
		assertEquals(3, attributeGroups.size());
		assertEquals(1, attributeGroups.get(0).getNumber());
		assertEquals(2, attributeGroups.get(1).getNumber());
		assertEquals(3, attributeGroups.get(2).getNumber());
	}

	@Test
	public void testPersistLicenseContentForLicense() throws Exception {
		long licenseId = 1L;

		License license = createTestLicenseWithAssociations(1L);

		storage.persistLicenseContentsForLicense(licenseId, license.getLicenseContents(), true);
		ArrayList<LicenseContent> licenseContents = storage.getLicenseContentsForLicense(licenseId);
		assertEquals(2, licenseContents.size());
		assertEquals("TV2", licenseContents.get(0).getName());
		assertEquals("DR1", licenseContents.get(1).getName());
	}

	@Test
	public void testPersistAttributesForAttributeGroup() throws Exception {

		License license = createTestLicenseWithAssociations(1L);
		long attributeGroupId = 1L;

		storage.persistAttributesForAttributeGroup(attributeGroupId, license.getAttributeGroups().get(0).getAttributes(), true);

		ArrayList<Attribute> attributes = storage.getAttributesForAttributeGroup(attributeGroupId);
		assertEquals(2, attributes.size());
		assertEquals("wayf.schacHomeOrganization", attributes.get(0).getAttributeName());
		assertEquals("wayf.eduPersonPrimaryAffiliation", attributes.get(1).getAttributeName());
	}

	@Test
	public void testPersistValuesForAttribute() throws Exception {

		long attributeId = 1L;
		ArrayList<AttributeValue> values = new ArrayList<AttributeValue>();
		values.add(new AttributeValue("value1"));
		values.add(new AttributeValue("value2"));
		values.add(new AttributeValue("value3"));
		storage.persistValuesForAttribute(attributeId, values, true);

		values = storage.getValuesForAttribute(attributeId);
		assertEquals(3, values.size());
		assertEquals("value1", values.get(0).getValue());
	}

	@Test
	public void testDateFormat() throws Exception {
		boolean valid = Util.validateDateFormat("13-01-2012");
		assertEquals(true, valid);
		valid = Util.validateDateFormat("31-01-2012");
		assertEquals(true, valid);

		valid = Util.validateDateFormat("30-02-2012"); // feb 30. does not exist
		assertEquals(false, valid);

		valid = Util.validateDateFormat("01-15-2012"); // month 15
		assertEquals(false, valid);

		valid = Util.validateDateFormat("01-15-12"); // year must be 4 digits
		assertEquals(false, valid);

		valid = Util.validateDateFormat("10-11-2x12");
		assertEquals(false, valid);
	}

	@Test
	public void testFilterLicenseByValidDate() throws Exception {
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);
		ArrayList<License> licenses = LicenseCache.getAllLicense();
		assertEquals(1, licenses.size());

// License has validFrom=27-12-2012 and validTo=27-12-2013
long testDate = Util.convertDateFormatToLong("26-12-2012");// Before period
assertEquals(0, LicenseValidator.filterLicenseByValidDate(licenses, testDate).size());

testDate = Util.convertDateFormatToLong("27-12-2012");// just valid (start date)
assertEquals(1, LicenseValidator.filterLicenseByValidDate(licenses, testDate).size());

testDate = Util.convertDateFormatToLong("26-12-2023");// just valid (last valid date)
assertEquals(1, LicenseValidator.filterLicenseByValidDate(licenses, testDate).size());

testDate = Util.convertDateFormatToLong("27-12-2999");// just expired
assertEquals(0, LicenseValidator.filterLicenseByValidDate(licenses, testDate).size());

testDate = Util.convertDateFormatToLong("28-12-2999");// expired (1 day)
assertEquals(0, LicenseValidator.filterLicenseByValidDate(licenses, testDate).size());
	}

	@Test
	public void testAllLicesesCache() throws Exception {
		License license = createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);

		ArrayList<License> licenses = LicenseCache.getAllLicense();
		assertEquals(1, licenses.size());
	}

	

	// This License is used for most unittests, so it is important to understand the object tree.
	public static License createTestLicenseWithAssociations(long id) {
		License license = new License();
		license.setId(id);
		license.setLicenseName("Dighumlab adgang");
		license.setDescription_dk("info of hvem licensen vedr. og hvad der er adgang til");
		license.setDescription_en("engelsk beskrivelse..");
		license.setValidFrom("27-12-2012");
		license.setValidTo("27-12-2999");

		ArrayList<AttributeGroup> groups = new ArrayList<AttributeGroup>();
		AttributeGroup group1 = new AttributeGroup(1);
		AttributeGroup group2 = new AttributeGroup(2);
		AttributeGroup group3 = new AttributeGroup(3);
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		license.setAttributeGroups(groups);

		ArrayList<Attribute> group1_attributes = new ArrayList<Attribute>();
		group1.setAttributes(group1_attributes);
		Attribute group1_attribute1 = new Attribute();
		group1_attributes.add(group1_attribute1);
		group1_attribute1.setAttributeName("wayf.schacHomeOrganization");
		ArrayList<AttributeValue> group1_attribute1_values = new ArrayList<AttributeValue>();
		group1_attribute1.setValues(group1_attribute1_values);
		group1_attribute1_values.add(new AttributeValue("au.dk"));

		Attribute group1_attribute2 = new Attribute();
		group1_attributes.add(group1_attribute2);
		group1_attribute2.setAttributeName("wayf.eduPersonPrimaryAffiliation");
		ArrayList<AttributeValue> group1_attribute2_values = new ArrayList<AttributeValue>();
		group1_attribute2.setValues(group1_attribute2_values);
		group1_attribute2_values.add(new AttributeValue("student"));
		group1_attribute2_values.add(new AttributeValue("staff"));

		ArrayList<Attribute> group2_attributes = new ArrayList<Attribute>();
		group2.setAttributes(group2_attributes);
		Attribute group2_attribute1 = new Attribute();
		group2_attributes.add(group2_attribute1);
		group2_attribute1.setAttributeName("wayf.eduPersonPrimaryAffiliation");
		ArrayList<AttributeValue> group2_attribute1_values = new ArrayList<AttributeValue>();
		group2_attribute1.setValues(group2_attribute1_values);
		group2_attribute1_values.add(new AttributeValue("student"));

		Attribute group2_attribute2 = new Attribute();
		group2_attributes.add(group2_attribute2);
		group2_attribute2.setAttributeName("ip_role_mapper.SBIPRoleMapper");
		ArrayList<AttributeValue> group2_attribute2_values = new ArrayList<AttributeValue>();
		group2_attribute2.setValues(group2_attribute2_values);
		group2_attribute2_values.add(new AttributeValue("in_house"));

		ArrayList<Attribute> group3_attributes = new ArrayList<Attribute>();
		group3.setAttributes(group3_attributes);
		Attribute group3_attribute1 = new Attribute();
		group3_attributes.add(group3_attribute1);
		group3_attribute1.setAttributeName("attribut_store.MediestreamFullAccess");
		ArrayList<AttributeValue> group3_attribute1_values = new ArrayList<AttributeValue>();
		group3_attribute1.setValues(group3_attribute1_values);
		group3_attribute1_values.add(new AttributeValue("yes"));

		ArrayList<LicenseContent> licenseContents = new ArrayList<LicenseContent>();
		LicenseContent licenseContent1 = new LicenseContent();
		LicenseContent licenseContent2 = new LicenseContent();
		licenseContent1.setName("TV2");
		licenseContent2.setName("DR1");		
		licenseContents.add(licenseContent1);
		licenseContents.add(licenseContent2);

		ArrayList<Presentation> presentations1 = new ArrayList<Presentation>();
		ArrayList<Presentation> presentations2 = new ArrayList<Presentation>();
		presentations1.add(new Presentation("Stream"));
		presentations1.add(new Presentation("Download"));
		presentations2.add(new Presentation("Thumbnails"));
		licenseContent1.setPresentations(presentations1);
		licenseContent2.setPresentations(presentations2);

		license.setLicenseContents(licenseContents);

		return license;
	}

	@Test
	public void testGetUserGroupsWithPresentation() throws Exception {
       insertDefaultConfigurationTypes();
		
		License license = LicenseModuleStorageTest.createTestLicenseWithAssociations(1L);
		storage.persistLicense(license);

		GetUserGroupsInputDTO input = new GetUserGroupsInputDTO();

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

		input.setAttributes(userObjAttributes);
		input.setLocale("da");
		ArrayList<UserGroupDTO> usersGroups = LicenseValidator.getUsersGroups(input);		
		//Test danish names
		UserGroupDTO group1 = usersGroups.get(0);
		assertEquals("DR 1 TV",group1.getGroupName());
		assertEquals("Thumbnails_dk", group1.getPresentationTypes().get(0));
		
		input.setLocale("en");		
		usersGroups = LicenseValidator.getUsersGroups(input);
		
		//Test english names
		 group1 = usersGroups.get(0);
		assertEquals("English text",group1.getGroupName());
		assertEquals("Thumbnails_en", group1.getPresentationTypes().get(0));		

	}


	// This License is used for most unittests, so it is important to understand the object tree.
	// Attribute: wayf.schacHomeOrganization and value: au.dk
	// Individuelt forbud:Stream, Thumbnails , 10_sec_stream , Download
	// Klausuleret: Stream, Thumbnails , 10_sec_stream (BEMÆRK denne ikke har Download)
	// TV2 TV: Stream, Thumbnails , 10_sec_stream , Download
	public static License createTestSimpleMustGroupsLicenseWithAssociations() {
		License license = new License();
		license.setId(2);
		license.setLicenseName("Dighumlab adgang");
		license.setDescription_dk("info of hvem licensen vedr. og hvad der er adgang til");
		license.setDescription_en("engelsk beskrivelse");
		license.setValidFrom("16-10-2012");
		license.setValidTo("16-10-2013");

		ArrayList<AttributeGroup> groups = new ArrayList<AttributeGroup>();
		AttributeGroup group1 = new AttributeGroup(1);
		groups.add(group1);
		license.setAttributeGroups(groups);

		ArrayList<Attribute> group1_attributes = new ArrayList<Attribute>();
		group1.setAttributes(group1_attributes);
		Attribute group1_attribute1 = new Attribute();
		group1_attributes.add(group1_attribute1);
		group1_attribute1.setAttributeName("wayf.schacHomeOrganization");
		ArrayList<AttributeValue> group1_attribute1_values = new ArrayList<AttributeValue>();
		group1_attribute1.setValues(group1_attribute1_values);
		group1_attribute1_values.add(new AttributeValue("au.dk"));

		ArrayList<LicenseContent> licenseContents = new ArrayList<LicenseContent>();
		LicenseContent licenseContent1 = new LicenseContent();
		LicenseContent licenseContent2 = new LicenseContent();
		LicenseContent licenseContent3 = new LicenseContent();
		licenseContent1.setName("IndividueltForbud"); // Must
		licenseContent2.setName("Klausuleret"); // Must
		licenseContent3.setName("TV2"); // ikke must

		licenseContents.add(licenseContent1);
		licenseContents.add(licenseContent2);
		licenseContents.add(licenseContent3);

		ArrayList<Presentation> presentations1 = new ArrayList<Presentation>();
		ArrayList<Presentation> presentations2 = new ArrayList<Presentation>();
		ArrayList<Presentation> presentations3 = new ArrayList<Presentation>();
		presentations1.add(new Presentation("Stream"));
		presentations1.add(new Presentation("Thumbnails"));
		presentations1.add(new Presentation("10_sec_stream"));
		presentations1.add(new Presentation("Download"));
		presentations2.add(new Presentation("Stream"));
		presentations2.add(new Presentation("Thumbnails"));
		presentations2.add(new Presentation("10_sec_stream"));
		// presentations2.add(new Presentation("Download")); for at vise denne ikke med...
		presentations3.add(new Presentation("Stream"));
		presentations3.add(new Presentation("Thumbnails"));
		presentations3.add(new Presentation("10_sec_stream"));
		presentations3.add(new Presentation("Download"));

		licenseContent1.setPresentations(presentations1);
		licenseContent2.setPresentations(presentations2);
		licenseContent3.setPresentations(presentations3);
		license.setLicenseContents(licenseContents);

		return license;
	}

    /**
    * Multi protocol resource loader. Primary attempt is direct file, secondary is classpath resolved to File.
    *
    * @param resource a generic resource.
    * @return a File pointing to the resource.
    */
   private static File getFile(String resource) throws IOException {
       File directFile = new File(resource);
       if (directFile.exists()) {
           return directFile;
       }
       URL classLoader = Thread.currentThread().getContextClassLoader().getResource(resource);
       if (classLoader == null) {
           throw new FileNotFoundException("Unable to locate '" + resource + "' as direct File or on classpath");
       }
       String fromURL = classLoader.getFile();
       if (fromURL == null || fromURL.isEmpty()) {
           throw new FileNotFoundException("Unable to convert URL '" + fromURL + "' to File");
       }
       return new File(fromURL);
   }
   

   @Test
   public void testfilterUserObjAttributesToValidatedOnly() throws Exception {
       License license = LicenseModuleStorageTest.createTestLicenseWithAssociations(1L);


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
       License license = LicenseModuleStorageTest.createTestLicenseWithAssociations(1L);
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
       License license = LicenseModuleStorageTest.createTestLicenseWithAssociations(1L);
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
       License license = LicenseModuleStorageTest.createTestLicenseWithAssociations(1L);
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
     insertDefaultConfigurationTypes();
     LicenseCache.reloadCache(); //The buildGroup and FilterMustGroup uses cache for performance
              
      ArrayList<String> groups = new ArrayList<String>(); 
       
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
       
       //1 group that does not exist in DB
       groups = new ArrayList<String>(); 
       groups.add("does not exist");       
       try{
           LicenseValidator.buildGroups(groups);
           fail();
       }   
       catch (IllegalArgumentException e){
         //expected
       }
       
   }

   @Test
   public void testFilterLicensesWithGroupNamesAndPresentationTypeNoMustGroup() throws Exception {

       ArrayList<License> licenses = new ArrayList<License>(); 
       licenses.add(LicenseModuleStorageTest.createTestLicenseWithAssociations(1L));

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
       licenses.add(LicenseModuleStorageTest.createTestSimpleMustGroupsLicenseWithAssociations());
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
       licenses.add(LicenseModuleStorageTest.createTestSimpleMustGroupsLicenseWithAssociations());


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
       License l = LicenseModuleStorageTest.createTestLicenseWithAssociations(1);
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
       License l2 =  LicenseModuleStorageTest.createTestLicenseWithAssociations(1);
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
       String solrIdsQuery = AbstractSolrJClient.makeAuthIdPart(ids);
       assertEquals("(authID:\"testId1\" OR authID:\"testId2\")", solrIdsQuery); 

       //prevent Lucene query injection. Remove all " and / from the string
       ids = new ArrayList<String>(); 
       ids.add("test\"Id3\\");

       solrIdsQuery = AbstractSolrJClient.makeAuthIdPart(ids); 
       
       assertEquals("(authID:\"testId3\")", solrIdsQuery);                 
   }

   
   
   //file.delete does not work for a directory unless it is empty. hence this method
   private static void doDelete(File path) throws IOException
   {
       if (path.isDirectory()) {
           for (File child : path.listFiles()) {
               doDelete(child);
           }
       }
       if (!path.delete()) {
        //ignore.
       }
   }
	
}
