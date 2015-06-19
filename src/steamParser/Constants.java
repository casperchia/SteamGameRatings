package steamParser;

public class Constants {
	public static final String STEAM_KEY = "5BFDD3211928F0C392EFBC80FA343E69";

	static class Page {
		Response response;
	}
	
	static class Response {
		String steamid;
		Games games;
		int game_count;
	}
	
	static class Games {
		String appid;
		int playtime_forever;
	}
}
