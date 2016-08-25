package dk.statsbiblioteket.doms.licensemodule.facade;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomAttributeType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseModuleStorage;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;

public class LicenseModuleFacade {

    private static final Logger log = LoggerFactory.getLogger(LicenseModuleFacade.class);

    public static void persistDomLicensePresentationType(String key, String value_dk, String value_en) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.persistDomLicensePresentationType(key, value_dk, value_en);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }
    }

    public static ArrayList<ConfiguredDomLicensePresentationType> getDomLicensePresentationTypes() throws Exception {

        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            return storage.getDomLicensePresentationTypes();
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }
    }

    public static void deleteLicense(long licenseId, boolean commit) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.deleteLicense(licenseId, commit);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }
    }

    public static void persistDomLicenseGroupType(String key, String value, String value_en, String description, String description_en, String query, boolean mustGroup) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.persistDomLicenseGroupType(key, value, value_en, description, description_en, query, mustGroup);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }
    }

    public static void updateDomLicenseGroupType(long id, String value_dk, String value_en, String description, String description_en, String query, boolean mustGroup) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.updateDomLicenseGroupType(id, value_dk, value_en, description, description_en, query, mustGroup);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }

    public static void updateDomPresentationType(long id, String value_dk, String value_en) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.updateDomPresentationType(id, value_dk, value_en);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static void deleteDomLicenseGroupType(String groupName) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.deleteDomLicenseGroupType(groupName);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    
    public static void deleteDomPresentationType(String presentationName) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.deleteDomPresentationType(presentationName);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static void persistLicense(License license)  throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.persistLicense(license);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    

    
    public static ArrayList<ConfiguredDomLicenseGroupType> getDomLicenseGroupTypes() throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           return storage.getDomLicenseGroupTypes();
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static void persistDomAttributeType(String value) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           storage.persistDomAttributeType(value);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static void deleteDomAttributeType(String attributeTypeName) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           storage.deleteDomAttributeType(attributeTypeName);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
     
    
    public static ArrayList<ConfiguredDomAttributeType> getDomAttributeTypes() throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           return storage.getDomAttributeTypes();
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static ArrayList<License> getAllLicenseNames() throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           return storage.getAllLicenseNames();
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static License getLicense(long licenseId)throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           return storage.getLicense(licenseId);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    

// License getLicense(long licenseId) throws Exception {
    

}
