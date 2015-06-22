package steamParser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		
		String url = "jdbc:postgresql://localhost/steamdb";
		String user = "casper";
		String password = "qwer";
		
		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			rs = st.executeQuery("SELECT * from games");
			
			if (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;		
		} finally {
			if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (con != null) {
                con.close();
            }

		}
		
		
		for (Game game : page.response.games) {
			System.out.println("appid: "+ game.appid);
			
//			System.out.println(SteamIdManager.readUrl(Constants.STEAM_APP_URL + game.appid));			
			String html = SteamIdManager.readUrl(Constants.STEAM_APP_URL + game.appid);
			Pattern pattern = Pattern.compile("user_reviews_count\">\\((.*)\\)</span>");
			Matcher matcher = pattern.matcher(html);
			
			
			if(matcher.find()){
				System.out.println("Positive: " + matcher.group(1));

				if(matcher.find()){
					System.out.println("Negative: " + matcher.group(1));
				}
			}
			

			System.out.println("Playtime: "+ game.playtime_forever);
		}
	}
	
	
	
}
//*[@id="ReviewsTab_positive"]/a/span[2]

//*[@id="ReviewsTab_negative"]/a/span[2]

// <span class="user_reviews_count">(2,232)</span>
