package dk.statsbiblioteket.doms.licensemodule.persistence;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.validation.LicenseValidator;

//Cache implementation that will reload all licenses every 15 minutes.
//However when the DB changes, the H2Storage class will fire a reload to this cache.
public class LicenseCache {

	// Cached instances
	private static ArrayList<License> cachedLicenses;
	private static ArrayList<ConfiguredDomLicenseGroupType> cachedDomLicenseGroupTypes;
	private static ArrayList<ConfiguredDomLicenseGroupType> cachedDomLicenseMustGroupTypes;
	private static ArrayList<ConfiguredDomAttributeType> cachedDomAttributeTypes;
	private static ArrayList<ConfiguredDomLicensePresentationType> cachedDomLicensePresentationTypes;
    private static HashMap<String, ConfiguredDomLicenseGroupType> groupIdMap;
    private static HashMap<String, ConfiguredDomLicensePresentationType> presentationTypeIdMap;
    
	private static final Logger log = LoggerFactory.getLogger(LicenseCache.class);
	private static final long reloadIntervalInSec = 15 * 1000 * 60L; // 15 minutes
	private static long lastReloadTime = 0;

	public static ArrayList<License> getAllLicense() {
		checkReload();
		return cachedLicenses;
	}

	public static ArrayList<ConfiguredDomLicenseGroupType> getConfiguredDomLicenseGroupTypes() {
		checkReload();
		return cachedDomLicenseGroupTypes;
	}

	public static ArrayList<ConfiguredDomLicenseGroupType> getConfiguredMUSTDomLicenseGroupTypes() {
		checkReload();
		return cachedDomLicenseMustGroupTypes;
	}

	public static ArrayList<ConfiguredDomAttributeType> getConfiguredDomAttributeTypes() {
		checkReload();
		return cachedDomAttributeTypes;

	}

	public static ArrayList<ConfiguredDomLicensePresentationType> getConfiguredDomLicenseTypes() {
		checkReload();
		return cachedDomLicensePresentationTypes;
	}

	private static synchronized void checkReload() {

		if (System.currentTimeMillis() - lastReloadTime > reloadIntervalInSec) {
			reloadCache();
		}
	}

	public static void reloadCache() {
	    LicenseModuleStorage storage =  null;
	    try {
			storage = new LicenseModuleStorage();
			log.info("Reloading cache from DB");
			lastReloadTime = System.currentTimeMillis();

			// Load all Licenses
			ArrayList<License> licenseList = new ArrayList<License>();
			ArrayList<License> names = storage.getAllLicenseNames();

			for (License current : names) {
				License license = storage.getLicense(current.getId());
				licenseList.add(license);
			}
			cachedLicenses = licenseList;
			log.debug("#licenses reload=" + cachedLicenses.size());

			// Load DomLicenseGroupTypes
			cachedDomLicenseGroupTypes = storage.getDomLicenseGroupTypes();

			// Load DomLicenseMustGroupTypes
			ArrayList<ConfiguredDomLicenseGroupType> allList = storage.getDomLicenseGroupTypes();
			cachedDomLicenseMustGroupTypes = LicenseValidator.filterMustGroups(allList);

			// Load DomAttributeTypes
			cachedDomAttributeTypes = storage.getDomAttributeTypes();
			
			
			// Load DomLicensePresentationTypes
			cachedDomLicensePresentationTypes = storage.getDomLicensePresentationTypes();
		
		    //create Dk2En name map
			groupIdMap = new HashMap<String,ConfiguredDomLicenseGroupType>();
			
            for (ConfiguredDomLicenseGroupType current : cachedDomLicenseGroupTypes){
            	groupIdMap.put(current.getKey(), current);            	
            }
            
            presentationTypeIdMap = new HashMap<String, ConfiguredDomLicensePresentationType>();
            for (ConfiguredDomLicensePresentationType current : cachedDomLicensePresentationTypes){
            	presentationTypeIdMap.put(current.getKey(), current);            	
            }
			
		
		} catch (Exception e) {
			log.error("Error in reload cache", e);
			throw new RuntimeException(e);
		}
		finally{
            storage.close();            
        }				
	}

	public static String getPresentationtypeName(String id, String locale){
		
		if (LicenseValidator.LOCALE_DA.equals(locale)){
			return presentationTypeIdMap.get(id).getValue_dk();
		}
		else if (LicenseValidator.LOCALE_EN.equals(locale)){
			return presentationTypeIdMap.get(id).getValue_en();
		}		
		return null; 	
	}
	
	public static String getGroupName(String id, String locale){

		if (LicenseValidator.LOCALE_DA.equals(locale)){
			return groupIdMap.get(id).getValue_dk();
		}
		else if (LicenseValidator.LOCALE_EN.equals(locale)){
			return groupIdMap.get(id).getValue_en();
		}			   
		return null; 	
	}
	
}
