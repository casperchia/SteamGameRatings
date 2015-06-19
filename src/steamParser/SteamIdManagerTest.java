package steamParser;

import static org.junit.Assert.*;

import org.junit.Test;

public class SteamIdManagerTest {

	@Test
	public void testGetSteamId() throws Exception {
		String id = SteamIdManager.getSteamId("mozface");
		assertNotNull(id);
		assertEquals("76561198024048520", id);
		assertNull(SteamIdManager.getSteamId("mozface123123"));
	}

}
