package dk.statsbiblioteket.doms.licensemodule.listeners;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseModuleStorage;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitializationContextListener implements ServletContextListener {
  
	private  final Logger log = LoggerFactory.getLogger(getClass());
    private static String version;	
	//this is called by the web-container before opening up for requests.(defined in web.xml)
	public void contextInitialized(ServletContextEvent event) {


	    log.info("Licensemodule starting up...");
		try 
		{
          InitialContext ctx = new InitialContext();
          String configFile = (String) ctx.lookup("java:/comp/env/application-config");
          version =  getClass().getPackage().getImplementationVersion();
          
							    					
		  LicenseModulePropertiesLoader.init(configFile); //Load properties
		  log.info("Licensemodule version "+version+" started successfully");
		  
		} catch (Exception e) {
			log.error("failed to initialize service", e);
			throw new RuntimeException("failed to initialize service", e);
		}
	

		try {		    		
			LicenseModuleStorage.initialize(LicenseModulePropertiesLoader.DB_DRIVER, 
			        LicenseModulePropertiesLoader.DB_URL,
			        LicenseModulePropertiesLoader.DB_USERNAME,
			        LicenseModulePropertiesLoader.DB_PASSWORD
            );
            					
		} 
		catch (Exception e) {
		    log.error("failed to initialize service", e);
            throw new RuntimeException("failed to initialize service", e);
		} 			
	
	}

	//this is called by the web-container at shutdown. (defined in web.xml)
    public void contextDestroyed(ServletContextEvent sce) {
        try {        
            LicenseModuleStorage.shutdown();

            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();

                try {
                    log.debug("deregistering jdbc driver: {}", driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    log.debug("Error deregistering driver {}", driver, e);
                }
            }
            log.info("Licensemodule "+version+" shutdown success");

        } catch (Exception e) {
            log.error("failed to shutdown Licensemodule", e);
        }
                
    }

	



}

