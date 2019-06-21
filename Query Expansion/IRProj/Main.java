import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
//		HttpSolrClient solr = new HttpSolrClient.Builder(urlString).build();
//		solr.setParser(new XMLResponseParser());
		
		String query = "chocolate";
		
		try {
		//JSONArray solrJSONObj = 
				
		String result=QueryExpansion.solrCall(query, 15);
		
		System.out.println(result);
		
//		JSONObject queryExpJSONObj = QueryExpansion.queryExpCall(query);
//		
//		JSONObject jsonObject = new JSONObject();
//		
//		jsonObject.put("qexp", queryExpJSONObj);
//		response.setCharacterEncoding("UTF-8");
//		response.getWriter().print(jsonObject.toString());
		
		}
	
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
