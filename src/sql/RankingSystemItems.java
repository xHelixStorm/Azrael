package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Hashes;
import fileManagement.IniFileReader;
import inventory.InventoryContent;
import rankingSystem.Weapons;

public class RankingSystemItems {
	private static final Logger logger = LoggerFactory.getLogger(RankingSystemItems.class);
	
	private static String username = IniFileReader.getSQLUsername3();
	private static String password = IniFileReader.getSQLPassword3();
	
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	//weapon_category
	public static ArrayList<String> SQLgetWeaponCategories(long _guild_id){
		logger.debug("SQLgetWeaponCategories launched. Params passed {}", _guild_id);
		ArrayList<String> categories = new ArrayList<String>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT DISTINCT(name) FROM weapon_category WHERE fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			rs = stmt.executeQuery();
			while(rs.next()){
				categories.add(rs.getString(1));
			}
			return categories;
		} catch (SQLException e) {
			logger.error("SQLgetWeaponCategories Exception", e);
			return categories;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//JOINS
	public static ArrayList<Weapons> SQLgetWholeWeaponShop(long _guild_id){
		logger.debug("SQLgetWholeWeaponShop launched. Params passed {}", _guild_id);
		ArrayList<Weapons> weapons = new ArrayList<Weapons>();
		if(Hashes.getWeaponShopContent(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT s.weapon_id, s.description, s.price, s.weapon_abbv, s.fk_skin, s.weapon_stat, w.stat, s.fk_category_id, c.name, s.enabled FROM weapon_shop_content s INNER JOIN weapon_stats w ON s.weapon_stat = w.stat_id INNER JOIN weapon_category c ON s.fk_category_id = c.category_id && s.fk_guild_id = c.fk_guild_id WHERE s.fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, _guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					Weapons weapon = new Weapons(
						rs.getInt(1),
						rs.getString(2),
						rs.getLong(3),
						rs.getString(4),
						rs.getInt(5),
						rs.getInt(6),
						rs.getString(7),
						rs.getInt(8),
						rs.getString(9),
						rs.getBoolean(10)
					);
					weapons.add(weapon);
				}
				Hashes.addWeaponShopContent(_guild_id, weapons);
				return weapons;
			} catch (SQLException e) {
				logger.error("SQLgetWholeWeaponShop Exception", e);
				return weapons;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getWeaponShopContent(_guild_id);
	}
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long _user_id, long _guild_id, int _weapon_id, String _status){
		logger.debug("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _weapon_id, _status);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number, expires FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_guild_id = weapon_shop_content.fk_guild_id WHERE fk_user_id = ? AND fk_weapon_id = ? AND fk_status = ? AND inventory.fk_guild_id = ? AND enabled = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _weapon_id);
			stmt.setString(3, _status);
			stmt.setLong(4, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				InventoryContent inventory = new InventoryContent();
				inventory.setNumber(rs.getInt(1));
				inventory.setExpiration(rs.getTimestamp(2));
				return inventory;
			}
			return null;
		} catch (SQLException e) {
			logger.error("SQLgetNumberExpirationFromInventory Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//Transaction
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndInsertTimedInventory(long _user_id, long _guild_id, long _currency, int _item_id, long _position, long _expires, int _number){
		logger.debug("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _position, _expires, _number);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_weapon_id, position, number, fk_status, expires, fk_guild_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, new Timestamp(_position));
			stmt.setInt(4, _number);
			stmt.setTimestamp(5, new Timestamp(_expires+604800000));
			stmt.setLong(6, _guild_id);
			var editedRows = stmt.executeUpdate();
			myConn.commit();	
			return editedRows;
		} catch (SQLException e) {
			logger.error("SQLUpdateCurrencyAndInsertTimedInventory Exception", e);
			try {
				myConn.rollback();
			} catch (SQLException e1) {
				logger.error("SQLUpdateCurrencyAndInsertTImedInventory rollback Exception", e);
			}
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
}