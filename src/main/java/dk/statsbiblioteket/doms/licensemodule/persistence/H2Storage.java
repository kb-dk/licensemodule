package dk.statsbiblioteket.doms.licensemodule.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.h2.util.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all communication with the DB. Singleton pattern. From unit-test the test-class initializes the singleton - on Tomcat it is initialized by
 * InitializationContextListener.
 * 
 * For performance the DB can be accessed through the LicenseCache-class that cache the complete DB-tables. The cache has a 15 minute reload timer, but can also
 * instant reload when implicit notified by the DB-methods in this class.
 * 
 * The DB consist of the following tables:
 * 
 * 3 tables for configuration: PRESENTATIONTYPES: configured presentationtypes. GROUPTYPES: configured groups ATTRIBUTETYPES: configured attributes
 * 
 * The following tables to store created licenses: 
 * LICENSE (top parent)
 * ATTRIBUTEGROUP (parent=LICENSE) 
 * ATTRIBUTE (parent = ATTRIBUTEGROUP)
 * VALUE (parent = ATTRIBUTE)
 * LICENSECONTENT (parent = LICENSE)
 * PRESENTATION (parent = LICENSECONTENT)
 * 
 */
public class H2Storage {

	private static final Logger log = LoggerFactory.getLogger(H2Storage.class);
	private static H2Storage instance = null;

	private static Connection singleDBConnection = null;
	private static Profiler profiler = null;
	private long lastTimestamp = 0; // Remember last timestamp and make sure each is only used once;

	// statistics shown on monitor.jsp page
	public static Date INITDATE = null;
	public static Date LASTDATABASEBACKUPDATE = null;
	public static int NUMBER_DATABASE_BACKUP_SINCE_STARTUP = 0;

	// Table and column names
	private static final String DOMLICENSEPRESENTATIONTYPES_TABLE = "PRESENTATIONTYPES";
	private static final String DOMLICENSEGROUPTYPES_TABLE = "GROUPTYPES";
	private static final String DOMATTRIBUTETYPES_TABLE = "ATTRIBUTETYPES";
	private static final String LICENSE_TABLE = "LICENSE";
	private static final String ATTRIBUTEGROUP_TABLE = "ATTRIBUTEGROUP";
	private static final String ATTRIBUTE_TABLE = "ATTRIBUTE";
	private static final String VALUE_TABLE = "VALUE";
	private static final String LICENSECONTENT_TABLE = "LICENSECONTENT";
	private static final String PRESENTATION_TABLE = "PRESENTATION";

	private static final String VALIDTO_COLUMN = "VALIDTO";
	private static final String VALIDFROM_COLUMN = "VALIDFROM";
	private static final String NAME_COLUMN = "NAME";
	private static final String NAME_EN_COLUMN = "NAME_EN";
	private static final String DESCRIPTION_DK_COLUMN = "DESCRIPTION_DK";
	private static final String DESCRIPTION_EN_COLUMN = "DESCRIPTION_EN";
	private static final String QUERY_COLUMN = "QUERYSTRING";
	private static final String NUMBER_COLUMN = "NUMBER";
	private static final String LICENSEID_COLUMN = "LICENSEID";
	private static final String LICENSECONTENTID_COLUMN = "LICENSECONTENTID";
	private static final String ATTRIBUTEGROUPID_COLUMN = "ATTRIBUTEGROUPID";
	private static final String ATTRIBUTEID_COLUMN = "ATTRIBUTEID";

	private static final String ID_COLUMN = "ID"; // ID used for all tables
	private static final String KEY_COLUMN = "KEY";
	private static final String VALUE_COLUMN = "VALUE";
	private static final String VALUE_DK_COLUMN = "VALUE_DK";
	private static final String VALUE_EN_COLUMN = "VALUE_EN";
	private static final String MUSTGROUP_COLUMN = "MUSTGROUP";

	private final static String selectDomLicensePresentationTypesQuery = 
			" SELECT * FROM " + DOMLICENSEPRESENTATIONTYPES_TABLE;	

	private final static String selectAllLicensesQuery = 
			" SELECT * FROM " + LICENSE_TABLE;	

	private final static String selectLicenseQuery = 
			" SELECT * FROM " + LICENSE_TABLE +" WHERE ID = ? ";

	private final static String persistDomLicensePresentationTypeQuery = 
			"INSERT INTO " +DOMLICENSEPRESENTATIONTYPES_TABLE
			+ " ("
			+ ID_COLUMN + ","
			+ KEY_COLUMN + "," 
			+ VALUE_DK_COLUMN  + ","
			+ VALUE_EN_COLUMN  + ","
			+ ") VALUES (?,?,?,?)"; // #|?|=4

	private final static String persistAttributeGroupForLicenseQuery = 
			"INSERT INTO " + ATTRIBUTEGROUP_TABLE
			+ " ("
			+ ID_COLUMN + ","
			+ NUMBER_COLUMN +","
			+ LICENSEID_COLUMN   
			+ ") VALUES (?,?,?)"; // #|?|=3

	private final static String selectAttributeGroupsForLicenseQuery = 
			" SELECT * FROM " + ATTRIBUTEGROUP_TABLE + " WHERE "+ LICENSEID_COLUMN+ "= ? ORDER BY NUMBER";


