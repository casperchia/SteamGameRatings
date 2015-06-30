package steamParser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
	
	private static final int NUM_OF_THREADS = 40;
	public static int i;
	private static Lock lock = new ReentrantLock();
	
	
	public static class MyRunnable implements Runnable {
		
		private java.sql.Connection con;
		private PreparedStatement pst;
		private int appid;

		MyRunnable(int appid,java.sql.Connection con, PreparedStatement pst) {
			this.con = con;
			this.pst = pst;
			this.appid = appid;
		}
		
		@Override
		public void run() {
			Document doc = null;
			int retryCount = 0;
			while (doc == null && retryCount < Constants.MAX_RETRIES) {
//				if (retryCount > 0) {
//					System.out.println("Reconnecting " + "(" + retryCount + ") " + "to appid: " + appid);
//				}
				try {
					doc = Jsoup.connect(Constants.STEAM_APP_URL + appid).timeout(5*1000).get();
				} catch (IOException e1) {
//					System.out.println("---------------------------------");
//					System.out.println("Error connecting to appid: " + appid);
//					e1.printStackTrace();
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
						
						retryCount = 0;
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
//								e.printStackTrace();
							}
							retryCount++;
						}
						
						if (retryCount == Constants.MAX_RETRIES){
							System.out.println("Reached max AGECHECK retries. Unable to connect to appid: " + appid);
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
						
						lock.lock();
//						System.out.println(i++ + ")");
//						System.out.println("appid: "+ appid);
//						System.out.println(name);
//						System.out.println("+ " + positive);
//						System.out.println("- " + negative);
//						System.out.println(rating + "%");
//						System.out.println("---------------------------------------");
						
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
						} finally {
							lock.unlock();
						}

						
					} else {
//						System.out.println("appid: "+ appid);
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
		
		Connection con = null;
		PreparedStatement pst = null;
		
		try {
			con = getConnection();
			con.setAutoCommit(false);
			
			String sql = "UPDATE games SET name=?, positive=?, negative=?, rating=? WHERE appid=?;"
					+ "INSERT INTO games (appid, name, positive, negative, rating)"
					+ "SELECT ?, ?, ?, ?, ?"
					+ "WHERE NOT EXISTS (SELECT 1 FROM games WHERE appid=?)";
			pst = con.prepareStatement(sql);
			
			i = 1;
			System.out.println("Starting threads...");
			
			List<Integer> appidList = getAppidList(steamid);
			long startTime = System.currentTimeMillis();
			for (int appid : appidList) {	
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

	
	/**
	 * Scrapes all steam pages for appid: 0-400k and insert/updates into database.
	 * @throws SQLException
	 */
	public static void loadAllGames() throws SQLException {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
		Connection con = null;
		PreparedStatement pst = null;
		
		try {
			con = getConnection();
			con.setAutoCommit(false);
			
			String sql = "UPDATE games SET name=?, positive=?, negative=?, rating=? WHERE appid=?;"
					+ "INSERT INTO games (appid, name, positive, negative, rating)"
					+ "SELECT ?, ?, ?, ?, ?"
					+ "WHERE NOT EXISTS (SELECT 1 FROM games WHERE appid=?)";
			pst = con.prepareStatement(sql);
			
			
			i = 1;
			System.out.println("Starting threads...");
			long startTime = System.currentTimeMillis();
			for (int appid = 0; appid < 400000; appid++) {
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
	
	
	/**
	 * Scrape and insert all games in appids list into db.
	 * @param appids
	 * @throws SQLException 
	 */
	public static void loadGamesFromList (List<Integer> appids) {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
		Connection con = null;
		PreparedStatement pst = null;
		
		try {
			con = getConnection();
			con.setAutoCommit(false);
			
			String sql = "UPDATE games SET name=?, positive=?, negative=?, rating=? WHERE appid=?;"
					+ "INSERT INTO games (appid, name, positive, negative, rating)"
					+ "SELECT ?, ?, ?, ?, ?"
					+ "WHERE NOT EXISTS (SELECT 1 FROM games WHERE appid=?)";
			pst = con.prepareStatement(sql);
			
			
			i = 1;
			System.out.println("Starting threads...");
			long startTime = System.currentTimeMillis();
			for (int appid : appids) {
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
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return;		
			
		} finally {
			if (pst != null) {
				try { pst.close(); } catch (SQLException e) {e.printStackTrace();}
			}
            if (con != null) {
				try { con.close(); } catch (SQLException e) {e.printStackTrace();}
            }
		}
	}
	
	/**
	 * Get list of appids from given steam ID.
	 * @param steamid
	 * @return List of appids. Returns empty list if there are no games. Returns null if user cannot be found.
	 * @throws Exception
	 */
	public static List<Integer> getAppidList(String steamid) {
		List<Integer> appids = new ArrayList<Integer>();
		String json = null;
		try {
			json = SteamIdManager.readUrl("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + Constants.STEAM_KEY + "&steamid=" + steamid + "&format=json");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (json == null) {
			return null;
		} else {
			Gson gson = new Gson();
			Constants.Page page = gson.fromJson(json, Constants.Page.class);
			if (page.response.games != null) {
				for (Game game : page.response.games) {
					appids.add(Integer.parseInt(game.appid));
				}
			} 
			return appids;
		}
	}
	
	/**
	 * Gets list of games in GameBeans for given steamid, and also updates db with any missing games the user has (and the db doesn't).
	 * @param steamid
	 * @return List<GameBean> list of games.
	 */
	public static List<GameBean> getGames(String steamid) {
		List<GameBean> games = new ArrayList<GameBean>();
		List<Integer> appidList = getAppidList(steamid);
		List<Integer> missingAppids = getAppidList(steamid);
		List<Integer> dbList = getDbList();

		missingAppids.removeAll(dbList);
		loadGamesFromList(missingAppids);
				
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			con = getConnection();
			pst = con.prepareStatement("SELECT * FROM games where appid=?");
			
			if (appidList != null) {
	//			int x = 1;
				for (int id : appidList) {
					pst.setInt(1, id);
					rs = pst.executeQuery();
					
					while (rs.next()) {
						GameBean game = new GameBean();
						game.setAppid(rs.getInt(1));
						game.setName(rs.getString(2));
						game.setPositive(rs.getInt(3));
						game.setNegative(rs.getInt(4));
						game.setRating(rs.getDouble(5));
						games.add(game);
						
	//					System.out.println(x++ + ")");
	//					System.out.println(game.getAppid());
	//					System.out.println(game.getName());
	//					System.out.println(game.getPositive() + "/" + game.getNegative());
	//					System.out.println(game.getRating() + "%");
	//					System.out.println("-------------------------------");
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
			
		} finally {
			if (pst != null) {
				try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
            if (con != null) {
				try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
		}
		
		return games;
	}
	
	/**
	 * Creates a new connection and returns it.
	 * @return new connection
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		
		Properties props = new Properties();
		
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to load db.properties file.");
			return null;
		}
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String url = props.getProperty("POSTGRES_DB_URL");
		String user = props.getProperty("POSTGRES_DB_USERNAME");
		String password = props.getProperty("POSTGRES_DB_PASSWORD");

		Connection con = null;
		con = DriverManager.getConnection(url, user, password);
		
		return con;		
	}
	
	/**
	 * Get list of appids of all games in DB.
	 * @return List of appids (Integer).
	 */
	public static List<Integer> getDbList() {
		List<Integer> appids = new ArrayList<Integer>();
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			con = GamesManager.getConnection();
			pst = con.prepareStatement("SELECT appid FROM games ORDER BY appid");
			rs = pst.executeQuery();

			while (rs.next()) {
				appids.add(rs.getInt(1));
			}
						
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try { pst.close(); } catch (SQLException e) {e.printStackTrace();}
			}
            if (con != null) {
				try { con.close(); } catch (SQLException e) {e.printStackTrace();}
            }
		}
		
		return appids;
	}
	
}












