package steamParser;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
		PreparedStatement pst = null;
//		Statement st = null;
//		ResultSet rs = null;
		
		String url = "jdbc:postgresql://localhost/steamdb";
		String user = "casper";
		String password = "qwer";
		
		try {
			con = DriverManager.getConnection(url, user, password);
			
//			st = con.createStatement();
//			rs = st.executeQuery("SELECT * from games");
//			
//			if (rs.next()) {
//				System.out.println(rs.getString(1));
//			}
			String sql = "INSERT INTO games (appid, name, positive, negative) VALUES (?, ?, ?, ?)";
			pst = con.prepareStatement(sql);
			
			
			int i = 1;	
			for (Game game : page.response.games) {
				String html = SteamIdManager.readUrl(Constants.STEAM_APP_URL + game.appid);
				Document doc = Jsoup.parse(html);
				Elements nameEle = doc.getElementsByClass("apphub_AppName");
				
				// If name cannot be found, it means there is no app with that appid anymore, or the webpage requires age input.
				if (!(nameEle.size() > 0)) {
					// Auto fill age form with timeout of 10 seconds.
					doc = Jsoup.connect("http://store.steampowered.com/agecheck/app/" + game.appid)
				            .data("ageYear", "1990")
				            .data("ageMonth", "January")
				            .data("ageDay", "1")
				            .timeout(10*1000)
				            .post();
				}
								
				nameEle = doc.getElementsByClass("apphub_AppName");
				Element positiveEle = doc.getElementById("ReviewsTab_positive");
				Element negativeEle = doc.getElementById("ReviewsTab_negative");
				
				if (nameEle.size() > 0) {
					System.out.println(i++ + ")");
					System.out.println("appid: "+ game.appid);
					String name = nameEle.text();
					int positive;
					int negative;
					
					String positiveStr = positiveEle.getElementsByClass("user_reviews_count").text()
							.replace("(", "")
							.replace(")", "")
							.replace(",", "");
					
					String negativeStr = negativeEle.getElementsByClass("user_reviews_count").text()
							.replace("(", "")
							.replace(")", "")
							.replace(",", "");
					
					positive = Integer.parseInt(positiveStr);
					negative = Integer.parseInt(negativeStr);
					
					System.out.println(name);
					System.out.println(positive);
					System.out.println(negative);
					
					pst.setInt(1, Integer.parseInt(game.appid));
					pst.setString(2, name);
					pst.setInt(3, positive);
					pst.setInt(4, negative);
					
					pst.executeUpdate();
				
				} else {
					System.out.println("appid: "+ game.appid);
					System.out.println("No info found.");
				}
				
				System.out.println("---------------------------------------");

			}
			
			
			
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;		
		} finally {
//			if (rs != null) {
//                rs.close();
//            }
//            if (st != null) {
//                st.close();
//            }
			if (pst != null) {
				pst.close();
			}
            if (con != null) {
                con.close();
            }

		}
		

	}
	
}
