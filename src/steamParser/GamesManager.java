package steamParser;

import steamParser.Constants.Game;

import com.google.gson.Gson;

/**
 * @author Casper
 *
 */
public class GamesManager {
	
	public static void printGames(String steamid) throws Exception {
		String json = SteamIdManager.readUrl("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + Constants.STEAM_KEY + "&steamid=" + steamid + "&format=json");
		Gson gson = new Gson();
		Constants.Page page = gson.fromJson(json, Constants.Page.class);
		for(Game game : page.response.games) {
			System.out.println("appid: "+ game.appid);
			System.out.println("Playtime: "+ game.playtime_forever);
		}
	}
	
	
}
