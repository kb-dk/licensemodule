package dk.statsbiblioteket.doms.licensemodule.solr;

import java.util.ArrayList;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

public class AbstractSolrJClient {

  //Will also remove " from all ids to prevent Query-injection
    public static String makeAuthIdPart (ArrayList<String> ids){
        StringBuilder queryIdPart = new StringBuilder();
        queryIdPart.append("(");
        for (int i = 0 ;i<ids.size();i++){
        String id = ids.get(i);
        //Remove all \ and " from the string
          id= id.replaceAll("\\\\", "");
          id= id.replaceAll("\\\"", "");
        
            queryIdPart.append("authID:\""+id +"\"");
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
            String id = current.getFieldValue("authID").toString();
            ids.add(id);
        }   
        return ids;
    }
    
}
