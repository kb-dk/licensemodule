package dk.statsbiblioteket.doms.licensemodule.solr;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;

public class AbstractSolrJClient {

    private static String filterField;
    protected HttpSolrServer solrServer;
    static{ 
        //Silent all the debugs log from HTTP Client (used by SolrJ)
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR"); 
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR"); 
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");        
        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF); 
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);
   
        filterField=LicenseModulePropertiesLoader.SOLR_FILTER_FIELD;
    }

  
    
    public ArrayList<String> filterIds(ArrayList<String> ids, String queryPartAccess) throws Exception{

        if (ids == null || ids.size() == 0){
            return new ArrayList<String>();
        }

        String queryStr= makeAuthIdPart(ids);
 
        SolrQuery query = new SolrQuery( queryStr);        
        query.setFilterQueries(queryPartAccess);
        query.setFields();
        query.setRows(ids.size()); //default is 10 rows and id-list can be 20 etcs             
        query.set("facet", "false"); //  Must be parameter set, because this java method does NOT work: query.setFacet(false);   
       
        QueryResponse response = solrServer.query(query);
        ArrayList<String> filteredIds = getIdsFromResponse(response);

        return filteredIds;

    }
     
    
    //Will also remove " from all ids to prevent Query-injection
    public static String makeAuthIdPart (ArrayList<String> ids){
        StringBuilder queryIdPart = new StringBuilder();
        queryIdPart.append("(");
        for (int i = 0 ;i<ids.size();i++){
            String id = ids.get(i);
            //Remove all \ and " from the string
            id= id.replaceAll("\\\\", "");
            id= id.replaceAll("\\\"", "");

            queryIdPart.append(filterField+":\""+id +"\"");
            if (i<ids.size()-1){
                queryIdPart.append(" OR ");
            }                       
        }
        queryIdPart.append(")");
        return queryIdPart.toString();
    }

    public static ArrayList<String> getIdsFromResponse(QueryResponse response){
        ArrayList<String> ids= new ArrayList<String>();

        for (SolrDocument current : response.getResults()){
             Collection<Object> fieldValues = current.getFieldValues(filterField); //Multivalued
             for (Object idFound : fieldValues){
                 ids.add(idFound.toString());                 
             }                           
        }   
        return ids;
    }


    public HttpSolrServer getSolrServer() {
        return solrServer;
    }

   
}
