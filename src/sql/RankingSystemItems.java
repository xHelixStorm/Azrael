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
import constructors.ItemEquip;
import constructors.Skills;
import constructors.WeaponAbbvs;
import constructors.WeaponStats;
import constructors.Weapons;
import core.Hashes;
import fileManagement.IniFileReader;
import util.STATIC;

public class RankingSystemItems {
	private static final Logger logger = LoggerFactory.getLogger(RankingSystemItems.class);
	
	private static String ip = IniFileReader.getSQLIP2();
	private static String username = IniFileReader.getSQLUsername2();
	private static String password = IniFileReader.getSQLPassword2();
	
	public static void SQLconnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver couldn't be loaded", e);
		}
	}
	
	//users
	public static int SQLRemoveEquippedWeapon(long _user_id, long _guild_id, int _slot) {
		logger.info("SQLRemoveEquippedWeapon launched. Passed params {}, {}, {}", _user_id, _guild_id, _slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET "+(_slot == 1 ? "weapon1" : (_slot == 2 ? "weapon2" : "weapon3"))+" = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLRemoveEquippedWeapon Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLRemoveEquippedSkill(long _user_id, long _guild_id) {
		logger.info("SQLRemoveEquippedSkill launched. Passed params {}, {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET skill = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLRemoveEquippedSkill Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLEquipWeapon(long _user_id, long _guild_id, int _item_id, int _slot) {
		logger.info("SQLEquipWeapon launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _item_id, _slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET "+(_slot == 1 ? "weapon1" : (_slot == 2 ? "weapon2" : "weapon3"))+" = ? WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _item_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLEquipWeapon Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUnequipWeapon(long _user_id, long _guild_id, int _slot) {
		logger.info("SQLUnequipWeapon launched. Passed params {}, {}, {}", _user_id, _guild_id, _slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET "+(_slot == 1 ? "weapon1" : (_slot == 2 ? "weapon2" : "weapon3"))+" = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUnequipWeapon Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUnequipWholeEquipment(long _user_id, long _guild_id) {
		logger.info("SQLUnequipWeapon launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET weapon1 = NULL, weapon2 = NULL, weapon3 = NULL, skill = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUnequipWholeEquipment Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLEquipSkill(long _user_id, long _guild_id, int _item_id) {
		logger.info("SQLEquipSkill launched. Passed params {}, {}, {}", _user_id, _guild_id, _item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET skill = ? WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, _item_id);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLEquipSkill Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//inventory
	public static int SQLgetNumberOfWeaponID(long _guild_id, int _weapon_id, int _theme_id) {
		logger.info("SQLgetNumberOfWeaponID launched. Params passed {}, {}, {}", _guild_id, _weapon_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	public static int SQLgetRandomWeaponIDByAbbv(String _abbv, int _stat_id, int _theme_id) {
		logger.info("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}", _abbv, _stat_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT weapon_id FROM weapon_shop_content WHERE fk_theme_id = ? AND weapon_abbv = ? AND weapon_stat = ? ORDER BY RAND() LIMIT 1");
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
		logger.info("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}, {}", _guild_id, _category, _stat_id, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT weapon_id FROM weapon_shop_content INNER JOIN weapon_category ON fk_category_id = category_id AND weapon_shop_content.fk_theme_id = weapon_category.fk_theme_id WHERE weapon_shop_content.fk_theme_id = ? AND name = ?  AND weapon_stat = ? ORDER BY RAND() LIMIT 1");
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
	public static ArrayList<String> SQLgetWeaponCategories(long _guild_id, int _theme_id, boolean _overrideSkill){
		if(Hashes.getWeaponCategories(_guild_id) == null) {
			logger.info("SQLgetWeaponCategories launched. Params passed {}, {}", _guild_id, _theme_id);
			ArrayList<String> categories = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT DISTINCT(name) FROM weapon_category WHERE fk_theme_id = ? "+(_overrideSkill == false ? "AND skill = 0" : ""));
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
				return null;
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
		if(Hashes.getWeaponAbbreviations(_guild_id) == null) {
			logger.info("SQLgetWeaponAbbvs launched. Params passed {}, {}", _guild_id, _theme_id);
			ArrayList<WeaponAbbvs> abbreviations = new ArrayList<WeaponAbbvs>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
		if(Hashes.getWeaponStats(_guild_id) == null) {
			logger.info("SQLgetWeaponStats launched. Params passed {}, {}", _guild_id, _theme_id);
			ArrayList<WeaponStats> stats = new ArrayList<WeaponStats>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
	public static ArrayList<Weapons> SQLgetWholeWeaponShop(long _guild_id, int _theme_id) {
		if(Hashes.getWeaponShopContent(_guild_id) == null) {
			logger.info("SQLgetWholeWeaponShop launched. Params passed {}", _guild_id);
			ArrayList<Weapons> weapons = new ArrayList<Weapons>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM all_weapons WHERE theme_id = ?");
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
						rs.getBoolean(11),
						rs.getString(12),
						rs.getString(13),
						rs.getBoolean(14),
						rs.getBoolean(15),
						rs.getBoolean(16),
						rs.getInt(17),
						rs.getInt(18),
						rs.getInt(19),
						rs.getString(20),
						rs.getInt(21),
						rs.getInt(22),
						rs.getInt(23),
						rs.getInt(24),
						rs.getInt(25),
						rs.getInt(26),
						rs.getInt(27),
						rs.getInt(28),
						rs.getInt(29),
						rs.getString(30),
						rs.getString(31),
						rs.getInt(32),
						rs.getInt(33),
						rs.getInt(34),
						rs.getInt(35),
						rs.getInt(36),
						rs.getInt(37),
						rs.getInt(38),
						rs.getInt(39),
						rs.getInt(40),
						rs.getString(41),
						rs.getString(42),
						rs.getInt(43),
						rs.getInt(44),
						rs.getInt(45),
						rs.getInt(46),
						rs.getInt(47),
						rs.getInt(48),
						rs.getInt(49),
						rs.getInt(50),
						rs.getInt(51),
						rs.getString(52),
						rs.getString(53),
						rs.getInt(54),
						rs.getInt(55),
						rs.getInt(56),
						rs.getInt(57),
						rs.getInt(58),
						rs.getInt(59),
						rs.getInt(60),
						rs.getInt(61),
						rs.getInt(62),
						rs.getString(63)
					);
					weapons.add(weapon);
				}
				Hashes.addWeaponShopContent(_guild_id, weapons);
				return weapons;
			} catch (SQLException e) {
				logger.error("SQLgetWholeWeaponShop Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getWeaponShopContent(_guild_id);
	}
	
	public static ArrayList<Skills> SQLgetSkills(long _guild_id, int _theme_id) {
		if(Hashes.getSkillShop(_guild_id) == null) {
			logger.info("SQLgetSkills launched. Params passed {}, {}", _guild_id, _theme_id);
			ArrayList<Skills> skills = new ArrayList<Skills>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT skill_id, description, full_description, price, thumbnail, enabled FROM skill_shop_content WHERE fk_theme_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setInt(1, _theme_id);
				rs = stmt.executeQuery();
				while(rs.next()) {
					skills.add(new Skills(
						rs.getInt(1),
						rs.getString(2),
						rs.getString(3),
						rs.getLong(4),
						rs.getString(5),
						rs.getBoolean(6)
					));
				}
				Hashes.addSkillShop(_guild_id, skills);
				return skills;
			} catch (SQLException e) {
				logger.error("SQLgetSkills Exception", e);
				return null;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getSkillShop(_guild_id);
	}
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long _user_id, long _guild_id, int _item_id, String _status, int _theme_id, boolean _weapon){
		logger.info("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _item_id, _status, _theme_id, _weapon);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = "";
			if(_weapon) 
				sql = ("SELECT number, expires FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_theme_id = weapon_shop_content.fk_theme_id WHERE fk_user_id = ? AND fk_weapon_id = ? AND fk_status = ? AND fk_guild_id = ? AND inventory.fk_theme_id = ? AND enabled = 1");
			
			else 
				sql = ("SELECT number, expires FROM inventory INNER JOIN skill_shop_content ON fk_skill_id = skill_id AND inventory.fk_theme_id = skill_shop_content.fk_theme_id WHERE fk_user_id = ? AND fk_skill_id = ? AND fk_status = ? AND fk_guild_id = ? AND inventory.fk_theme_id = ? AND enabled = 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setInt(2, _item_id);
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
	
	public static String SQLgetEquippedWeaponDescription(long _user_id, long _guild_id, int slot) {
		logger.info("SQLgetEquippedWeaponDescription launched. Params passed {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT u."+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+", w.description, s.stat FROM users u LEFT JOIN inventory i ON u.user_id = i.fk_user_id AND u.fk_guild_id = i.fk_guild_id AND u."+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+" = i.fk_weapon_id LEFT JOIN weapon_shop_content w ON w.weapon_id = i.fk_weapon_id LEFT JOIN weapon_stats s ON w.weapon_stat = s.stat_id AND w.fk_theme_id = s.fk_theme_id WHERE u.user_id = ? AND u.fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1) != 0 && rs.getString(2) != null && rs.getString(2).length() > 0) {
					return rs.getString(2)+" "+rs.getString(3);
				}
				else {
					return "expired";
				}
			}
			return "empty";
		} catch (SQLException e) {
			logger.error("SQLgetEquippedWeaponDescription Exception", e);
			return "empty";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
			try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static String SQLgetEquippedSkillDescription(long _user_id, long _guild_id) {
		logger.info("SQLgetEquippedSkillDescription launched. Params passed {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT u.skill, s.description FROM users u LEFT JOIN inventory i ON u.user_id = i.fk_user_id AND u.fk_guild_id = i.fk_guild_id AND u.skill = i.fk_skill_id LEFT JOIN skill_shop_content s ON s.skill_id = i.fk_skill_id WHERE u.user_id = ? AND u.fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1) != 0 && rs.getString(2) != null && rs.getString(2).length() > 0) {
					return rs.getString(2);
				}
				else {
					return "expired";
				}
			}
			return "empty";
		} catch (SQLException e) {
			logger.error("SQLgetEquippedSkillDescription Exception", e);
			return "empty";
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
			try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<ItemEquip> SQLfilterInventoryWeapons(long _user_id, long _guild_id, String _item) {
		logger.info("SQLfilterInventoryWeapons launched. Params passed {}, {}, {}", _user_id, _guild_id, _item);
		ArrayList<ItemEquip> items = new ArrayList<ItemEquip>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT w.weapon_id, w.description, s.stat, w.weapon_abbv FROM inventory i INNER JOIN weapon_shop_content w ON i.fk_weapon_id = w.weapon_id INNER JOIN weapon_stats s ON w.weapon_stat = s.stat_id WHERE i.fk_user_id = ? && i.fk_guild_id = ? && w.description = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, "%"+_item+"%");
			rs = stmt.executeQuery();
			while(rs.next()) {
				items.add(new ItemEquip(
					rs.getInt(1),
					rs.getString(2),
					rs.getString(3),
					rs.getString(4)
				));
			}
			return items;
		} catch (SQLException e) {
			logger.error("SQLfilterInventoryWeapons Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
			try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static ArrayList<ItemEquip> SQLfilterInventorySkills(long _user_id, long _guild_id, String _item) {
		logger.info("SQLfilterInventorySkills launched. Params passed {}, {}, {}", _user_id, _guild_id, _item);
		ArrayList<ItemEquip> items = new ArrayList<ItemEquip>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT i.fk_skill_id, s.description FROM inventory i INNER JOIN skill_shop_content s ON i.fk_skill_id = s.skill_id WHERE i.fk_user_id = ? && i.fk_guild_id = ? && s.description = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _user_id);
			stmt.setLong(2, _guild_id);
			stmt.setString(3, "%"+_item+"%");
			rs = stmt.executeQuery();
			while(rs.next()) {
				items.add(new ItemEquip(
					rs.getInt(1),
					rs.getString(2)
				));
			}
			return items;
		} catch (SQLException e) {
			logger.error("SQLfilterInventorySkills Exception", e);
			return null;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
			try { stmt.close(); } catch (Exception e) { /* ignored */ }
			try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//Transaction
	@SuppressWarnings("resource")
	public static int SQLUpdateCurrencyAndInsertTimedInventory(long _user_id, long _guild_id, long _currency, int _item_id, long _position, long _expires, int _number, int _theme_id, boolean _weapon){
		logger.info("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _position, _expires, _number, _theme_id, _weapon);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, _currency);
			stmt.setLong(2, _user_id);
			stmt.setLong(3, _guild_id);
			stmt.executeUpdate();
			
			String sql2 = "";
			if(_weapon)
				sql2 = ("INSERT INTO inventory (fk_user_id, fk_weapon_id, position, number, fk_status, expires, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires)");
			else
				sql2 = ("INSERT INTO inventory (fk_user_id, fk_skill_id, position, number, fk_status, expires, fk_guild_id, fk_theme_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires)");
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
		logger.info("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _weapon_id, _timestamp, _number, _theme_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
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