	private final static String persistAttributeForAttributeGroupQuery = 
			"INSERT INTO " + ATTRIBUTE_TABLE
			+ " ("
			+ ID_COLUMN + ","
			+ NAME_COLUMN +","
			+ ATTRIBUTEGROUPID_COLUMN   
			+ ") VALUES (?,?,?)"; // #|?|=3

	private final static String selectAttributesForAttributeGroupQuery = 
			" SELECT * FROM " + ATTRIBUTE_TABLE + " WHERE "+ ATTRIBUTEGROUPID_COLUMN +" = ?";

	private final static String persistValueForAttributeQuery = 
			"INSERT INTO " + VALUE_TABLE
			+ " ("
			+ ID_COLUMN + ","
			+ VALUE_COLUMN +","
			+ ATTRIBUTEID_COLUMN   
			+ ") VALUES (?,?,?)"; // #|?|=3

	private final static String selectValuesForAttributeQuery = 
			" SELECT * FROM " + VALUE_TABLE + " WHERE "+ ATTRIBUTEID_COLUMN +" = ?";	

	private final static String selectDomLicenseGroupTypesQuery = 
			" SELECT * FROM " + DOMLICENSEGROUPTYPES_TABLE +" ORDER BY "+KEY_COLUMN;

	private final static String persistDomLicenseGroupTypeQuery = 
			"INSERT INTO " + DOMLICENSEGROUPTYPES_TABLE
			+ " ("
			+ ID_COLUMN + ","
		    + KEY_COLUMN + ","
			+ VALUE_DK_COLUMN +" ,"
			+ VALUE_EN_COLUMN +" ,"
			+ DESCRIPTION_DK_COLUMN +" ,"
			+ DESCRIPTION_EN_COLUMN +" ,"
			+ QUERY_COLUMN+ " ,"			
			+ MUSTGROUP_COLUMN
			+ ") VALUES (?,?,?,?,?,?,?,?)"; // #|?|=8

	private final static String updateDomLicenseGroupTypeQuery = 
			"UPDATE " + DOMLICENSEGROUPTYPES_TABLE +			
			" SET " + VALUE_DK_COLUMN+ " = ? , "+			
			VALUE_EN_COLUMN + " = ? ,"+
			DESCRIPTION_DK_COLUMN + " = ? ,"+
	        DESCRIPTION_EN_COLUMN + " = ? ,"+
			QUERY_COLUMN + " = ? ,"+
			MUSTGROUP_COLUMN + " = ? "+				  
			"WHERE "+ ID_COLUMN +" = ? "; 	

	private final static String updateDomsLicensePresentationTypeQuery = 
			"UPDATE " + DOMLICENSEPRESENTATIONTYPES_TABLE +			
			" SET " + VALUE_DK_COLUMN+ " = ? , "+			
			VALUE_EN_COLUMN + " = ? "+			
			"WHERE "+ ID_COLUMN +" = ? "; 	
	

	private final static String persistLicenseQuery = 
			"INSERT INTO " + LICENSE_TABLE
			+ " ("
			+ ID_COLUMN + ","
			+ NAME_COLUMN + ","
			+ NAME_EN_COLUMN + ","
			+ DESCRIPTION_DK_COLUMN + ","
			+ DESCRIPTION_EN_COLUMN + ","
			+ VALIDFROM_COLUMN + ","
			+ VALIDTO_COLUMN
			+ ") VALUES (?,?,?,?,?,?,?)"; // #|?|=7

	private final static String selectDomAttributeTypesQuery = 
			" SELECT * FROM " + DOMATTRIBUTETYPES_TABLE +" ORDER BY "+VALUE_COLUMN;;

			private final static String deleteDomAttributeTypeByNameQuery = 
					" DELETE FROM " + DOMATTRIBUTETYPES_TABLE +" WHERE "+ VALUE_COLUMN  +" = ?";

			private final static String deleteDomGroupTypeByKeyQuery = 
					" DELETE FROM " + DOMLICENSEGROUPTYPES_TABLE +" WHERE "+ KEY_COLUMN  +" = ?";

			private final static String deleteDomPresentationTypeByKeyQuery = 
					" DELETE FROM " + DOMLICENSEPRESENTATIONTYPES_TABLE +" WHERE "+ KEY_COLUMN  +" = ?";

			private final static String persistDomAttributeTypeQuery = 
					"INSERT INTO " + DOMATTRIBUTETYPES_TABLE
					+ " ("
					+ ID_COLUMN + ","
					+ VALUE_COLUMN
					+ ") VALUES (?,?)"; // #|?|=2

			private final static String selectLicenseContentForLicenseQuery = 
					" SELECT * FROM " + LICENSECONTENT_TABLE + " WHERE "+ LICENSEID_COLUMN +" = ? ";

			private final static String persistLicenseContentForLicenseQuery = 
					"INSERT INTO " + LICENSECONTENT_TABLE
					+ " ("
					+ ID_COLUMN + ","
					+ NAME_COLUMN +","
					+ LICENSEID_COLUMN   
					+ ") VALUES (?,?,?)"; // #|?|=3

