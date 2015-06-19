package steamParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;

/**
 * @author Casper
 *
 */
public class SteamIdManager {
	
	private static String steamKey = "5BFDD3211928F0C392EFBC80FA343E69";

	static class Page {
		Response response;
	}
	
	static class Response {
		String steamid;
	}
	
	/**
	 * @param urlString 
	 * @return Source html of website in string
	 * @throws Exception
	 */
	public static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	/**
	 * Gets steam ID of user from given VanityUrl.
	 * @param username VanityUrl of user
	 * @return Steam ID. Null if no such user found.
	 * @throws Exception
	 */
	public static String getSteamId(String username) throws Exception{
		String json = readUrl("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + steamKey + "&vanityurl=" + username);
		Gson gson = new Gson();
		Page page = gson.fromJson(json, Page.class);
		return page.response.steamid;
	}	
	
}


