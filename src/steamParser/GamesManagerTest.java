package steamParser;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GamesManagerTest {

	@Test
	public void testLoadGames() throws Exception {
//		 mozface id
//		for (int i = 0; i < 20; i++){
//			GamesManager.loadGames("76561198024048520");
//		}
		// eunicell id
//		GamesManager.loadGames("76561198061965614");
		
		// valkyriebiscuit		
//		GamesManager.loadGames((SteamIdManager.getSteamId("ValkyrieBiscuit")));

//		GamesManager.loadAllGames();
		
//		GamesManager.getGames("76561198024048520");
	}
 
	@Test
	public void testLoadGamesFromList() throws Exception {
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			con = GamesManager.getConnection();
			con.setAutoCommit(false);
			pst = con.prepareStatement("DELETE FROM games where appid< 51");
			pst.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		} finally {
			if (pst != null) {
				pst.close();
			}
            if (con != null) {
                con.close();
            }
		}
		
		List<Integer> appids = new ArrayList<Integer>();
		appids.add(10);
		appids.add(20);
		appids.add(30);
		appids.add(40);
		appids.add(50);
		
		GamesManager.loadGamesFromList(appids);
		
		try {
			con = GamesManager.getConnection();
			pst = con.prepareStatement("SELECT * FROM games where appid< 51");
			rs = pst.executeQuery();
			
			for (int i = 10; i < 51; i = i + 10) {
				rs.next();
				assertEquals(rs.getInt(1), i);
			}
			
			System.out.println("loadGamesFromList() test successful!");
			
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		} finally {
			if (pst != null) {
				pst.close();
			}
            if (con != null) {
                con.close();
            }
		}
	}

	@Test
	public void testGetDbList() {
		List<Integer> appids = new ArrayList<Integer>();
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		int totalGames;
		int listSize;
		
		try {
			con = GamesManager.getConnection();
			pst = con.prepareStatement("SELECT count(*) FROM games");
			rs = pst.executeQuery();
			rs.next();
			totalGames = rs.getInt(1);
			listSize = GamesManager.getDbList().size();
			System.out.println("listSize: " + listSize);
			System.out.println("totalGames: " + totalGames);
			assertEquals(listSize, totalGames);
			System.out.println("getDbList() test successful!");

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
		
		
	}
}
