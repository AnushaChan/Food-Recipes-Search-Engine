import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;


public class QueryExpansion {
	
	public static String solrCall(String searchText, int maxRows) throws IOException,
		JSONException {

		//searchText = searchText.replaceAll(" ", "_");
//		final String solrQuery = "http://127.0.0.1:8983/solr/nutch/select?q=url:"
//				+ URLEncoder.encode(searchText, "UTF-8")
//				+ "&rows="+maxRows+"&wt=json&indent=true";
		
		final String solrQuery="http://localhost:8983/solr/nutch/select?q=content:"
				+ URLEncoder.encode(searchText, "UTF-8")
				+ "&rows="+maxRows+"&wt=json&indent=true";
		
		//System.out.println(URLEncoder.encode(searchText, "UTF-8"));
		//System.out.println(solrQuery);
		final URL url = new URL(solrQuery);
		final URLConnection connection = url.openConnection();
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;
		final StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		JSONObject items = null;
		JSONArray dataArray = null;
		JSONArray arrObj = null;
		String result =  null;
		
		if (response != null) {
			//System.out.println(response.toString());
			JSONObject obj = new JSONObject(response.toString());
			// System.out.println(response.toString());
			items = obj.getJSONObject("response");
			dataArray = items.getJSONArray("docs");
			
			//System.out.println(items.toString());
			//System.out.println(dataArray.toString());
			
			System.out.println(dataArray.length());
			//System.out.println(dataArray.get(5));
			
			final String[] documents = new String[10];
			for (int i = 0; i < 10; i++)
			{
				try {
					
					//System.out.println(dataArray.getJSONObject(i).get("content"));
					documents[i] = dataArray.getJSONObject(i).get("content").toString();
				} catch (final Exception e) {
					break;
				}
			}
//			for (int i = 0; i < 10; i++)
//			{
//				System.out.println(documents[i]);
//			}
			
			//final File stopwords = new File(cmd.getOptionValue("stop"));
			
			final File stopwords = new File("stopwords");
					
			final Parser parser = new Parser(stopwords);
			parser.parse(Arrays.asList(documents));
			// Display results
			final HashMap<String, Map<Integer, Integer>> tokenMap = parser.getTokenMap();
			
			//System.out.println(tokenMap.size());
			
			final String wordnet = "dict";
			
			final Stemming stemming = new Stemming(wordnet, searchText);
			
			//System.out.println(stemming);
			
			stemming.stem(tokenMap);
			final Map<String, Set<String>> stemsMap = stemming.getStemsMap();
			
			//System.out.println(stemsMap.size());
			
			//System.out.println(scalarClusters(tokenMap, stemsMap, searchText));
			
			final Element[][] elements = metricClusters(tokenMap, stemsMap, searchText);
			
			final List<Element> list = new ArrayList<>();
			for (final Element[] elements2 : elements) {
				for (final Element element : elements2) {
					if (element != null) {
						list.add(element);
					}
				}
			}

			Collections.sort(list, new Comparator<Element>() {
				@Override
				public int compare(final Element o1, final Element o2) {
					return o1.value >= o2.value ? 1 : -1;
				}
			});

			final LinkedHashSet<String> set = new LinkedHashSet<>();
			for (int i = list.size() - 1; i > 0; i--) {
				set.add(list.get(i).v);
			}
			
			result = searchText + " " + String.join(" ", set);
		}
		
		return result;
	}


		
	public static Element[][] metricClusters(final HashMap<String, Map<Integer, Integer>> tokenMap, final Map<String, Set<String>> stemsMap, final String query) {
		final Element[][] matrix = new Element[stemsMap.size()][stemsMap.size()];
		final String[] stems = stemsMap.keySet().toArray(new String[stemsMap.size()]);
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				final Set<String> iStrings = stemsMap.get(stems[i]);
				final Set<String> jStrings = stemsMap.get(stems[j]);
				for (final String string1 : iStrings) {
					for (final String string2 : jStrings) {
						final Map<Integer, Integer> iMap = tokenMap.get(string1);
						final Map<Integer, Integer> jMap = tokenMap.get(string2);
						for (final Integer integer : iMap.keySet()) {
							if (jMap.containsKey(integer)) {
								cuv += 1.0 / Math.abs(iMap.get(integer) - jMap.get(integer));
							}
						}
					}
				}

				matrix[i][j] = new Element(stems[i], stems[j], cuv);
				//System.out.println(matrix[i][j]);
			}
		}

		final Element[][] norm = new Element[stemsMap.size()][stemsMap.size()];
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				if (matrix[i][j] != null) {
					cuv = matrix[i][j].value / (stemsMap.get(stems[i]).size() * stemsMap.get(stems[j]).size());
				}

				norm[i][j] = new Element(stems[i], stems[j], cuv);
			}
		}

		return printTopN(norm, stems, query, tokenMap, stemsMap);
	}
	
	
	
	static Element[][] printTopN(final Element[][] metric,
			final String[] stems,
			final String query,
			final HashMap<String, Map<Integer, Integer>> tokenMap,
			final Map<String, Set<String>> stemsMap) {
		final Set<String> strings = new HashSet<>();
		strings.addAll(Arrays.asList(query.split(" ")));

		final Element[][] elements = new Element[strings.size()][3];
		int index = 0;
		for (final String string : strings) {
			final PriorityQueue<Element> queue = new PriorityQueue<>(3, new Comparator<Element>() {

				@Override
				public int compare(final Element o1, final Element o2) {
					return o1.value >= o2.value ? 1 : -1;
				}
			});

			final int i = find(stems, string);
			if (i == -1) {
				continue;
			}

			for (int j = 0; j < metric[i].length; j++) {
				if (metric[i][j] == null || strings.contains(metric[i][j].u) && !metric[i][j].u.equals(string) || strings.contains(metric[i][j].v) &&
						!metric[i][j].v.equals(string)) {
					continue;
				}

				if (tokenMap.containsKey(metric[i][j].v)) {
					queue.add(metric[i][j]);
				} else {
					queue.add(new Element(metric[i][j].u, stemsMap.get(metric[i][j].v).iterator().next(), metric[i][j].value));
				}
				if (queue.size() > 3) {
					queue.poll();
				}
			}

			elements[index++] = queue.toArray(new Element[3]);
		}

		return elements;
	}

	public static int find(final String[] arr, final String string) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equalsIgnoreCase(string)) {
				return i;
			}
		}

		return -1;
	}

	static class Element {
		String	u;
		String	v;
		double	value;

		public Element() {
		}

		public Element(final String u, final String v, final double value) {
			this.u = u;
			this.v = v;
			this.value = value;
		}

		@Override
		public String toString() {
			return this.u + " " + this.v + " : " + this.value;
		}
	}
	
	public static String scalarClusters(final HashMap<String, Map<Integer, Integer>> tokenMap, final Map<String, Set<String>> stemsMap, final String query) {
		final Element[][] matrix = new Element[stemsMap.size()][stemsMap.size()];
		final String[] stems = stemsMap.keySet().toArray(new String[stemsMap.size()]);
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
//				double s_vector[]=1.0 ;
				int count=0;
				final Set<String> iStrings = stemsMap.get(stems[i]);
				final Set<String> jStrings = stemsMap.get(stems[j]);
				for (final String string1 : iStrings) {
					for (final String string2 : jStrings) {
						final Map<Integer, Integer> iMap = tokenMap.get(string1);
						final Map<Integer, Integer> jMap = tokenMap.get(string2);
						for (final Integer integer : iMap.keySet()) {
							if (jMap.containsKey(integer)) {
//								s_vector[count++]=1.0 / Math.abs(iMap.get(integer) - jMap.get(integer));
								cuv += 1.0 / Math.abs(iMap.get(integer) - jMap.get(integer));
							}
						}
					}
				}

				matrix[i][j] = new Element(stems[i], stems[j], cuv);
				//System.out.println(matrix[i][j]);
			}
			
			//System.out.println("Matrix:" + matrix);
		}
		
		int xrow = stems.length;
		double[] y = new double[xrow];  
		
		HashMap<String, double[]> mapped_cluster_structure = new HashMap<String, double[]>();
		
		final Element[][] norm = new Element[stemsMap.size()][stemsMap.size()];
		for (int i = 0; i < stems.length; i++) {
			for (int j = 0; j < stems.length; j++) {
				if (i == j) {
					continue;
				}

				double cuv = 0.0;
				if (matrix[i][j] != null) {
					cuv = matrix[i][j].value / (stemsMap.get(stems[i]).size() * stemsMap.get(stems[j]).size());
					y[j] = matrix[i][j].value;
				}

				norm[i][j] = new Element(stems[i], stems[j], cuv);
			}
			mapped_cluster_structure.put(stems[i],y) ;
		}
		
		//System.out.println(mapped_cluster_structure);
		//return printTopN(norm, stems, query, tokenMap, stemsMap);
		
		double[] res = mapped_cluster_structure.get(query);
		String query_ex= null;
		if(mapped_cluster_structure.containsKey(query)) {
			//System.out.println("Yes");
			double[] a = mapped_cluster_structure.get(query);
			
			
			
			for (int i = 0; i < stems.length; i++) {
				int cuv=0;
				double[] b = mapped_cluster_structure.get(stems[i]);
				for (int j=0 ; j<a.length; j++) {
					//System.out.println(a[j]);
					cuv += a[j]* b[j] ;
					//System.out.println(cuv);
				}
				res[i]=cuv/a.length*b.length;
			}
			double max_val=0;
			
			for (int i = 0; i < res.length; i++) {
				//System.out.println(res[i]);
				if (max_val < res[i]) {
					max_val = res[i];
					query_ex=stems[i];
					System.out.println(max_val);
			}
			
		}
		}
		return query_ex;
	}
}