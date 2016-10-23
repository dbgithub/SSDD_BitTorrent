package es.deusto.ingenieria.ssdd.data;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DBManagerTEST {

	DBManager dbm;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDBManager() throws SQLException {
		dbm = new DBManager("test/db/test.db");
		assertNotNull(dbm.getCon());
	}

	@Test
	public void testInitDB() {
		this.dbm.initDB();
	}

	@Test
	public void testInsertPeer() {
		dbm.insertPeer(1234, "192.168.1.", 8080);
		assertNotEquals(-1, dbm.retrievePeers());
	}

	@Test
	public void testInsertTorrent() {
		dbm.insertTorrent("Superinfohash568945112265");
		assertNotEquals(-1, dbm.retrieveTorrents());
	}

	@Test
	public void testInsertPeer_Torrent() {
		dbm.insertPeer_Torrent(1234, "Superinfohash568945112265", 1000, 2500, 35000, 0);
		assertNotEquals(-1, dbm.retrievePeerTorrent());
	}
	
	@Test
	public void testResetDB() {
		dbm.resetDB();
	}
	
	@Test
	public void testCloseDB() throws SQLException {
		assertTrue(dbm.getCon().isClosed());
	}

}
