package steamParser;

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
		
		GamesManager.getGames("76561198024048520");
	}
 
}