			private final static String selectPresentationTypesForLicenseContentQuery = 
					" SELECT * FROM " + PRESENTATION_TABLE + " WHERE "+ LICENSECONTENTID_COLUMN +" = ? ";

			private final static String persistPresentationTypesForLicenseContentQuery = 
					"INSERT INTO " + PRESENTATION_TABLE
					+ " ("
					+ ID_COLUMN + ","
					+ NAME_COLUMN +","
					+ LICENSECONTENTID_COLUMN   
					+ ") VALUES (?,?,?)"; // #|?|=3

			//Deletes
			private final static String deletePresentationsByLicenseContentIdQuery = 
					" DELETE FROM " + PRESENTATION_TABLE +" WHERE "+ LICENSECONTENTID_COLUMN +" = ?";	

			private final static String deleteLicenseContentsByLicenseIdQuery = 
					" DELETE FROM " + LICENSECONTENT_TABLE +" WHERE "+ LICENSEID_COLUMN +" = ?";	

			private final static String deleteAttributesByAttributeGroupIdQuery = 
					" DELETE FROM " + ATTRIBUTE_TABLE +" WHERE "+ ATTRIBUTEGROUPID_COLUMN +" = ?";	

			private final static String countAttributesByAttributeNameQuery = 
					" SELECT COUNT(*) FROM " + ATTRIBUTE_TABLE +" WHERE "+ NAME_COLUMN +" = ?";	

			private final static String countGroupTypeByGroupNameQuery = 
					" SELECT COUNT(*) FROM " + LICENSECONTENT_TABLE +" WHERE "+ NAME_COLUMN +" = ?";	

			private final static String countPresentationTypeByPresentationNameQuery = 
					" SELECT COUNT(*) FROM " + PRESENTATION_TABLE +" WHERE "+ NAME_COLUMN +" = ?";	

			private final static String deleteValuesByAttributeIdQuery = 
					" DELETE FROM " + VALUE_TABLE +" WHERE "+ ATTRIBUTEID_COLUMN +" = ?";	

			private final static String deleteAttributeGroupByLicenseIdQuery = 
					" DELETE FROM " + ATTRIBUTEGROUP_TABLE +" WHERE "+ LICENSEID_COLUMN +" = ?";	

			private final static String deleteLicenseByLicenseIdQuery = 
					" DELETE FROM " + LICENSE_TABLE +" WHERE "+ ID_COLUMN +" = ?";	

			public H2Storage(String dbFilePath) throws SQLException {
				log.info("Intialized H2Storage, dbFile=" + dbFilePath);
				synchronized (H2Storage.class) {
					initializeDBConnection(dbFilePath);
				}
			}

			public static H2Storage getInstance() {
				if (instance == null) {
					throw new IllegalArgumentException("H2 Storage has not been initialized yet");
				}

				return instance;
			}

