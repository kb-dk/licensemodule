package dk.statsbiblioteket.doms.licensemodule.integrationtest;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class HttpClientPoster {

  /*
public static String postJSON(String url, String content) throws Exception {
	    
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", "Dokumentleveringweb XML client");

		BufferedReader br = null;

		//TODO property
		PostMethod method = new PostMethod(url);

		method.addRequestHeader("Content-Type", "application/json;charset=UTF-8");		
		method.setRequestBody(content); // How to do this a non-deprecated way?

		try {
			int httpCode = client.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			
			
		 String readLine;
		StringBuilder response = new StringBuilder();		
		 while (((readLine = br.readLine()) != null)) {
					response.append(readLine);

				}
		return response.toString();	
			
		} catch (Exception e) {
			throw e;
		} finally {
			method.releaseConnection();
			if (br != null)
				try {
					br.close();
				} catch (Exception fe) {
				fe.printStackTrace();
				}
		}


	}
*/	
	
}
