package dk.statsbiblioteket.doms.licensemodule.integrationtest;

import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import dk.statsbiblioteket.doms.licensemodule.solr.DomsSolrJClient;

//Integration test. Run manually
public class DomsSolrJClientTest {


	private static HttpSolrServer solrServer = new HttpSolrServer("http://localhost:57308/doms/sbsolr");
	
	
	public static void main(String[] args) throws Exception{
		
		ArrayList<String> ids = new ArrayList<String>(); 
		ids.add("doms_radioTVCollection:uuid:12efb195-194f-4795-bdc8-4efb2fa43152");//radio TV
		ids.add("doms_reklamefilm:uuid:b12445f8-8b88-4d32-bc14-d7494debb491"); //reklame
		String queryPartAccess="recordBase:doms_radioTVCollection";
		ArrayList<String> filteredIds = filterIds(ids, queryPartAccess);		
		System.out.println("Size:"+filteredIds.size());
	    System.out.println(filteredIds);		
	}
	
	
	//Basically same as method in DomsSolrJClient, but here I use URL to solrserver defined in this test 
    public static ArrayList<String> filterIds(  ArrayList<String> ids, String queryPartAccess) throws Exception{
        solrServer.setRequestWriter(new BinaryRequestWriter()); //To avoid http error code 413/414, due to monster URI. (and it is faster)        
        String queryPartStr= DomsSolrJClient.makeRecordbaseIdPart(ids);         
        
        System.out.println(queryPartStr);  
        SolrQuery query = new SolrQuery( queryPartStr);
        query.setFilterQueries(queryPartAccess);
        query.setFields("recordID");
        query.set("facet", "false");
        query.setRows(ids.size());
        QueryResponse response = solrServer.query(query);
        ArrayList<String> filteredIds = DomsSolrJClient.getIdsFromResponse(response);
        return filteredIds;     
    }
	
}
