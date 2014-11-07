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

public class DomsSolrJClient extends AbstractSolrJClient{
	private static HttpSolrServer domsSolrServer;
	
	private static final Logger log = LoggerFactory.getLogger(DomsSolrJClient.class);

	static{	
	    
	    try{
			//Silent all the debugs log from HTTP Client (used by SolrJ)
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR"); 
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR"); 
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR"); 		
			java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF); 
			java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);

			domsSolrServer = new HttpSolrServer(LicenseModulePropertiesLoader.DOMS_SOLR_SERVER);
			domsSolrServer.setRequestWriter(new BinaryRequestWriter()); //To avoid http error code 413/414, due to monster URI. (and it is faster) 
			
	   }
		catch(Exception e){
			System.out.println("Unable to connect to doms Solr server");
			e.printStackTrace();
			log.error("Unable to connect to doms Solr server",e);
		}
	}

	public static ArrayList<String>  filterIds(ArrayList<String> ids, String queryPartAccess) throws Exception{

		if (ids == null || ids.size() == 0){
			return new ArrayList<String>();
		}

		String queryStr= makeAuthIdPart(ids);
 
		SolrQuery query = new SolrQuery( queryStr);        
		query.setFilterQueries(queryPartAccess);
		query.setFields("authID");
		query.setRows(ids.size()); //default is 10 rows and id-list can be 20 etcs			   
        query.set("facet", "false"); //  Must be parameter set, because this java method does NOT work: query.setFacet(false);   
       
        QueryResponse response = domsSolrServer.query(query);
		ArrayList<String> filteredIds = getIdsFromResponse(response);

		return filteredIds;

	}

	

}
