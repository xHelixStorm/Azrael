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

import constructors.InventoryContent;
import constructors.WeaponAbbvs;
import constructors.WeaponStats;
import constructors.Weapons;
import core.Hashes;
import fileManagement.IniFileReader;

public class RankingSystemItems {
	private static final Logger logger = LoggerFactory.getLogger(RankingSystemItems.class);
	
	private static String username = IniFileReader.getSQLUsername2();
	private static String password = IniFileReader.getSQLPassword2();
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	//inventory
	public static int SQLgetNumberOfWeaponID(long _guild_id, int _weapon_id, int _theme_id) {
		logger.debug("SQLgetNumberOfWeaponID launched. Params passed {}, {}, {}", _guild_id, _weapon_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number FROM inventory WHERE fk_guild_id =  ? AND fk_weapon_id = ? AND fk_theme_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _guild_id);
			stmt.setInt(2, _weapon_id);
			stmt.setInt(3, _theme_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetNumberOfWeaponID Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//weapon_shop_content
	public static int SQLgetRandomWeaponIDByAbbv(long _guild_id, String _abbv, int _stat_id, int _theme_id) {
		logger.debug("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}, {}", _guild_id, _abbv, _stat_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT weapon_id FROM weapon_shop_content WHERE fk_theme_id = ? AND weapon_abbv LIKE ? AND weapon_stat = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _theme_id);
			stmt.setString(2, _abbv);
			stmt.setInt(3, _stat_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetRandomWeaponIDByAbbv Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetRandomWeaponIDByCategory(long _guild_id, String _category, int _stat_id, int _theme_id) {
		logger.debug("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}, {}", _guild_id, _category, _stat_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT weapon_id FROM weapon_shop_content INNER JOIN weapon_category ON fk_category_id = category_id AND weapon_shop_content.fk_theme_id = weapon_category.fk_theme_id WHERE weapon_shop_content.fk_theme_id = ? AND name LIKE ?  AND weapon_stat = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _theme_id);
			stmt.setString(2, _category);
			stmt.setInt(3, _stat_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetRandomWeaponIDByAbbv Exception", e);
			return 0;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//weapon_category
	public static ArrayList<String> SQLgetWeaponCategories(long _guild_id, int _theme_id){
		logger.debug("SQLgetWeaponCategories launched. Params passed {}, {}", _guild_id, _theme_id);
		ArrayList<String> categories = new ArrayList<String>();
		if(Hashes.getWeaponCategories(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT DISTINCT(name) FROM weapon_category WHERE fk_theme_id = ? AND skill = 0");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					categories.add(rs.getString(1));
				}
				Hashes.addWeaponCategories(_guild_id, categories);
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
		return Hashes.getWeaponCategories(_guild_id);
	}
	
	//weapon_abbreviation
	public static ArrayList<WeaponAbbvs> SQLgetWeaponAbbvs(long _guild_id, int _theme_id) {
		logger.debug("SQLgetWeaponAbbvs launched. Params passed {}, {}", _guild_id, _theme_id);
		ArrayList<WeaponAbbvs> abbreviations = new ArrayList<WeaponAbbvs>();
		if(Hashes.getWeaponAbbreviations(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT abbv, description FROM weapon_abbreviation WHERE fk_theme_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					WeaponAbbvs abbreviation = new WeaponAbbvs(rs.getString(1), rs.getString(2));
					abbreviations.add(abbreviation);
				}
				Hashes.addWeaponAbbreviation(_guild_id, abbreviations);
				return abbreviations;
			} catch (SQLException e) {
				logger.error("SQLgetWeponAbbvs Exception", e);
				return abbreviations;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getWeaponAbbreviations(_guild_id);
	}
	
	//weapon_stats
	public static ArrayList<WeaponStats> SQLgetWeaponStats(long _guild_id, int _theme_id) {
		logger.debug("SQLgetWeaponStats launched. Params passed {}, {}", _guild_id, _theme_id);
		ArrayList<WeaponStats> stats = new ArrayList<WeaponStats>();
		if(Hashes.getWeaponStats(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT stat_id, stat FROM weapon_stats WHERE fk_theme_id = ? AND weapon = 1");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					WeaponStats stat = new WeaponStats(rs.getInt(1), rs.getString(2));
					stats.add(stat);
				}
				Hashes.addWeaponStat(_guild_id, stats);
				return stats;
			} catch (SQLException e) {
				logger.error("SQLgetWeponStats Exception", e);
				return stats;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getWeaponStats(_guild_id);
	}
	
	//JOINS
	public static ArrayList<Weapons> SQLgetWholeWeaponShop(long _guild_id, int _theme_id){
		logger.debug("SQLgetWholeWeaponShop launched. Params passed {}", _guild_id);
		ArrayList<Weapons> weapons = new ArrayList<Weapons>();
		if(Hashes.getWeaponShopContent(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT s.weapon_id, s.description, s.price, s.weapon_abbv, s.fk_skin, s.weapon_stat, w.stat, s.fk_category_id, c.name, o.overlay, s.enabled FROM weapon_shop_content s INNER JOIN weapon_stats w ON s.weapon_stat = w.stat_id INNER JOIN weapon_category c ON s.fk_category_id = c.category_id && s.fk_theme_id = c.fk_theme_id INNER JOIN randomshop_reward_overlays o ON s.fk_overlay_id = o.overlay_id  && s.fk_theme_id = o.fk_theme_id WHERE s.fk_theme_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
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
						rs.getString(10),
						rs.getBoolean(11)
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
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long _user_id, long _guild_id, int _weapon_id, String _status, int _theme_id){
		logger.debug("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}, {}", _user_id, _guild_id, _weapon_id, _status, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT number, expires FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_theme_id = weapon_shop_content.fk_theme_id WHERE fk_user_id = ? AND fk_weapon_id = ? AND fk_status = ? AND fk_guild_id = ? AND inventory.fk_theme_id = ? AND enabled = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _weapon_id);
			stmt.setString(3, _status);
			stmt.setLong(4, _guild_id);
			stmt.setInt(5, _theme_id);
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
	public static int SQLUpdateCurrencyAndInsertTimedInventory(long _user_id, long _guild_id, long _currency, int _item_id, long _position, long _expires, int _number, int _theme_id){
		logger.debug("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _position, _expires, _number, _theme_id);
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
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_weapon_id, position, number, fk_status, expires, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
			stmt.setTimestamp(3, new Timestamp(_position));
			stmt.setInt(4, _number);
			stmt.setTimestamp(5, new Timestamp(_expires+604800000));
			stmt.setLong(6, _guild_id);
			stmt.setInt(7, _theme_id);
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
	
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndInsertWeaponRandomshop(long _user_id, long _guild_id, long _currency, int _weapon_id, Timestamp _timestamp, int _number, int _theme_id){
		logger.debug("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _weapon_id, _timestamp, _number, _theme_id);
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
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_weapon_id, position, number, fk_status, fk_guild_id, fk_theme_id) SELECT ?, weapon_id, ?, ?, 'perm', ?, ? FROM weapon_shop_content WHERE weapon_id = ? AND fk_theme_id = ? ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, _user_id);
			stmt.setTimestamp(2, _timestamp);
			stmt.setInt(3, _number);
			stmt.setLong(4, _guild_id);
			stmt.setInt(5, _theme_id);
			stmt.setInt(6, _weapon_id);
			stmt.setInt(7, _theme_id);
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
