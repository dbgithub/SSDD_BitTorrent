package es.deusto.ingenieria.ssdd.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import es.deusto.ingenieria.ssdd.classes.Peer;
import es.deusto.ingenieria.ssdd.classes.PeerTorrent;

/**
 * DBManager will handle and manage the operations done against the database for every tracker.
 * @author aitor & kevin
 *
 */
public class DBManager {

	protected Connection con;
	
	/**
	 * Initializes a SQLite database with the name given by the parameter. The connection is established.
	 * If the database did not exist, then, it is created. If it did exist previously, then, the connection is established.
	 * (*Note for developers: The main difference between executeUpdate and executeQuery is that the former one return an int
	 *  whereas the latter one returns a ResultSet to iterate though it.)
	 * @param DBname
	 */
	public DBManager(String DBname) {
		try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:" + DBname);
			con.setAutoCommit(false);
			System.out.println("[DBManager] SQLite database connection established to: '" + DBname+"'");			
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Unable to open a connection with the database ('"+DBname+"'):" + e.getMessage());
		}
	}
	
	/**
	 * @return the con
	 */
	protected Connection getCon() {
		return con;
	}

	/**
	 * Initializes the database with the basic scheme of the domain including: Peer, Torrent and Peer_Torrent tables.
	 * No data is saved at this moment in time.
	 */
	public void initDB() {
		try {
			Statement statement = con.createStatement();
			statement.setQueryTimeout(15);  // a timeout of 15 secs
			try {
				int resul1 = statement.executeUpdate("CREATE TABLE IF NOT EXISTS Peer ("+
						"'IDpeer' INTEGER(20) NOT NULL," +
						"'IP' TEXT,"+
						"'Port' INTEGER(7),"+
						"PRIMARY KEY (IDpeer)"+
						")");
				if (resul1 >= 0) {
					con.commit();
					System.out.println("[DBManager] table was created successfuly ('Peer')");
				} else {
					con.rollback();
					System.out.println("[DBManager] WARNING! A rollback was performed in CREATE 'peer'");
				}
			} catch (SQLException e) {
				System.err.println("ERROR/EXCEPTION. Unable to CREATE table ('Peer'):" + e.getMessage());
			}
			try {
				int resul2 = statement.executeUpdate("CREATE TABLE IF NOT EXISTS Torrent ("+
						"'InfoHash' TEXT(20) NOT NULL,"+
						"PRIMARY KEY (InfoHash))");		
				if (resul2 >= 0) {
					con.commit();
					System.out.println("[DBManager] table was created successfuly ('Torrent')");
				} else {
					con.rollback();
					System.out.println("[DBManager] WARNING! A rollback was performed in CREATE 'torrent'");
				}
			} catch (SQLException e) {
				System.err.println("ERROR/EXCEPTION. Unable to CREATE table ('Torrent'):" + e.getMessage());
			}
			try {
				int resul3 = statement.executeUpdate("CREATE TABLE IF NOT EXISTS Peer_Torrent ("+
						"'FK_IDpeer' INTEGER(20) NOT NULL,"+
						"'FK_InfoHash' TEXT(20) NOT NULL,"+
						"'Uploaded' LONG(8),"+
						"'Downloaded' LONG(8),"+
						"'Left' LONG(8),"+
						"PRIMARY KEY (FK_IDpeer, FK_InfoHash)," +
						"FOREIGN KEY (FK_IDpeer) REFERENCES peer(IDpeer),"+
						"FOREIGN KEY (FK_InfoHash) REFERENCES torrent(InfoHash))");
				if (resul3 >= 0) {
					con.commit();
					System.out.println("[DBManager] table was created successfuly ('Peer_Torrent')");
				} else {
					con.rollback();
					System.out.println("[DBManager] WARNING! A rollback was performed in CREATE 'peer_torrent'");
				}
			} catch (SQLException e) {
				System.err.println("ERROR/EXCEPTION. Unable to CREATE table ('Peer_torrent'):" + e.getMessage());
			}
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Unable to CREATE tables:" + e.getMessage());
		}
	}
	
	/**
	 * Resets all tuples contained within each and every table fo the database. The data is removed and the tables remain clean.
	 */
	public void resetDB() {
		try {
			Statement statement = con.createStatement();
			statement.setQueryTimeout(15);  // a timeout of 15 secs
			int resul1 = statement.executeUpdate("DROP TABLE IF EXISTS peer");
			int resul2 = statement.executeUpdate("DROP TABLE IF EXISTS torrent");
			int resul3 = statement.executeUpdate("DROP TABLE IF EXISTS peer_torrent");
			if (resul1 >= 0 && resul2 >= 0 && resul3 >=0) {
				con.commit();
				System.out.println("[DBManager] tables within the database reseted successfuly");				
			} else {
				con.rollback();
				System.out.println("[DBManager] WARNING! A rollback was performed in resetDB method");
			}
		} catch (SQLTimeoutException etimeout) {
			System.err.println("ERROR/EXCEPTION. Timeout exceeded when droping tables: " + etimeout.getMessage());
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Unable to DROP tables:" + e.getMessage());
		}
	}
	
	/**
	 * Takes down the connection established with the database.
	 */
	public void closeDB() {
		try {
			con.close();
			System.out.println("[DBManager] Database connection was closed");
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error closing database connection: " + e.getMessage());
		}
	}
	
	
	// OPERATIONS & METHODS:
	
	public void insertPeer(Integer IDpeer, String IP, Integer Port) {
		// TODO: validate parameters
		
//		String sqlString = "INSERT INTO peer ('IDpeer', 'IP', 'Port') VALUES (?,?,?)";
		String sqlString = "INSERT INTO peer ('IDpeer', 'IP', 'Port') SELECT ?,?,? WHERE NOT EXISTS "+
							"(SELECT * FROM peer WHERE IDpeer = ?)";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setInt(1, IDpeer);
			stmt.setString(2, IP);
			stmt.setInt(3, Port);
			stmt.setInt(4, IDpeer);
			int exists = stmt.executeUpdate(); // Check if the tuple exists or not. EXISTS = 0; NOT EXISTS = 1

			if (exists > 0) {
				con.commit();
				System.out.println("[DBManager] a new record was saved into the database ('Peer')");
			} else if (exists !=0){
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in insertPeer method");				
			}	
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error inserting data into 'Peer'!" + e.getStackTrace());
		}
	}
	
	public void insertTorrent(String InfoHash){
		// TODO: validate parameteres

//		String sqlString = "INSERT INTO torrent ('InfoHash') VALUES (?)";
		String sqlString = "INSERT INTO torrent ('InfoHash') SELECT ? WHERE NOT EXISTS "+
							"(SELECT * FROM torrent WHERE InfoHash = ?)";

		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setString(1, InfoHash);
			stmt.setString(2, InfoHash);
			int exists = stmt.executeUpdate(); // Check if the tuple exists or not. EXISTS = 0; NOT EXISTS = 1
			
			if (exists > 0) {
				con.commit();
				System.out.println("[DBManager] a new record was saved into the database ('Torrent')");
			} else if (exists != 0) {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in insertTorrent method");
			}	
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR/EXCEPTION. Error inserting data into 'Torrent'!" + e.getMessage());
		}
	}
	
	public void insertPeer_Torrent(Integer IDpeer, String InfoHash, Integer uploaded, Integer downloaded, Integer left) {
		// TODO: validate parameteres

		String sqlString = "INSERT INTO peer_torrent ('FK_IDpeer', 'FK_InfoHash', 'Uploaded', 'Downloaded', 'Left') VALUES (?,?,?,?,?)";

		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setInt(1, IDpeer);
			stmt.setString(2, InfoHash);
			stmt.setInt(3, uploaded);
			stmt.setInt(4, downloaded);
			stmt.setInt(5, left);

			if (stmt.executeUpdate() >= 0) {
				con.commit();
				System.out.println("[DBManager] a new record was saved into the database ('Peer_torrent')");
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in insertPeer_torrent method");
			}	
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error inserting data into 'Peer_torrent'!" + e.getMessage());
		}
	}
	
	/**
	 * Retrieves a list of peers with their corresponding peer-torrent information.
	 * Apart from retrieving the attributes of a peer, the result also contains the attributes related to the
	 * torrent files the peer is linked to.
	 * @return a list of peers with attributes concerning the peer plus information regarding peer-torrent attributes.
	 */
	public ArrayList<Peer> retrievePeers() {
		ArrayList<Peer> list = new ArrayList<Peer>();
		String sqlString = "SELECT * FROM peer INNER JOIN peer_torrent ON peer.IDpeer = peer_torrent.FK_IDpeer";
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			con.commit();
			System.out.println("[DBManager] all peers with their respective peer-torrent information:");
			while(rs.next()) {
				int peerindex = list.indexOf(new Peer(rs.getInt("IDpeer")));
				if (peerindex != -1) {// This statement will occur when the peer was already in the result list.
					Peer updatedPeer = list.get(peerindex); // This is the peer to whom its information will be updated.
					updatedPeer.getSwarmList().put(rs.getString("FK_InfoHash"), new PeerTorrent(rs.getString("FK_InfoHash"),rs.getInt("Uploaded"),rs.getInt("Downloaded"),rs.getInt("Left")));
					list.set(peerindex, updatedPeer);
					System.out.println("[Retrieving peers...] Peer updated -> "+rs.getInt("IDpeer") +" | "+  rs.getString("IP") + " | " + rs.getInt("Port"));
				} else {// This statement will occur when there is no such peer in the result list. Thus, we instantiate a new one.
					HashMap<String, PeerTorrent> temp = new HashMap<String, PeerTorrent>();
					temp.put(rs.getString("FK_InfoHash"), new PeerTorrent(rs.getString("FK_InfoHash"),rs.getInt("Uploaded"),rs.getInt("Downloaded"),rs.getInt("Left")));
					list.add(new Peer(rs.getInt("IDpeer"),rs.getString("IP"),rs.getInt("Port"),temp));
					System.out.println("[Retrieving peers...] New peer inserted -> "+rs.getInt("IDpeer") +" | "+  rs.getString("IP") + " | " + rs.getInt("Port"));
				}
			}		
			return list;
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error retrieving tuples in 'peer': " + e.getMessage());
			try {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in retrievePeers method");
			} catch (SQLException e1) {
				System.err.println("[DBManager] Error performing the rollback in retrievePeers method");
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Retrieves all torrents saved in the database. 
	 * @return a list of infoHashes (strings)
	 */
	public ArrayList<String> retrieveTorrents() {
		ArrayList<String> list = new ArrayList<String>();
		String sqlString = "SELECT * FROM torrent";
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			con.commit();
			System.out.println("[DBManager] all peers within 'torrent':");		
			while (rs.next()) {
				list.add(rs.getString("Infohash"));
				System.out.println("[Retrieving torrents...] torrent -> " + rs.getString("InfoHash"));
			} 
			return list;
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error retrieving tuples in 'torrent': " + e.getMessage());
			try {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in retrieveTorrents method");
			} catch (SQLException e1) {
				System.err.println("[DBManager] Error performing the rollback in retrieveTorrents method");
				e1.printStackTrace();
			}

		}
		return null;
	}
	
	/**
	 * Retrieves a map of peer-torrent with key equals to the peer's ID and value equals to a list of (collection)
	 * peer-torrent objects.
	 * @return a map of peer-torrents information.
	 */
	public HashMap<Integer, ArrayList<PeerTorrent>> retrievePeerTorrent() {
		HashMap<Integer, ArrayList<PeerTorrent>> map = new HashMap<Integer, ArrayList<PeerTorrent>>();
		String sqlString = "SELECT * FROM peer_torrent";
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			con.commit();
			System.out.println("[DBManager] all peeer-torrents within 'peer_torrent':");
			while(rs.next()) {
				int peerkey = rs.getInt("FK_IDpeer");
				ArrayList<PeerTorrent> temp;
				if (map.containsKey(peerkey)) {
					if (map.get(peerkey) != null || map.get(peerkey).size() != 0) {
						temp = map.get(peerkey);
						temp.add(new PeerTorrent(rs.getString("FK_InfoHash"),rs.getInt("Uploaded"),rs.getInt("Downloaded"),rs.getInt("Left")));
						System.out.println("[Retrieving peer-torrents...] Peer-torrent updated with a new insert -> "+rs.getString("FK_InfoHash") +" | "+ rs.getInt("Uploaded") +" | "+ rs.getInt("Downloaded") +" | "+ rs.getInt("Left"));
						map.put(peerkey, temp);
					} 
				} else {
					temp = new ArrayList<PeerTorrent>();
					temp.add(new PeerTorrent(rs.getString("FK_InfoHash"),rs.getInt("Uploaded"),rs.getInt("Downloaded"),rs.getInt("Left")));
					System.out.println("[Retrieving peer-torrents...] New Peer-torrent inserted -> "+rs.getString("FK_InfoHash") +" | "+ rs.getInt("Uploaded") +" | "+ rs.getInt("Downloaded") +" | "+ rs.getInt("Left"));
					map.put(peerkey, temp);
				}
			}	
			return map;
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error retrieving tuples in 'peer_torrent': " + e.getMessage());
			try {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in retrievePeer_torrents method");
			} catch (SQLException e1) {
				System.err.println("[DBManager] Error performing the rollback in retrievePeer_torrents method");
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Updates an already existent peer with the values given by parameters
	 * @param IDpeer
	 * @param IP
	 * @param Port
	 * @return 1 if the operation was successful, if not, it returns -1
	 */
	public int updatePeer(Integer IDpeer, String IP, Integer Port) {
		// TODO: Validate input parameters, check for NULL values
		String sqlString = "UPDATE peer SET IP=?, Port=? WHERE IDpeer ="+IDpeer;
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setString(1, IP);
			stmt.setInt(2, Port);
			
			if (stmt.executeUpdate() >= 0) {
				con.commit();
				System.out.println("[DBManager] peer with ID = "+IDpeer+" was updated in the database ('Peer')");
				return 1;
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in updatePeer method");
				return -1;
			}
		} catch (SQLException e) {
			System.err.println("ERROR/EXCEPTION. Error updating a 'Peer' (ID="+IDpeer+")!" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Updates an already existent torrent with the values given by parameters
	 * @param InfoHash
	 * @return 1 if the operation was successful, if not, it returns -1
	 */
	public int updateTorrent(String primarykey, String InfoHash) {
		// TODO: Validate input parameters, check for NULL values
		String sqlString = "UPDATE torrent SET InfoHash=? WHERE InfoHash ='"+primarykey+"'";

		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setString(1, InfoHash);

			if (stmt.executeUpdate() >= 0) {
				con.commit();
				System.out.println("[DBManager] torrent with InfoHash = "+primarykey+" was updated to '"+InfoHash+"' in the database ('Torrent')");
				return 1;
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in updateTorrent method");
				return -1;
			}
		} catch (SQLException e) {
			System.err.println("ERROR/EXCEPTION. Error updating a 'Torrent' (InfoHash="+primarykey+")!" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Updates an already existent peer-torrent with the values given by parameters
	 * @param IDpeer
	 * @param InfoHash
	 * @param uploaded
	 * @param downloaded
	 * @param left
	 * @return 1 if the operation was successful, if not, it returns -1
	 */
	public int updatePeerTorrent(Integer IDpeer, String InfoHash, long uploaded, long downloaded, long left){
		// TODO: Validate input parameters, check for NULL values
		String sqlString = "UPDATE peer_torrent SET Uploaded=?, Downloaded=?, Left=? " +
							"WHERE EXISTS (SELECT * FROM peer_torrent WHERE FK_IDpeer ="+IDpeer + " AND FK_InfoHash='"+InfoHash+"') AND FK_IDpeer ="+IDpeer + " AND FK_InfoHash='"+InfoHash+"'";

		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setLong(1, uploaded);
			stmt.setLong(2, downloaded);
			stmt.setLong(3, left);
			
			int exists = stmt.executeUpdate(); // Check if the tuple exists or not. EXISTS = 0; NOT EXISTS = 1

			if (exists > 0) {
				con.commit();
				System.out.println("[DBManager] peer-torrent with ID = "+IDpeer+" and InfoHash = "+InfoHash+" was updated in the database ('Peer_Torrent')");
				return 1;
			} else if (exists == 0) {
				return -2; // If we return -2 means that no peer_torrent was found, so it is necessary to insert the peer and the torrent (in case they do not exist already)
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in updatePeerTorrent method");
				return -1;				
			}
		} catch (SQLException e) {
			System.err.println("ERROR/EXCEPTION. Error updating a 'PeerTorrent' (ID="+IDpeer+", InfoHash="+InfoHash+")!" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Deletes a peer specified by a given parameter
	 * @param idpeer
	 * @return 1 if the operation was successful, if not, it returns -1
	 */
	public int deletePeer(Integer idpeer) {
		String sqlString = "DELETE FROM peer WHERE IDpeer = "+idpeer;
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			if (!stmt.execute()) {
				con.commit();
				if (stmt.getUpdateCount() == 1) {
					System.out.println("Peer with ID = "+idpeer+" deleted successfully!");
					return 1;
					}
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in deletePeer method");
				return -1;
			}
			return -1;
		} catch (SQLException e) {
			System.err.println("ERROR/EXCEPTION. Error deleting a 'Peer' (ID="+idpeer+")!" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Deletes a torrent specified by a given parameter
	 * @param infohash
	 * @return 1 if the operation was successful, if not, it returns -1
	 */
	public int deleteTorrent(String infohash) {
		String sqlString = "DELETE FROM torrent WHERE InfoHash = '"+infohash+"'";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			if (!stmt.execute()) {
				con.commit();
				if (stmt.getUpdateCount() == 1) {
					System.out.println("Torrent with InfoHash = "+infohash+" deleted successfully!");
					return 1;
					}
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in deleteTorrent method");
				return -1;
			}
			return -1;
		} catch (SQLException e) {
			System.err.println("ERROR/EXCEPTION. Error deleting a 'Torrent' (InfoHash="+infohash+")!" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Deletes a peer-torrent specified by a given parameter
	 * @param idpeer
	 * @param infohash
	 * @return 1 if the operation was successful, if not, it returns -1
	 */
	public int deletePeerTorrent(Integer idpeer, String infohash) {
		String sqlString = "DELETE FROM peer_torrent WHERE FK_IDpeer = "+idpeer+" AND FK_InfoHash = '" + infohash+"'";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			if (!stmt.execute()) {
				con.commit();
				if (stmt.getUpdateCount() == 1) {
					System.out.println("PeerTorrent with ID = "+idpeer+" and InfoHash = "+infohash+" deleted successfully!");
					return 1;
					}
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in deletePeerTorrent method");
				return -1;
			}
			return -1;
		} catch (SQLException e) {
			System.err.println("ERROR/EXCEPTION. Error deleting a 'PeerTorrent' (ID="+idpeer+", InfoHash="+infohash+")!" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
}
