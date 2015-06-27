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
		
//		private Lock lock;
		private java.sql.Connection con;
		private PreparedStatement pst;
		private int appid;

		MyRunnable(int appid,java.sql.Connection con, PreparedStatement pst) {
			this.con = con;
			this.pst = pst;
//			this.lock = new ReentrantLock();
			this.appid = appid;
		}
		
		@Override
		public void run() {
			Document doc = null;
			int retryCount = 0;
			while (doc == null && retryCount < Constants.MAX_RETRIES) {
				if (retryCount > 0) {
					System.out.println("Reconnecting " + "(" + retryCount + ") " + "to appid: " + appid);
				}
				try {
					doc = Jsoup.connect(Constants.STEAM_APP_URL + appid).timeout(5*1000).get();
				} catch (IOException e1) {
					System.out.println("---------------------------------");
					System.out.println("Error connecting to appid: " + appid);
					e1.printStackTrace();
				}
				retryCount++;
			}
			
//			if (retryCount != 1){
//				System.out.println("RETRY COUNT = " + retryCount);				
//			}
			if (retryCount == Constants.MAX_RETRIES){
				System.out.println("Reached max retries. Unable to connect to appid: " + appid);
			}
			
			Elements nameEle = new Elements();
			if (doc != null) {
				
				nameEle = doc.getElementsByClass("apphub_AppName");
				
				// If name cannot be found, it means there is no app with that appid anymore, or the webpage requires age input.
				if (!(nameEle.size() > 0)) {
					
					if(doc.getElementsByClass("agecheck").size() > 0){
						doc = null;
						
						// Auto fill age form with timeout of 5 seconds.
						while (doc == null && retryCount < Constants.MAX_RETRIES) {
							try {
								doc = Jsoup.connect("http://store.steampowered.com/agecheck/app/" + appid)
								        .data("ageYear", "1990")
								        .data("ageMonth", "January")
								        .data("ageDay", "1")
								        .timeout(5*1000)
								        .post();
							} catch (IOException e) {
								System.out.println("Error connecting to AGECHECK of appid: " + appid);
								e.printStackTrace();
							}
							retryCount++;
						}
					}
				}
				
				if (doc != null) {
					nameEle = doc.getElementsByClass("apphub_AppName");
					
					if (nameEle.size() > 0) {
						String name = nameEle.text();
						int positive;
						int negative;
						BigDecimal rating;
						
						System.out.println(i++ + ")");
						System.out.println("appid: "+ appid);
						
						Element positiveEle = doc.getElementById("ReviewsTab_positive");
						Element negativeEle = doc.getElementById("ReviewsTab_negative");
						
						if (positiveEle == null) {
							positive = 0;
						} else {
							String positiveStr = positiveEle.getElementsByClass("user_reviews_count").text()
									.replace("(", "")
									.replace(")", "")
									.replace(",", "");
							
							positive = Integer.parseInt(positiveStr);

						}
						
						if (negativeEle == null) {
							negative = 0;
						} else {
							String negativeStr = negativeEle.getElementsByClass("user_reviews_count").text()
									.replace("(", "")
									.replace(")", "")
									.replace(",", "");
							
							negative = Integer.parseInt(negativeStr);
						}

						if (positive == 0 && negative == 0) {
							rating = BigDecimal.valueOf(0);
						} else {
							rating = BigDecimal.valueOf((positive * 100.0) / (positive + negative));
						}
						System.out.println(name);
//						System.out.println("+ " + positive);
//						System.out.println("- " + negative);
//						System.out.println(rating + "%");
						
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
							System.out.println("Error with appid: " + appid);
							e.printStackTrace();
							try {
								con.rollback();
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}

						
					} else {
//						System.out.println("appid: "+ game.appid);
//						System.out.println("No info found.");
					}
				}
				
			} else {
				System.out.println("appid: " + appid + " returns NULL");
			}
			
//			System.out.println("---------------------------------------");
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
			System.out.println("Starting threads...");
			long startTime = System.currentTimeMillis();
			for (Game game : page.response.games) {
				Runnable worker = new MyRunnable(Integer.parseInt(game.appid), con, pst);
				executor.execute(worker);

			}
			executor.shutdown();
			while (!executor.isTerminated()) {
				
			}
			long finishTime = System.currentTimeMillis();
			System.out.println("Finished all threads");
			System.out.println("That took: " + (finishTime - startTime) + " ms.");
			System.out.println("-----------------------");
			
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			con.rollback();
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

	public static void loadAllGames() throws SQLException {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
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
			
			
			i = 1;
			System.out.println("Starting threads...");
			long startTime = System.currentTimeMillis();
			for (int appid = 0; appid < 350000; appid++) {
//			for(int x = 0; x < 1000; x++){
//				int appid = 7796;
				Runnable worker = new MyRunnable(appid, con, pst);
				executor.execute(worker);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
				
			}
			long finishTime = System.currentTimeMillis();
			System.out.println("Finished all threads");
			System.out.println("That took: " + (finishTime - startTime) + " ms.");
			System.out.println("-----------------------");
			
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			con.rollback();
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
