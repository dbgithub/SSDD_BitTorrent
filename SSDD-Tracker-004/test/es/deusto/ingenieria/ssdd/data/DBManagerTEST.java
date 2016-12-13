package es.deusto.ingenieria.ssdd.data;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.PeerTorrent;

public class DBManagerTEST {

	static DBManager dbm;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dbm = new DBManager("test/db/test.db");
		dbm.initDB();
	}
	
	@Before
	public void setUp() throws Exception {
		// not used for the moment
	}
	
	@After
	public void tearDown() throws Exception {
		// not used for the moment
	}

	@Test
	public void testDBManager() throws SQLException {
		assertNotNull(dbm.getCon());
	}

	//@Test
	public void testInsertPeer() {
		dbm.insertPeer(1234, "192.168.1.1", 8080);
		assertNotEquals(-1, dbm.retrievePeers());
	}

	//@Test
	public void testInsertTorrent() {
		dbm.insertTorrent("Superinfohash568945112265");
		assertNotEquals(-1, dbm.retrieveTorrents());
	}

	//@Test
	public void testInsertPeer_Torrent() {
		dbm.insertPeer_Torrent(1234, "Superinfohash568945112265", 1000, 2500, 35000);
		assertNotEquals(-1, dbm.retrievePeerTorrent());
	}
	
	//@Test
	public void testretrievePeers() {
		ArrayList<Peer> resul = dbm.retrievePeers();
		assertNotNull(resul);
		assertTrue((resul.size() != 0));
	}
	
	//@Test
	public void testretrieveTorrents() {
		ArrayList<String> resul = dbm.retrieveTorrents();
		assertNotNull(resul);
		assertTrue((resul.size() != 0));
	}
	
	//@Test
	public void testretretrievePeerTorrent() {
		HashMap<Integer, ArrayList<PeerTorrent>> map = dbm.retrievePeerTorrent();
		assertNotNull(map);
		assertTrue((map.keySet().size()!=0));
	}
	
	//@Test
	public void testupdatePeer() {
		assertEquals(1, dbm.updatePeer(1234, "192.168.1.2", 9090));
	}
	
	//@Test
	public void testupdateTorrent() {
		assertEquals(1, dbm.updateTorrent("ilAsaEkgRzIfrqFBwayv", "ilAsaEkgRzIfrqFBwayv_2"));
	}
	
	//@Test
	public void testupdatePeerTorrent() {
		assertEquals(1, dbm.updatePeerTorrent(1235, "1571000710", 512, 1024, 6092));
	}
	
	//@Test
	public void testdeletePeer() {
		assertEquals(1, dbm.deletePeer(1235));
	}
	
	//@Test
	public void testdeleteTorrent() {
		assertEquals(1, dbm.deleteTorrent("vhvodQk kXuNVaHOuTXo"));
	}
	
	//@Test
	public void testdeletePeerTorrent() {
		assertEquals(1, dbm.deletePeerTorrent(1234, "1188903679"));
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws SQLException {
//		dbm.resetDB();
		dbm.closeDB();
		assertTrue(dbm.getCon().isClosed());
	}

}