			public void persistDomLicensePresentationType(String key, String value_dk, String value_en) throws Exception {
				log.info("Persisting new dom license presentationtype: " + key );

				validateValue(key);
				validateValue(value_dk);
				value_dk = value_dk.trim();
				key = key.trim();
				validateValue(value_en);
				value_en=value_en.trim();
				
				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(persistDomLicensePresentationTypeQuery);
					stmt.setLong(1, generateUniqueID());
					stmt.setString(2, key);
					stmt.setString(3, value_dk);
					stmt.setString(4, value_en);
					stmt.execute();
					singleDBConnection.commit();
				} catch (SQLException e) {
					log.error("SQL Exception in persistPresentationType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
				// We are not closing the connection (EVAR!)
			}

			public ArrayList<ConfiguredDomLicensePresentationType> getDomLicensePresentationTypes() throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<ConfiguredDomLicensePresentationType> list = new ArrayList<ConfiguredDomLicensePresentationType>();

				try {
					stmt = singleDBConnection.prepareStatement(selectDomLicensePresentationTypesQuery);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String key = rs.getString(KEY_COLUMN);
						String value = rs.getString(VALUE_DK_COLUMN);
						String value_en = rs.getString(VALUE_EN_COLUMN);
						ConfiguredDomLicensePresentationType item = new ConfiguredDomLicensePresentationType(id, key, value, value_en);
						list.add(item);
					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getPresentationTypes:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			public synchronized void deleteLicense(long licenseId, boolean commit) throws Exception {
				log.info("Deleting license with id: " + licenseId);
				License license = null;
				try {
					license = getLicense(licenseId);
				} catch (IllegalArgumentException e) {
					// No license in DB with that ID, nothing to delete
					return;
				}

				for (AttributeGroup currentAttributeGroup : license.getAttributeGroups()) {

					ArrayList<Attribute> attributes = currentAttributeGroup.getAttributes();
					for (Attribute currentAttribute : attributes) {
						deleteById(deleteValuesByAttributeIdQuery, currentAttribute.getId(), commit);
					}

					deleteById(deleteAttributesByAttributeGroupIdQuery, currentAttributeGroup.getId(), commit);
				}

				for (LicenseContent currentLicenseContent : license.getLicenseContents()) {
					deleteById(deletePresentationsByLicenseContentIdQuery, currentLicenseContent.getId(), commit);
				}

				deleteById(deleteLicenseContentsByLicenseIdQuery, licenseId, commit);
				deleteById(deleteAttributeGroupByLicenseIdQuery, licenseId, commit);
				deleteById(deleteLicenseByLicenseIdQuery, licenseId, commit);

				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			// query can be null or empty
			public void persistDomLicenseGroupType(String key, String value, String value_en , String description, String description_en,  String query, boolean mustGroup) throws Exception {

				if (!StringUtils.isNotEmpty(key)) {
					throw new IllegalArgumentException("Key must not be null when creating new Group");
				}
				
				if (!StringUtils.isNotEmpty(value)) {
					throw new IllegalArgumentException("Value must not be null when creating new Group");
				}

				if (!StringUtils.isNotEmpty(value_en)) {
					throw new IllegalArgumentException("Value(EN) must not be null when creating new Group");
				}
				
				if (!StringUtils.isNotEmpty(query)) {
					throw new IllegalArgumentException("Query must not be null when creating new Group");
				}

				log.info("Persisting new dom license group type: " + key);

				validateValue(value);
				value = value.trim();

				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(persistDomLicenseGroupTypeQuery);
					stmt.setLong(1, generateUniqueID());
					stmt.setString(2, key);
					stmt.setString(3, value);
					stmt.setString(4, value_en);	
					stmt.setString(5, description);
					stmt.setString(6, description_en);
					stmt.setString(7, query);
					stmt.setBoolean(8, mustGroup);
					stmt.execute();
					singleDBConnection.commit();
				} catch (SQLException e) {
					log.error("SQL Exception in persistDomLicenseGroupType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			public void updateDomLicenseGroupType(long id,String value_dk,String value_en, String description, String description_en, String query, boolean mustGroup) throws Exception {
				PreparedStatement stmt = null;

				try {
					log.info("Updating Group type with id:" + id);

					// if it exists already, we do not add it.
					stmt = singleDBConnection.prepareStatement(updateDomLicenseGroupTypeQuery);
					stmt.setString(1,value_dk);
					stmt.setString(2,value_en);
					stmt.setString(3,description);
					stmt.setString(4,description_en);
					stmt.setString(5, query);
					stmt.setBoolean(6, mustGroup);
					stmt.setLong(7, id);

					int updated = stmt.executeUpdate();
					if (updated != 1) {
						throw new SQLException("Grouptype id not found:" + id);
					}

					singleDBConnection.commit();

				} catch (Exception e) {
					log.error("Exception in updateDomLicenseGroupType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);

				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			
			
			public void updateDomPresentationType(long id,String value_dk,String value_en) throws Exception {
				PreparedStatement stmt = null;

				try {
					log.info("Updating Presentation type with id:" + id);

					// if it exists already, we do not add it.
					stmt = singleDBConnection.prepareStatement(updateDomsLicensePresentationTypeQuery);
					stmt.setString(1,value_dk);
					stmt.setString(2,value_en);					
					stmt.setLong(3, id);

					int updated = stmt.executeUpdate();
					if (updated != 1) {
						throw new SQLException("Presentationtype id not found:" + id);
					}

					singleDBConnection.commit();

				} catch (Exception e) {
					log.error("Exception in updateDomPresentationType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);

				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			
			public synchronized void deleteDomLicenseGroupType(String groupName) throws Exception {

				log.info("Deleting grouptype: " + groupName);
				// First check it is not used in any license, in that case throw exception.

				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(countGroupTypeByGroupNameQuery);
					stmt.setString(1, groupName);
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						int number = rs.getInt(1);
						if (number > 0) {
							throw new IllegalArgumentException("Can not delete group with name:" + groupName + " because it is used in at least 1 license");
						}
					}
					stmt = singleDBConnection.prepareStatement(deleteDomGroupTypeByKeyQuery);
					stmt.setString(1, groupName);
					int updated = stmt.executeUpdate();
					log.info("deleted " + updated + " grouptypes with name:" + groupName);
					singleDBConnection.commit();
				} catch (SQLException e) {
					log.error("SQL Exception in deleteDomLicenseGroupType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			public synchronized void deleteDomPresentationType(String presentationName) throws Exception {

				log.info("Deleting presentation type: " + presentationName);
				// First check it is not used in any license, in that case throw exception.

				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(countPresentationTypeByPresentationNameQuery);
					stmt.setString(1, presentationName);
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						int number = rs.getInt(1);
						if (number > 0) {
							throw new IllegalArgumentException("Can not delete presentationtype with name:" + presentationName + " because it is used in at least 1 license");
						}
					}
					stmt = singleDBConnection.prepareStatement(deleteDomPresentationTypeByKeyQuery);
					stmt.setString(1, presentationName);
					int updated = stmt.executeUpdate();
					log.info("deleted " + updated + " presentationtype with name:" + presentationName);
					singleDBConnection.commit();
				} catch (SQLException e) {
					log.error("SQL Exception in deleteDomPresentationType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			// Syncronized because of possible rollback.
			public synchronized void persistLicense(License license) throws Exception {

				log.info("Persisting new license: " + license.getLicenseName());

				// validate name, description, validTo,validFrom
				boolean validateMainFields = license.validateMainFields();
				boolean validateAttributesValues = license.validateAttributesAndValuesNotNull();
				if (!validateMainFields) {
					throw new IllegalArgumentException("Validation error. Name/description too short or validTo/validFrom not legal dates");
				}
				if (!validateAttributesValues) {
					throw new IllegalArgumentException("Validation error. Attributes or values must not be empty");
				}

				PreparedStatement stmt = null;
				try {
					Long licenseId;
					if (license.getId() > 0) { // This is an existing license in the DB, delete it before updating
						licenseId = license.getId();
						// Delete old license before updating (creating new)
						log.info("Deleting license before updating");
						deleteLicense(licenseId, false);

					} else {
						licenseId = generateUniqueID(); // new ID.
					}

					stmt = singleDBConnection.prepareStatement(persistLicenseQuery);

					stmt.setLong(1, licenseId);
					stmt.setString(2, license.getLicenseName());
					stmt.setString(3, license.getLicenseName_en());
					stmt.setString(4, license.getDescription_dk());
					stmt.setString(5, license.getDescription_en());
					stmt.setString(6, license.getValidFrom());
					stmt.setString(7, license.getValidTo());
					stmt.execute();

					// persist assocations, do not commit before all completed.
					persistAttributeGroupsForLicense(licenseId, license.getAttributeGroups(), false);
					persistLicenseContentsForLicense(licenseId, license.getLicenseContents(), false);
					singleDBConnection.commit(); // This commit will also commit all the nested statements
				} catch (Exception e) {
					log.error("SQL Exception in persistLicense:" + e.getMessage());
					log.error("Beginning rollback of transaction");
					singleDBConnection.rollback(); // This is the only situation we have to do rollback due to multiple statements.
					log.error("Rollback of transaction completed");
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			public ArrayList<ConfiguredDomLicenseGroupType> getDomLicenseGroupTypes() throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<ConfiguredDomLicenseGroupType> list = new ArrayList<ConfiguredDomLicenseGroupType>();
				try {
					stmt = singleDBConnection.prepareStatement(selectDomLicenseGroupTypesQuery);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String key = rs.getString(KEY_COLUMN);
						String value_dk = rs.getString(VALUE_DK_COLUMN);
						String value_en = rs.getString(VALUE_EN_COLUMN);
						String description = rs.getString(DESCRIPTION_DK_COLUMN);
						String description_en = rs.getString(DESCRIPTION_EN_COLUMN);
						String query = rs.getString(QUERY_COLUMN);
						boolean mustGroup = rs.getBoolean(MUSTGROUP_COLUMN);
						ConfiguredDomLicenseGroupType item = new ConfiguredDomLicenseGroupType(id, key,value_dk,value_en, description, description_en, query, mustGroup);
						list.add(item);
					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getDomLicenseGroupTypes():" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			public synchronized void persistDomAttributeType(String value) throws Exception {

				log.info("Persisting new DOM attribute type: " + value);

				validateValue(value);
				value = value.trim();

				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(persistDomAttributeTypeQuery);
					stmt.setLong(1, generateUniqueID());
					stmt.setString(2, value);
					stmt.execute();
					singleDBConnection.commit();
				} catch (SQLException e) {
					log.error("SQL Exception in persistDomAttributeType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			public synchronized void deleteDomAttributeType(String attributeTypeName) throws Exception {

				log.info("Deleting attributetype: " + attributeTypeName);
				// First check it is not used in any license, in that case throw exception.

				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(countAttributesByAttributeNameQuery);
					stmt.setString(1, attributeTypeName);
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						int number = rs.getInt(1);
						if (number > 0) {
							throw new IllegalArgumentException("Can not delete attribute with name:" + attributeTypeName + " because it is used in at least 1 license");
						}
					}
					stmt = singleDBConnection.prepareStatement(deleteDomAttributeTypeByNameQuery);
					stmt.setString(1, attributeTypeName);
					int updated = stmt.executeUpdate();
					log.info("deleted " + updated + " attributetypes with name:" + attributeTypeName);
					singleDBConnection.commit();
				} catch (SQLException e) {
					log.error("SQL Exception in deleteDomAttributeType:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LicenseCache.reloadCache(); // Force reload so the change will be instant in the cache
			}

			public ArrayList<ConfiguredDomAttributeType> getDomAttributeTypes() throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<ConfiguredDomAttributeType> list = new ArrayList<ConfiguredDomAttributeType>();
				try {
					stmt = singleDBConnection.prepareStatement(selectDomAttributeTypesQuery);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String value = rs.getString(VALUE_COLUMN);
						ConfiguredDomAttributeType item = new ConfiguredDomAttributeType(id, value);
						list.add(item);
					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getDomAttributes:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			// this method only loads the name of the license and NOT all associations.
			public ArrayList<License> getAllLicenseNames() throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<License> list = new ArrayList<License>();
				try {
					stmt = singleDBConnection.prepareStatement(selectAllLicensesQuery);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						License license = new License();
						Long id = rs.getLong(ID_COLUMN);
						String name = rs.getString(NAME_COLUMN);
						String description_dk = rs.getString(DESCRIPTION_DK_COLUMN);
						String description_en = rs.getString(DESCRIPTION_EN_COLUMN);
						String validFrom = rs.getString(VALIDFROM_COLUMN);
						String validTo = rs.getString(VALIDTO_COLUMN);
						license.setId(id);
						license.setLicenseName(name);
						license.setDescription_dk(description_dk);
						license.setDescription_en(description_en);
						license.setValidFrom(validFrom);// todo format
						license.setValidTo(validTo);// todo format
						list.add(license);
					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getAllLicenseNames:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			// this method a license from licenseId with all associations (complete object-tree)
			public License getLicense(long licenseId) throws Exception {

				PreparedStatement stmt = null;
				License license = new License();
				try {
					stmt = singleDBConnection.prepareStatement(selectLicenseQuery);
					stmt.setLong(1, licenseId);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) { // maximum one due to unique/primary key constraint
						Long id = rs.getLong(ID_COLUMN);
						String name = rs.getString(NAME_COLUMN);
						String name_en = rs.getString(NAME_EN_COLUMN);
						String description_dk = rs.getString(DESCRIPTION_DK_COLUMN);
						String description_en = rs.getString(DESCRIPTION_EN_COLUMN);
						String validFrom = rs.getString(VALIDFROM_COLUMN);
						String validTo = rs.getString(VALIDTO_COLUMN);
						license.setId(id);
						license.setLicenseName(name);
						license.setLicenseName_en(name_en);
						license.setDescription_dk(description_dk);
						license.setDescription_en(description_en);
						license.setValidFrom(validFrom);
						license.setValidTo(validTo);

						license.setAttributeGroups(getAttributeGroupsForLicense(id));
						license.setLicenseContents(getLicenseContentsForLicense(id));

						return license;
					}
					throw new IllegalArgumentException("License not found for licenseId:" + licenseId);

				} catch (SQLException e) {
					log.error("SQL Exception in getLicense:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected void validateValue(String value) {
				// sanity, must have length at least 2
				if (value == null || value.trim().length() < 2) {
					throw new IllegalArgumentException("Value empty or too short");
				}

			}

			/*
			 * public StatisticsDTO getStatistics() throws SQLException{
			 * 
			 * StatisticsDTO output = new StatisticsDTO(); long t1=System.currentTimeMillis(); PreparedStatement stmt = null; try{
			 * 
			 * //Extract statistics for number of items added for each day, up to 50 days back. Calendar cal = Calendar.getInstance();
			 * 
			 * Date start=cal.getTime(); Date end=cal.getTime();; ArrayList<Integer> itemsAddedDaysAgo = new ArrayList<Integer>();
			 * 
			 * for (int daysAgo=0;daysAgo<50;daysAgo++){ //last 50 days cal.add(Calendar.DAY_OF_YEAR, -1); start=cal.getTime();
			 * 
			 * long startLong=start.getTime(); long endLong=end.getTime(); stmt = singleDBConnection.prepareStatement("SELECT COUNT(*) FROM " + ATTRIBUTESTORE_TABLE +
			 * " WHERE "+MODIFIED_COLUMN +" > ? AND " +MODIFIED_COLUMN +" <= ?"); stmt.setLong(1, startLong); stmt.setLong(2, endLong); ResultSet
			 * rs=stmt.executeQuery(); while (rs.next()){ int inserted=rs.getInt(1); itemsAddedDaysAgo.add(new Integer(inserted)); } end=cal.getTime(); }
			 * 
			 * stmt = singleDBConnection.prepareStatement("SELECT COUNT(*) FROM " + ATTRIBUTESTORE_TABLE); ResultSet rs=stmt.executeQuery(); rs.next(); //Will always
			 * have 1 row int count = rs.getInt(1);
			 * 
			 * output.setTotalItems(count); output.setItemsAddedDaysAgo(itemsAddedDaysAgo); output.setLast100KeyValuesAdded(lastKeyValues);
			 * output.setExtractTimeInMillis(System.currentTimeMillis()-t1);
			 * 
			 * 
			 * return output; } catch (SQLException e){ e.printStackTrace(); log.error("SQL Exception in extractStatistics:"+e.getMessage()); throw e; } finally{
			 * closeStatement(stmt); }
			 * 
			 * 
			 * }
			 */

			// Just a simple way to generate unique ID's and make sure they are unique
			private synchronized long generateUniqueID() {
				long now = System.currentTimeMillis();
				if (now <= lastTimestamp) { // this timestamp has already been used. just +1 and use that
					lastTimestamp++;
					return lastTimestamp;
				} else {
					lastTimestamp = now;
					return now;
				}
			}

			/*
			 * Will create a zip-file with a backup of the database.
			 * 
			 * @param filePathtoBackup Full path+ filename of the backup file (including .zip)
			 * 
			 * It will take about 5 seconds for a database with 250K rows
			 */
			public synchronized void backupDatabase(String fileName) throws SQLException {
				log.info("Creating database backup :" + fileName);
				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement("BACKUP TO '" + fileName + "'");
					stmt.execute();
					log.info("Database backup success");
				} catch (SQLException e) {
					log.error("SQL Exception in databasebackup:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
				LASTDATABASEBACKUPDATE = new Date();
				NUMBER_DATABASE_BACKUP_SINCE_STARTUP++;
			}

			public static String getH2ProfilerData(int topWorst) {
				if (profiler != null) {
					return profiler.getTop(topWorst);
				}
				return "Profiler not initialised. It must be enabled in the code.";

			}

			// Used from unittests. Create tables DDL etc.
			protected synchronized void runDDLScript(File file) throws SQLException {
				log.info("Running DDL script:" + file.getAbsolutePath());

				if (!file.exists()) {
					log.error("DDL script not found:" + file.getAbsolutePath());
					throw new RuntimeException("DDLscript file not found:" + file.getAbsolutePath());
				}

				String scriptStatement = "RUNSCRIPT FROM '" + file.getAbsolutePath() + "'";

				singleDBConnection.prepareStatement(scriptStatement).execute();
			}

			protected void persistAttributeGroupsForLicense(Long licenseId, ArrayList<AttributeGroup> attributegroups, boolean commit) throws SQLException {

				if (attributegroups == null || attributegroups.size() == 0) {
					throw new IllegalArgumentException("No attributegroups defined for license");
				}
				PreparedStatement stmt = null;
				try {

					for (AttributeGroup current : attributegroups) {
						long attributeGroupId = generateUniqueID();

						stmt = singleDBConnection.prepareStatement(persistAttributeGroupForLicenseQuery);
						stmt.setLong(1, attributeGroupId);
						stmt.setInt(2, current.getNumber());
						stmt.setLong(3, licenseId);
						stmt.execute();

						persistAttributesForAttributeGroup(attributeGroupId, current.getAttributes(), commit);
					}
					if (commit) {
						singleDBConnection.commit();
					}
				} catch (SQLException e) {
					log.error("SQL Exception in persistAttributeGroupsForLicense:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			//
			protected void deleteById(String query, Long id, boolean commit) throws SQLException {
				PreparedStatement stmt = null;
				try {
					stmt = singleDBConnection.prepareStatement(query);
					stmt.setLong(1, id);
					stmt.executeUpdate();

					if (commit) {
						singleDBConnection.commit();
					}
				} catch (SQLException e) {
					log.error("SQL Exception in deleteById for query:" + query + " Exception:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected ArrayList<AttributeGroup> getAttributeGroupsForLicense(Long licenseId) throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<AttributeGroup> list = new ArrayList<AttributeGroup>();
				try {
					stmt = singleDBConnection.prepareStatement(selectAttributeGroupsForLicenseQuery);
					stmt.setLong(1, licenseId);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						int number = rs.getInt(NUMBER_COLUMN);
						AttributeGroup item = new AttributeGroup(number);
						item.setId(id);
						list.add(item);

						ArrayList<Attribute> attributes = getAttributesForAttributeGroup(id);
						item.setAttributes(attributes);

					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getAttributeGroupsForLicense:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}

			}

			protected void persistAttributesForAttributeGroup(Long attributeGroupId, ArrayList<Attribute> attributes, boolean commit) throws SQLException {

				if (attributes == null || attributes.size() == 0) {
					throw new IllegalArgumentException("No attributes defined for attributegroup:" + attributeGroupId);
				}
				PreparedStatement stmt = null;
				try {

					for (Attribute current : attributes) {
						long attributeId = generateUniqueID();

						stmt = singleDBConnection.prepareStatement(persistAttributeForAttributeGroupQuery);
						stmt.setLong(1, attributeId);
						stmt.setString(2, current.getAttributeName());
						stmt.setLong(3, attributeGroupId);
						stmt.execute();

						persistValuesForAttribute(attributeId, current.getValues(), commit);

					}
					if (commit) {
						singleDBConnection.commit();
					}
				} catch (SQLException e) {
					log.error("SQL Exception in persistAttributesForAttributeGroup:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected ArrayList<LicenseContent> getLicenseContentsForLicense(Long licenseId) throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<LicenseContent> list = new ArrayList<LicenseContent>();
				try {
					stmt = singleDBConnection.prepareStatement(selectLicenseContentForLicenseQuery);
					stmt.setLong(1, licenseId);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String name = rs.getString(NAME_COLUMN);
						LicenseContent item = new LicenseContent();
						item.setId(id);
						item.setName(name);
						list.add(item);

						ArrayList<Presentation> presentations = getPresentationsForLicenseContent(id);
						item.setPresentations(presentations);

					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getLicenseContentsForLicense:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}

			}

			protected void persistLicenseContentsForLicense(Long licenseId, ArrayList<LicenseContent> licenseContents, boolean commit) throws SQLException {

				PreparedStatement stmt = null;
				try {

					for (LicenseContent current : licenseContents) {
						long licenseContentId = generateUniqueID();

						stmt = singleDBConnection.prepareStatement(persistLicenseContentForLicenseQuery);
						stmt.setLong(1, licenseContentId);
						stmt.setString(2, current.getName());
						stmt.setLong(3, licenseId);
						stmt.execute();

						persistPresentationsForLicenseContent(licenseContentId, current.getPresentations(), commit);

					}
					if (commit) {
						singleDBConnection.commit();
					}
				} catch (SQLException e) {
					log.error("SQL Exception in persistLicenseContentsForLicense:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected void persistPresentationsForLicenseContent(Long licenseContentId, ArrayList<Presentation> presentations, boolean commit) throws SQLException {

				if (presentations == null || presentations.size() == 0) {
					throw new IllegalArgumentException("No presentationtypes defined for licensecontentId:" + licenseContentId);
				}
				PreparedStatement stmt = null;
				try {

					for (Presentation current : presentations) {
						stmt = singleDBConnection.prepareStatement(persistPresentationTypesForLicenseContentQuery);
						stmt.setLong(1, generateUniqueID());
						stmt.setString(2, current.getKey());
						stmt.setLong(3, licenseContentId);
						stmt.execute();
					}

					if (commit) {
						singleDBConnection.commit();
					}
				} catch (SQLException e) {
					log.error("SQL Exception in persistPresentationsForLicenseContent:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected ArrayList<Presentation> getPresentationsForLicenseContent(long licenseContentId) throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<Presentation> list = new ArrayList<Presentation>();
				try {
					stmt = singleDBConnection.prepareStatement(selectPresentationTypesForLicenseContentQuery);
					stmt.setLong(1, licenseContentId);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String name = rs.getString(NAME_COLUMN);
						Presentation item = new Presentation(name);
						item.setId(id);
						list.add(item);

					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getPresentationsForLicenseContent:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}

			}

			protected ArrayList<Attribute> getAttributesForAttributeGroup(long attributeGroupId) throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<Attribute> list = new ArrayList<Attribute>();
				try {
					stmt = singleDBConnection.prepareStatement(selectAttributesForAttributeGroupQuery);
					stmt.setLong(1, attributeGroupId);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String name = rs.getString(NAME_COLUMN);
						Attribute item = new Attribute();
						item.setId(id);
						item.setAttributeName(name);

						ArrayList<AttributeValue> attributeValues = getValuesForAttribute(id);
						item.setValues(attributeValues);
						list.add(item);

					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getAttributesForAttributeGroup:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected void persistValuesForAttribute(Long attributeId, ArrayList<AttributeValue> values, boolean commit) throws SQLException {

				if (values == null || values.size() == 0) {
					throw new IllegalArgumentException("No values defined for attribute:" + attributeId);
				}
				PreparedStatement stmt = null;
				try {

					for (AttributeValue current : values) {
						stmt = singleDBConnection.prepareStatement(persistValueForAttributeQuery);
						stmt.setLong(1, generateUniqueID());
						stmt.setString(2, current.getValue());
						stmt.setLong(3, attributeId);
						stmt.execute();
					}

					if (commit) {
						singleDBConnection.commit();
					}
				} catch (SQLException e) {
					log.error("SQL Exception in  persistValuesForAttribute:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}
			}

			protected ArrayList<AttributeValue> getValuesForAttribute(long attributeId) throws SQLException {
				PreparedStatement stmt = null;
				ArrayList<AttributeValue> list = new ArrayList<AttributeValue>();
				try {
					stmt = singleDBConnection.prepareStatement(selectValuesForAttributeQuery);
					stmt.setLong(1, attributeId);
					ResultSet rs = stmt.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(ID_COLUMN);
						String value = rs.getString(VALUE_COLUMN);
						AttributeValue item = new AttributeValue(value);
						item.setId(id);
						item.setValue(value);
						list.add(item);
					}
					return list;
				} catch (SQLException e) {
					log.error("SQL Exception in getValuesForAttribute:" + e.getMessage());
					throw e;
				} finally {
					closeStatement(stmt);
				}

			}

			private synchronized void initializeDBConnection(String dbFilePath) throws SQLException {
				log.info("initializeDBConnection. DB path:" + dbFilePath);
				if (singleDBConnection != null) {
					log.error("DB allready initialized and locked:" + dbFilePath);
					throw new RuntimeException("DB allready initialized and locked:" + dbFilePath);
				}

				try {
					Class.forName("org.h2.Driver"); // load the driver
				} catch (ClassNotFoundException e) {
					throw new SQLException(e);
				}
				String DB_URL = "jdbc:h2:" + dbFilePath;
				singleDBConnection = DriverManager.getConnection(DB_URL, "", "");
				singleDBConnection.setAutoCommit(false);

				instance = this;
				// If ever performance is an issue, TRANSACTION_READ_UNCOMMITTED can be enabled. But
				// I prefer not to have this setting. Since we only have 1 connection at a time,
				// it should be possible to enable this setting without breaking anything.
				// singleDBConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

				INITDATE = new Date();

				// In-comment to enable profiler (small performance overhead)
				// profiler = new Profiler();
				// profiler.startCollecting();

			}

			private void closeStatement(PreparedStatement stmt) {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException e) {
					log.error("Failed to close statement");
					// ignore..
				}
			}

			// This is called by from InialialziationContextListener by the Web-container when server is shutdown,
			// Just to be sure the DB lock file is free.
			public static void shutdown() {
				log.info("Shutdown H2Storage");
				try {
					if (singleDBConnection != null) {
						PreparedStatement shutdown = singleDBConnection.prepareStatement("SHUTDOWN");
						shutdown.execute();
						if (singleDBConnection != null) {
							singleDBConnection.close();
						}
						Thread.sleep(3000L);
					}
				} catch (Exception e) {
					// ignore errors during shutdown, we cant do anything about it anyway
					log.error("shutdown failed", e);
				}
			}
}
