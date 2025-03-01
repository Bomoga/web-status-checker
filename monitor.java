import java.io.*;
import java.net.*;
import java.util.*;

public class monitor {

	public static void main(String[] args) {
	        // Specify the file path directly (hard-coded)
	        String filename = "URLfile.txt"; //You can adjust this to the exact path where your file is stored
	
	        List<String> urls = readUrlsFromResource(filename);
	        if (urls.isEmpty()) {
	            System.out.println("No URLs found or error reading the file.");
	            return;
	        }
	
	        for (String url : urls) {
	            	try {
	                	handleRequest(url);
	            	} 
			catch (Exception e) {
	                	System.out.println("URL: " + url + "\nStatus: Network Error");
	            	}
	        }
    	}

	private static List<String> readUrlsFromResource(String filename) {
	        List<String> urls = new ArrayList<>();
	        ClassLoader classLoader = monitor.class.getClassLoader();
	
	        try (BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(filename)))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                urls.add(line.trim());  // Add each URL to the list
	            }
	        } catch (IOException | NullPointerException e) {
	            System.out.println("Error reading file from resource: " + filename);
	        }
	        return urls;
    	}

    	private static void handleRequest(String url) throws IOException {
	        // Determine the protocol and establish a connection
	        URL parsedUrl = new URL(url);
	        HttpURLConnection connection = (HttpURLConnection) parsedUrl.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setInstanceFollowRedirects(false); // Disable automatic redirects
	
	        // Try to connect and get the response
	        try {
	            connection.connect();
	            int statusCode = connection.getResponseCode();
	            System.out.println("URL: " + url);
	            System.out.println("Status: " + statusCode + " " + connection.getResponseMessage());
	
	            // Handle redirects
	            if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
	                String redirectUrl = connection.getHeaderField("Location");
	                System.out.println("Redirected URL: " + redirectUrl);
	                handleRequest(redirectUrl); // Follow the redirect
	            }
	
	            // Handle referencing URLs (fetch the body to find references)
	            if (statusCode == HttpURLConnection.HTTP_OK) {
	                handleReferencedUrls(connection);
	            }
	
	        } 
	        catch (IOException e) {
	            System.out.println("Status: Network Error");
	        } 
	        finally {
	            connection.disconnect();
	        }
    	}

    	private static void handleReferencedUrls(HttpURLConnection connection) throws IOException {
	        // Reading the response body to find referenced URLs
	        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
	            String inputLine;
	            while ((inputLine = in.readLine()) != null) {
	                
	                if (inputLine.contains("src=\"")) {
	                    int start = inputLine.indexOf("src=\"") + 5;
	                    int end = inputLine.indexOf("\"", start);
	                    if (end > start) {
	                        String referencedUrl = inputLine.substring(start, end);
	                        System.out.println("Referenced URL: " + referencedUrl);
	                        handleRequest(referencedUrl); // Check the status of the referenced URL
	                    }
	                }
	                
	                if (inputLine.contains("href=\"")) {
	                    int start = inputLine.indexOf("href=\"") + 6;
	                    int end = inputLine.indexOf("\"", start);
	                    if (end > start) {
	                        String hrefUrl = inputLine.substring(start, end);
	                        System.out.println("Referenced URL: " + hrefUrl);
	                        handleRequest(hrefUrl); // Check the status of the referenced URL
	                    }
	                }
	            }
	        }
    	}
}
