package dk.statsbiblioteket.doms.licensemodule;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseModulePropertiesLoader {
	
	private static final Logger log = LoggerFactory.getLogger(LicenseModulePropertiesLoader.class);
	private static final String LICENSEMODULE_PROPERTY_FILE = "license_module.properties";
	 
	private static final String H2_DB_FILE_PROPERTY="licensemodule.h2.db.file";
	private static final String H2_DB_BACKUP_FOLDER_PROPERTY="licensemodule.h2.db.backup.folder";
	private static final String DOMS_SOLR_SERVER_PROPERTY = "licensemodule.doms.solr.server";
	
	
	public static String DBFILE = null;
	public static String DBBACKUPFOLDER = null;
	public static String DOMS_SOLR_SERVER = null;
	
	static{
		log.info("Initializing Licensemodule-properties");
		try {
			initProperties();		
		} 
		catch (Exception e) {
			e.printStackTrace();
			log.error("Could not load property file:"+LICENSEMODULE_PROPERTY_FILE);					
		}
	}
		
	private static void initProperties()  throws Exception{

		String user_home=System.getProperty("user.home");
		log.info("Load properties: Using user.home folder:" + user_home);
		InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(user_home,LICENSEMODULE_PROPERTY_FILE)), "ISO-8859-1");

		Properties serviceProperties = new Properties();
		serviceProperties.load(isr);
		isr.close();

		DBFILE =serviceProperties.getProperty(H2_DB_FILE_PROPERTY);		
		DBBACKUPFOLDER =serviceProperties.getProperty(H2_DB_BACKUP_FOLDER_PROPERTY);
		DOMS_SOLR_SERVER = serviceProperties.getProperty(DOMS_SOLR_SERVER_PROPERTY);	
		
		log.info("Property:"+ H2_DB_FILE_PROPERTY +" = " + DBFILE );
		log.info("Property:"+ H2_DB_BACKUP_FOLDER_PROPERTY +" = "+ DBBACKUPFOLDER );
		log.info("Property:"+ DOMS_SOLR_SERVER_PROPERTY +" = "+ DOMS_SOLR_SERVER);
		
	}
	
}
