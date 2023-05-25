package dk.statsbiblioteket.doms.licensemodule.facade;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomAttributeType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseModuleStorage;
import dk.statsbiblioteket.doms.licensemodule.service.exception.InvalidArgumentServiceException;
import dk.statsbiblioteket.doms.licensemodule.util.AuditLog;
import dk.statsbiblioteket.doms.licensemodule.util.ChangeDifferenceText;
import dk.statsbiblioteket.doms.licensemodule.util.LicenseChangelogGenerator;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;

public class LicenseModuleFacade {

    static String catalinaBase= System.getProperty("catalina.base");                       
    static String logFileLocation = catalinaBase+"/logs/audit.log";    
    static Path auditLogFile = Paths.get(logFileLocation);
 
    
    private static final Logger log = LoggerFactory.getLogger(LicenseModuleFacade.class);

    public static void persistDomLicensePresentationType(String IP,String key, String value_dk, String value_en) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.persistDomLicensePresentationType(key, value_dk, value_en);
            ConfiguredDomLicensePresentationType newType = new ConfiguredDomLicensePresentationType(0, key, value_dk, value_en);
            ChangeDifferenceText changes = LicenseChangelogGenerator.getPresentationTypeChanges(null, newType);
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Create presentationtype", key, changes.getBefore(), changes.getAfter());
            appendToAuditLog(auditLog);
            
            
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

    public static void deleteLicense(String IP,long licenseId, boolean commit) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            
            License licenseToDelete = storage.getLicense(licenseId);
            storage.deleteLicense(licenseId, commit);

