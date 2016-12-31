import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {

	private List<String> cookies;
	private HttpsURLConnection conn;
	StringBuffer sb=new StringBuffer();
	static String string = "";
	
	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {

		String username;
		String password;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Please Enter Your Netid");
			
		username = br.readLine();
		
		System.out.println("Enter your password");
		
		password = br.readLine();
		
		String url = "https://cas.rutgers.edu/login?service=https://dn.rutgers.edu/Default.aspx";
		String dnrutgers = "https://dn.rutgers.edu/Default.aspx?pageid=transcript";

		Scraper http = new Scraper();

		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

		String page = http.GetPageContent(url);
		
		String postParams = http.getFormParams(page, username, password);
		
		http.sendPost(url, postParams);

		String result = http.GetPageContent(dnrutgers);
		ArrayList<String> current = getCurrentClasses();
		
		System.out.println("These are your classes.");	
		System.out.println(current);
					
	}
	
	
	private static ArrayList<String> getCurrentClasses(){
		ArrayList<String> temp = new ArrayList<>();
			
		String delims[] = string.split("return false");
		String classes[] = new String[delims.length-1];

		for (int i = 1; i < delims.length; i++) {
			String item = delims[i];
			item = item.substring(3, item.indexOf("<"));
			temp.add(item);
		}
		return temp;
		
	}
	
	private void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "accounts.google.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
			"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", "https://accounts.google.com/ServiceLoginAuth");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in =
	             new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// System.out.println(response.toString());

	  }
	
	public String getFormParams(String html, String username, String password)
			throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// Google form id
		Element loginform = doc.getElementById("content");
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("username"))
				value = username;
			else if (key.equals("password"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	  }

	private String GetPageContent(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			if(inputLine.contains("Fall 2016")){
				if(inputLine.contains("return false")){
					sb.append(inputLine);
				}
			}
		}
		in.close();

		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		string = sb.toString();
		return response.toString();

	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

}
