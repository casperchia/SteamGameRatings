package steamParser;

import java.util.ArrayList;

public class Constants {
	public static final String STEAM_KEY = "5BFDD3211928F0C392EFBC80FA343E69";
	public static final String STEAM_APP_URL = "http://store.steampowered.com/app/";

	static class Page {
		Response response;
	}
	
	static class Response {
		String steamid;
		ArrayList<Game> games;
		int game_count;
	}
	
	static class Game {
		String appid;
		int playtime_forever;
	}
}
