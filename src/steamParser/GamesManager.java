package steamParser;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
		
		
		java.sql.Connection con = null;
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
		int i = 1;	
		for (Game game : page.response.games) {
			System.out.println("appid: "+ game.appid);
//			String html = SteamIdManager.readUrl(Constants.STEAM_APP_URL + game.appid);

			/*
			Document doc = Jsoup.parse(html);
//			Document doc = Jsoup.connect(Constants.STEAM_APP_URL + game.appid).get();
			Elements test = doc.getElementsByClass("apphub_AppName");
			System.out.println(test.toString());
			*/	
			
//			Connection.Response loginForm = Jsoup.connect(Constants.STEAM_APP_URL + "50130").method(Connection.Method.GET).execute();
			// Auto fill age form with timeout of 10 seconds.
			Document doc = Jsoup.connect("http://store.steampowered.com/agecheck/app/" + game.appid)
		            .data("ageYear", "1990")
		            .data("ageMonth", "January")
		            .data("ageDay", "1")
		            .timeout(10*1000)
		            .post();
//		           System.out.println(document);
			Elements name = doc.getElementsByClass("apphub_AppName");
			Element positive = doc.getElementById("ReviewsTab_positive");
			Element negative = doc.getElementById("ReviewsTab_negative");
			
			if (name.size() > 0) {
//				System.out.println("name = " + name.toString());
				System.out.println(i + ")");
				i++;
				System.out.println(name.text());
				System.out.println(positive.getElementsByClass("user_reviews_count").text());
				System.out.println(negative.getElementsByClass("user_reviews_count").text());
//				System.out.println(positive.toString());
//				System.out.println(negative.toString());				
			}
			
			System.out.println("---------------------------------------");

		}
	}
	
}
