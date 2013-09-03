package dk.statsbiblioteket.doms.licensemodule.listeners;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;
import dk.statsbiblioteket.doms.licensemodule.persistence.H2Storage;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitializationContextListener implements ServletContextListener {
	private static final Logger log = LoggerFactory.getLogger(InitializationContextListener.class);
	
	//this is called by the web-container before opening up for requests.(defined in web.xml)
	public void contextInitialized(ServletContextEvent event) {

		Properties props = new Properties();
		try 
		{
			props.load(InitializationContextListener.class.getResourceAsStream("/build.properties"));

			String version = props.getProperty("APPLICATION.VERSION");
			log.info("Licensemodule version "+version+" started successfully");
			
		} catch (Exception e) {
			log.error("failed to initialize service", e);
			throw new RuntimeException("failed to initialize service", e);
		}
	
	    //Initialize DB when used in a WEB container. 
		String dbFile=null;

		try {
		    dbFile=LicenseModulePropertiesLoader.DBFILE;			
			log.info("Connecting to H2 Database with DBfile:"+dbFile);
		    new H2Storage(dbFile); //Singleton
		    log.info("Connected");
		} 
		catch (Exception e) {
			e.printStackTrace();
			log.error("Could not connect to DBfile:"+dbFile+ ". ");
		} 			
	
	}

	//this is called by the web-container at shutdown. (defined in web.xml)
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			H2Storage.shutdown();
			log.info("Licensemodule H2database shutdown succesfully");
	
		} catch (Exception e) {
			log.error("failed to shutdown service", e);
		}
	}



}

