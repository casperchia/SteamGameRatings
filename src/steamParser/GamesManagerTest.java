package steamParser;

import org.junit.Test;

public class GamesManagerTest {

	@Test
	public void testPrintGames() throws Exception {
		// mozface id
//		GamesManager.printGames("76561198024048520");
		
		// eunicell id
		GamesManager.printGames("76561198061965614");
		
		// valkyriebiscuit		
//		GamesManager.printGames((SteamIdManager.getSteamId("ValkyrieBiscuit")));
	}
 
}
