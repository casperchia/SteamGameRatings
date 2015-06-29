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
	public static String getSteamId(String username) {
		String json = null;
		try {
			json = readUrl("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + Constants.STEAM_KEY + "&vanityurl=" + username);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to getSteamId for " + username);
			return null;
		}
		Gson gson = new Gson();
		Constants.Page page = gson.fromJson(json, Constants.Page.class);
		return page.response.steamid;
	}	
	
}