            ChangeDifferenceText changes = LicenseChangelogGenerator.getLicenseChanges(licenseToDelete,null);              
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Delete License", licenseToDelete.getLicenseName(), changes.getBefore(), changes.getAfter());               
            appendToAuditLog(auditLog);
        
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }
    }

    public static void persistDomLicenseGroupType(String IP,String key, String value, String value_en, String description, String description_en, String query, boolean mustGroup) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.persistDomLicenseGroupType(key, value, value_en, description, description_en, query, mustGroup);
            
            ConfiguredDomLicenseGroupType g = new ConfiguredDomLicenseGroupType(0L, key, value_en, value_en, description_en, description_en, query, mustGroup);
            ChangeDifferenceText changes = LicenseChangelogGenerator.getGroupTypeChanges(null, g);
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Create grouptype", key, changes.getBefore(), changes.getAfter());    
            appendToAuditLog(auditLog);
        
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }
    }

    public static void updateDomLicenseGroupType(String IP, long id, String value_dk, String value_en, String description, String description_en, String query, boolean mustGroup) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {

            ConfiguredDomLicenseGroupType oldGroupType= getGroupTypeById(id);
            
            storage.updateDomLicenseGroupType(id, value_dk, value_en, description, description_en, query, mustGroup);
            ConfiguredDomLicenseGroupType updateGroupType = new ConfiguredDomLicenseGroupType(id,oldGroupType.getKey(), value_dk, value_en, description_en, description_en, query, mustGroup);
                        
            ChangeDifferenceText changes = LicenseChangelogGenerator.getGroupTypeChanges(oldGroupType, updateGroupType);
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Update grouptype", value_dk, changes.getBefore(), changes.getAfter());        
            appendToAuditLog(auditLog);
                    
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }

    public static void updateDomPresentationType(String IP, long id, String value_dk, String value_en) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            ConfiguredDomLicensePresentationType oldPresentationType = getPresentationTypeById(id);
            storage.updateDomPresentationType(id, value_dk, value_en);
                        
            ConfiguredDomLicensePresentationType  newType = new ConfiguredDomLicensePresentationType(id,oldPresentationType.getKey(),value_dk,value_en); 
            ChangeDifferenceText changes = LicenseChangelogGenerator.getPresentationTypeChanges(oldPresentationType , newType);
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Update presentationtype", oldPresentationType .getKey(), changes.getBefore(), changes.getAfter());
            appendToAuditLog(auditLog);
            
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static void deleteDomLicenseGroupType(String IP,String groupName) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.deleteDomLicenseGroupType(groupName);
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Delete grouptype",groupName,groupName,"");
            appendToAuditLog(auditLog);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    
    public static void deleteDomPresentationType(String IP, String presentationName) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
            storage.deleteDomPresentationType(presentationName);
                                    
            ConfiguredDomLicensePresentationType oldType = getGroupTypeByPresentationName(presentationName);           
            ChangeDifferenceText changes = LicenseChangelogGenerator.getPresentationTypeChanges(oldType, null);
            AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Delete presentationtype", oldType.getKey(), changes.getBefore(), changes.getAfter());
            appendToAuditLog(auditLog);
            
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static synchronized void persistLicense(String IP,License license)  throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
         
            AuditLog auditLog = null;
            //audit log
            if (license.getId() == 0 ) {
               ChangeDifferenceText changes = LicenseChangelogGenerator.getLicenseChanges(null,license);              
               auditLog = new AuditLog(System.currentTimeMillis(),IP,"Create New License", license.getLicenseName(), changes.getBefore(), changes.getAfter());               

            }
            else {
               License oldLicense = storage.getLicense(license.getId());
               ChangeDifferenceText changes = LicenseChangelogGenerator.getLicenseChanges(oldLicense, license);
               auditLog = new AuditLog(System.currentTimeMillis(),IP,"Update License", license.getLicenseName(), changes.getBefore(), changes.getAfter());                                               
            }
            
            storage.persistLicense(license);                    
            appendToAuditLog(auditLog);   
                        
            
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
    
    public static void persistDomAttributeType(String IP, String value) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           storage.persistDomAttributeType(value);
           AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Create attribute", value,"",value);
           appendToAuditLog(auditLog);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        } finally {
            storage.close();
        }

    }
    
    public static void deleteDomAttributeType(String IP,String attributeTypeName) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();
        try {
           storage.deleteDomAttributeType(attributeTypeName);
           AuditLog auditLog = new AuditLog(System.currentTimeMillis(),IP,"Delete attribute", attributeTypeName,attributeTypeName,"");
           appendToAuditLog(auditLog);
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
    
    
    
  private static ConfiguredDomLicensePresentationType getGroupTypeByPresentationName(String presentationName)  throws Exception{
        
        LicenseModuleStorage storage = new LicenseModuleStorage();
        ArrayList<ConfiguredDomLicensePresentationType> all = storage.getDomLicensePresentationTypes();
        
        for (ConfiguredDomLicensePresentationType current : all) {
            if (current.getKey() == presentationName) {
              return current;
            }
        }
        
        throw new InvalidArgumentServiceException("No ConfiguredDomLicensePresentationType with presenstationname/key:"+presentationName);        
    }
    
        
    private static ConfiguredDomLicenseGroupType getGroupTypeById(long id)  throws Exception{
        
        LicenseModuleStorage storage = new LicenseModuleStorage();
        ArrayList<ConfiguredDomLicenseGroupType> all = storage.getDomLicenseGroupTypes();
        
        for (ConfiguredDomLicenseGroupType current : all) {
            if (current.getId() == id) {
              return current;
            }
        }
        
        throw new InvalidArgumentServiceException("No ConfiguredDomLicenseGroupType with Id:"+id);        
    }
    
    
    private static ConfiguredDomLicensePresentationType getPresentationTypeById(long id) throws Exception {
        LicenseModuleStorage storage = new LicenseModuleStorage();      
       ArrayList<ConfiguredDomLicensePresentationType> allPresentationTypes = storage.getDomLicensePresentationTypes();
        
        
        for (ConfiguredDomLicensePresentationType current : allPresentationTypes) {
            if (current.getId() == id) {        
              return current;
            }
        }
        
        throw new InvalidArgumentServiceException("No ConfiguredDomLicensePresentationType with Id:"+id);
        
    }
    
    
    private synchronized static void appendToAuditLog(AuditLog logEntry) throws Exception {
        if (!Files.exists(auditLogFile, LinkOption.NOFOLLOW_LINKS)) {
              Files.createFile(auditLogFile);
        }
        
        StringBuffer auditLogBuffer= new StringBuffer();
        auditLogBuffer.append("---------------------------------------------------------------------------------------------------------------------------------------------\n");
        auditLogBuffer.append("Date:"+new Date(logEntry.getMillis())+"\n");
        auditLogBuffer.append("Millis:"+logEntry.getMillis()+"\n");
        auditLogBuffer.append("User:"+logEntry.getUsername()+"\n");
        auditLogBuffer.append("ChangeType:"+logEntry.getChangeType()+"\n");
        auditLogBuffer.append("Objectname:"+logEntry.getObjectName()+"\n");
        auditLogBuffer.append("Values before:\n");
        auditLogBuffer.append(logEntry.getTextBefore() +"\n");
        auditLogBuffer.append("Values after:\n");
        auditLogBuffer.append(logEntry.getTextAfter() +"\n");               
        Files.write(auditLogFile, auditLogBuffer.toString().getBytes("UTF-8"), StandardOpenOption.APPEND);         
    }
        
}
