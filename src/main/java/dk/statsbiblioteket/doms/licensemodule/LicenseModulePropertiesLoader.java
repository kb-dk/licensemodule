package dk.statsbiblioteket.doms.licensemodule;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.solr.SolrServerClient;

public class LicenseModulePropertiesLoader {
	
	private static final Logger log = LoggerFactory.getLogger(LicenseModulePropertiesLoader.class);
	private static final String LICENSEMODULE_PROPERTY_FILE = "license_module.properties";
	 
	
	private static final String DB_DRIVER_PROPERTY="licensemodule.database.driver"; 
	private static final String DB_URL_PROPERTY="licensemodule.database.url";
	private static final String DB_USERNAME_PROPERTY="licensemodule.database.username";
    private static final String DB_PASSWORD_PROPERTY="licensemodule.database.password";
	
	
	private static final String SOLR_SERVERS_PROPERTY = "licensemodule.solr.servers";
	private static final String SOLR_FILTER_FIELD_PROPERTY = "licensemodule.solr.filter.field";
	
	
    public static String DB_DRIVER = null; 
    public static String DB_URL  = null;
    public static String DB_USERNAME = null;
    public static String DB_PASSWORD = null;

	public static String DBBACKUPFOLDER = null;
	public static ArrayList<SolrServerClient> SOLR_SERVERS = null;
	public static String SOLR_FILTER_FIELD = null;
	
    public static void init(){
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
		DB_DRIVER= serviceProperties.getProperty(DB_DRIVER_PROPERTY);
        DB_URL = serviceProperties.getProperty(DB_URL_PROPERTY);
        DB_USERNAME = serviceProperties.getProperty(DB_USERNAME_PROPERTY);
        DB_PASSWORD = serviceProperties.getProperty(DB_PASSWORD_PROPERTY);            				
		SOLR_FILTER_FIELD = serviceProperties.getProperty(SOLR_FILTER_FIELD_PROPERTY); 
		 
		String solr_servers=serviceProperties.getProperty(SOLR_SERVERS_PROPERTY);	
		StringTokenizer tokens = new StringTokenizer(solr_servers, ",");  
		SOLR_SERVERS  = new ArrayList<SolrServerClient>(); 
		
		while (tokens.hasMoreTokens()){
		  SOLR_SERVERS.add(new SolrServerClient(tokens.nextToken().trim()));    		   
		}		
		
		
        log.info("Property:"+ DB_DRIVER_PROPERTY +" = "+ DB_DRIVER);
	    log.info("Property:"+ DB_URL_PROPERTY +" = "+ DB_URL);
	    log.info("Property:"+ DB_USERNAME_PROPERTY +" = "+ DB_USERNAME);
	    log.info("Property:"+ DB_PASSWORD_PROPERTY +" = *******");      
	    	
		log.info("Property:"+ SOLR_FILTER_FIELD_PROPERTY +" = "+  SOLR_FILTER_FIELD);
	    log.info("Property:"+ SOLR_SERVERS_PROPERTY +" = "+  solr_servers);
	    log.info("Number of solr servers:"+SOLR_SERVERS.size());
	    
	}

    
    //For unittest
    public static void setSOLR_FILTER_FIELD(String sOLR_FILTER_FIELD) {
        SOLR_FILTER_FIELD = sOLR_FILTER_FIELD;
    }
	
}
