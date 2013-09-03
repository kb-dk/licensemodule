package dk.statsbiblioteket.doms.licensemodule.solr;

import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.LicenseModulePropertiesLoader;

public class DomsSolrJClient {
	private static HttpSolrServer solrServer;
	
	private static final Logger log = LoggerFactory.getLogger(DomsSolrJClient.class);

	static{	
	    
//
	    try{
			//Silent all the debugs log from HTTP Client (used by SolrJ)
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR"); 
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR"); 
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR"); 		
			java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF); 
			java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);


			solrServer = new HttpSolrServer(LicenseModulePropertiesLoader.DOMS_SOLR_SERVER);
			solrServer.setRequestWriter(new BinaryRequestWriter()); //To avoid http error code 413/414, due to monster URI. (and it is faster) 
		    	
		}
		catch(Exception e){
			System.out.println("Unable to connect to DOMs Solr server");
			e.printStackTrace();
			log.error("Unable to connect to DOMs Solr server",e);
		}
	}

	public static ArrayList<String>  filterIds(ArrayList<String> ids, String queryPartAccess) throws Exception{

		if (ids == null || ids.size() == 0){
			return new ArrayList<String>();
		}

		String queryStr= makeRecordbaseIdPart(ids);
 

		SolrQuery query = new SolrQuery( queryStr);        
		query.setFilterQueries(queryPartAccess);
		query.setFields("recordID");
		query.setRows(ids.size()); //default is 10 rows and id-list can be 20 etcs			   
        query.set("facet", "false"); //  Must be parameter set, because this java method does NOT work: query.setFacet(false);   
       
        QueryResponse response = solrServer.query(query);
		ArrayList<String> filteredIds = getIdsFromResponse(response);

		return filteredIds;

	}

	//Will also remove " from all ids to prevent Query-injection
	public static String makeRecordbaseIdPart (ArrayList<String> ids){
		StringBuilder queryIdPart = new StringBuilder();
		queryIdPart.append("(");
		for (int i = 0 ;i<ids.size();i++){
		String id = ids.get(i);
		//Remove all \ and " from the string
		  id= id.replaceAll("\\\\", "");
		  id= id.replaceAll("\\\"", "");
		
			queryIdPart.append("recordID:\""+id +"\"");
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
			String id = current.getFieldValue("recordID").toString();
			ids.add(id);
		}	
		return ids;
	}

}
