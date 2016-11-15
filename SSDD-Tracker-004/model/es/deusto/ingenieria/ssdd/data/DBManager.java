package es.deusto.ingenieria.ssdd.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;

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
						"'Uploaded' INTEGER(8),"+
						"'Downloaded' INTEGER(8),"+
						"'Left' INTEGER(8),"+
						"'Key' INTEGER(4),"+
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
		
		String sqlString = "INSERT INTO peer ('IDpeer', 'IP', 'Port') VALUES (?,?,?)";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setInt(1, IDpeer);
			stmt.setString(2, IP);
			stmt.setInt(3, Port);
			
			if (stmt.executeUpdate() >=0) {
				con.commit();
				System.out.println("[DBManager] a new record was saved into the database ('Peer')");
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in insertPeer method");
			}	
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error inserting data into 'Peer'!" + e.getStackTrace());
		}
	}
	
	public void insertTorrent(String InfoHash){
		// TODO: validate parameteres

		String sqlString = "INSERT INTO torrent ('InfoHash') VALUES (?)";

		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setString(1, InfoHash);

			if (stmt.executeUpdate() >= 0) {
				con.commit();
				System.out.println("[DBManager] a new record was saved into the database ('Torrent')");
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in insertTorrent method");
			}	
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR/EXCEPTION. Error inserting data into 'Torrent'!" + e.getMessage());
		}
	}
	
	public void insertPeer_Torrent(Integer IDpeer, String InfoHash, Integer uploaded, Integer downloaded, Integer left, Integer key) {
		// TODO: validate parameteres

		String sqlString = "INSERT INTO peer_torrent ('FK_IDpeer', 'FK_InfoHash', 'Uploaded', 'Downloaded', 'Left', 'Key') VALUES (?,?,?,?,?,?)";

		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
			stmt.setInt(1, IDpeer);
			stmt.setString(2, InfoHash);
			stmt.setInt(3, uploaded);
			stmt.setInt(4, downloaded);
			stmt.setInt(5, left);
			stmt.setInt(6, key);

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
	
	public int retrievePeers() {
		// TODO: return a real and meaningful value
		
		String sqlString = "SELECT * FROM peer";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			
			if (rs != null) {
				con.commit();
				System.out.println("[DBManager] all peers within 'peer':");
				while(rs.next()) {
					System.out.println(rs.getInt("IDpeer") +" | "+  rs.getString("IP") + " | " + rs.getInt("Port"));
				}		
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in retrievePeers method");
			}
			
			return 0; // to delete in the future. We should return meaningful value.
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error retrieving tuples in 'peer': " + e.getMessage());
			return -1; // to delete in the future. We should return meaningful value.
		}
	}
	
	public int retrieveTorrents() {
		// TODO: return a real and meaningful value
		
		String sqlString = "SELECT * FROM torrent";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			
			if (rs != null) {
				con.commit();
				System.out.println("[DBManager] all peers within 'torrent':");
				while(rs.next()) {
					System.out.println(rs.getString("InfoHash"));
				}			
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in retrieveTorrents method");
			}
			
			return 0; // to delete in the future. We should return meaningful value.
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error retrieving tuples in 'torrent': " + e.getMessage());
			return -1; // to delete in the future. We should return meaningful value.
		}
	}
	
	public int retrievePeerTorrent() {
		// TODO: return a real and meaningful value
		
		String sqlString = "SELECT * FROM peer_torrent";
		
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			
			if (rs != null) {
				con.commit();
				System.out.println("[DBManager] all peers within 'peer_torrent':");
				while(rs.next()) {
					System.out.println(rs.getInt("downloaded") +" | "+  rs.getInt("uploaded") + " | " + rs.getInt("left") + " | " + rs.getInt("key"));
				}	
			} else {
				con.rollback();
				System.err.println("[DBManager] WARNING! A rollback was performed in retrievePeer_torrents method");
			}
			
			return 0; // to delete in the future. We should return meaningful value.
		} catch (Exception e) {
			System.err.println("ERROR/EXCEPTION. Error retrieving tuples in 'peer_torrent': " + e.getMessage());
			return -1; // to delete in the future. We should return meaningful value.
		}
	}
}
