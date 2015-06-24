package steamParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private static final int NUM_OF_THREADS = 32;
	public static int i;
	
	
	public static class MyRunnable implements Runnable {
		
		private Lock lock;
		private Game game;
		private java.sql.Connection con;
		private PreparedStatement pst;
		
		MyRunnable(Game game,java.sql.Connection con, PreparedStatement pst) {
			this.game = game;
			this.con = con;
			this.pst = pst;
			this.lock = new ReentrantLock();
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
//			String html = null;
//			try {
//				html = SteamIdManager.readUrl(Constants.STEAM_APP_URL + game.appid);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Document doc = Jsoup.parse(html);
			Document doc = null;
			try {
				doc = Jsoup.connect(Constants.STEAM_APP_URL + game.appid).timeout(20*1000).get();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Elements nameEle = doc.getElementsByClass("apphub_AppName");
			
			// If name cannot be found, it means there is no app with that appid anymore, or the webpage requires age input.
			if (!(nameEle.size() > 0)) {
				// Auto fill age form with timeout of 10 seconds.
				try {
					doc = Jsoup.connect("http://store.steampowered.com/agecheck/app/" + game.appid)
					        .data("ageYear", "1990")
					        .data("ageMonth", "January")
					        .data("ageDay", "1")
					        .timeout(20*1000)
					        .post();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
							
			nameEle = doc.getElementsByClass("apphub_AppName");
			Element positiveEle = doc.getElementById("ReviewsTab_positive");
			Element negativeEle = doc.getElementById("ReviewsTab_negative");
//			lock.lock();
			if (nameEle.size() > 0) {
				int appid = Integer.parseInt(game.appid);
				String name = nameEle.text();
				int positive;
				int negative;
				BigDecimal rating;
				
//				lock.lock();
				System.out.println(i++ + ")");
//				System.out.println(i + ")");
//				i++;
//				lock.unlock();

				System.out.println("appid: "+ appid);
				
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
				rating = BigDecimal.valueOf((positive * 100.0) / (positive + negative));
				
				System.out.println(name);
				System.out.println("+ " + positive);
				System.out.println("- " + negative);
				System.out.println(rating + "%");
				
				try {
					pst.setString(1, name);
					pst.setInt(2, positive);
					pst.setInt(3, negative);
					pst.setBigDecimal(4, rating);
					pst.setInt(5, appid);
					
					pst.setInt(6, appid);
					pst.setString(7, name);
					pst.setInt(8, positive);
					pst.setInt(9, negative);
					pst.setBigDecimal(10, rating);
					
					pst.setInt(11, appid);

					pst.executeUpdate();
					
					con.commit();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			} else {
				System.out.println("appid: "+ game.appid);
				System.out.println("No info found.");
			}
			
			System.out.println("---------------------------------------");
//			lock.unlock();
		}
		
	}
	
	/**
	 * Gets games list of user with steamid and insert/updates into database.
	 * @param steamid
	 * @throws Exception
	 */
	public static void loadGames(String steamid) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
		String json = SteamIdManager.readUrl("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + Constants.STEAM_KEY + "&steamid=" + steamid + "&format=json");
		Gson gson = new Gson();
		Constants.Page page = gson.fromJson(json, Constants.Page.class);
		
		
		java.sql.Connection con = null;
		PreparedStatement pst = null;
		
		String url = "jdbc:postgresql://localhost/steamdb";
		String user = "casper";
		String password = "qwer";
		
		try {
			con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(false);
			
			String sql = "UPDATE games SET name=?, positive=?, negative=?, rating=? WHERE appid=?;"
					+ "INSERT INTO games (appid, name, positive, negative, rating)"
					+ "SELECT ?, ?, ?, ?, ?"
					+ "WHERE NOT EXISTS (SELECT 1 FROM games WHERE appid=?)";
			pst = con.prepareStatement(sql);
			
			
//			int i = 1;	
			i = 1;
			long startTime = System.currentTimeMillis();
			for (Game game : page.response.games) {
				Runnable worker = new MyRunnable(game, con, pst);
				executor.execute(worker);

			}
			executor.shutdown();
			while (!executor.isTerminated()) {
				
			}
			System.out.println("Finished all threads");
			long finishTime = System.currentTimeMillis();
			System.out.println("That took: " + (finishTime - startTime) + " ms.");
			
		} catch (SQLException e) {
			con.rollback();
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;		
			
		} finally {

			if (pst != null) {
				pst.close();
			}
            if (con != null) {
                con.close();
            }

		}
		

	}
	
}
