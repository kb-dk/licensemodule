package dk.statsbiblioteket.doms.licensemodule.solr;

import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SolrServerClient extends AbstractSolrJClient{

    private static final Logger log = LoggerFactory.getLogger(SolrServerClient .class);

    public SolrServerClient (String serverUrl){
        try{           
            solrServer = new HttpSolrServer(serverUrl);
            solrServer.setRequestWriter(new BinaryRequestWriter()); //To avoid http error code 413/414, due to monster URI. (and it is faster)                
        }
        catch(Exception e){            
            log.error("Unable to connect to solr-server:"+serverUrl,e);
        }


    }

}

