package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
	public static int SQLRemoveEquippedWeapon(long user_id, long guild_id, int slot) {
		logger.trace("SQLRemoveEquippedWeapon launched. Passed params {}, {}, {}", user_id, guild_id, slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET "+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+" = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLRemoveEquippedWeapon Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLRemoveEquippedSkill(long user_id, long guild_id) {
		logger.trace("SQLRemoveEquippedSkill launched. Passed params {}, {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET skill = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLRemoveEquippedSkill Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLEquipWeapon(long user_id, long guild_id, int item_id, int slot) {
		logger.trace("SQLEquipWeapon launched. Passed params {}, {}, {}, {}", user_id, guild_id, item_id, slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET "+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+" = ? WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, item_id);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLEquipWeapon Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUnequipWeapon(long user_id, long guild_id, int slot) {
		logger.trace("SQLUnequipWeapon launched. Passed params {}, {}, {}", user_id, guild_id, slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET "+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+" = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUnequipWeapon Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLUnequipWholeEquipment(long user_id, long guild_id) {
		logger.trace("SQLUnequipWeapon launched. Passed params {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET weapon1 = NULL, weapon2 = NULL, weapon3 = NULL, skill = NULL WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLUnequipWholeEquipment Exception", e);
			return 0;
		} finally {
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLEquipSkill(long user_id, long guild_id, int item_id) {
		logger.trace("SQLEquipSkill launched. Passed params {}, {}, {}", user_id, guild_id, item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("UPDATE users SET skill = ? WHERE user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, item_id);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
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
	public static int SQLgetNumberOfWeaponID(long user_id, long guild_id, int weapon_id) {
		logger.trace("SQLgetNumberOfWeaponID launched. Params passed {}, {}, {}", user_id, guild_id, weapon_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT number FROM inventory WHERE fk_user_id = ? AND fk_guild_id =  ? AND fk_weapon_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setInt(3, weapon_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetNumberOfWeaponID Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	//weapon_shop_content
	public static int SQLgetRandomWeaponIDByAbbv(String abbv, int stat_id, long guild_id) {
		logger.trace("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}", abbv, stat_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT weapon_id FROM weapon_shop_content WHERE fk_guild_id = ? AND weapon_abbv = ? AND weapon_stat = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, abbv);
			stmt.setInt(3, stat_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetRandomWeaponIDByAbbv Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	public static int SQLgetRandomWeaponIDByCategory(long guild_id, String category, int stat_id) {
		logger.trace("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}", guild_id, category, stat_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT weapon_id FROM weapon_shop_content INNER JOIN weapon_category ON fk_category_id = category_id AND weapon_shop_content.fk_guild_id = weapon_category.fk_guild_id WHERE weapon_shop_content.fk_guild_id = ? AND name = ?  AND weapon_stat = ? ORDER BY RAND() LIMIT 1");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, guild_id);
			stmt.setString(2, category);
			stmt.setInt(3, stat_id);
			rs = stmt.executeQuery();
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			logger.error("SQLgetRandomWeaponIDByAbbv Exception", e);
			return -1;
		} finally {
			try { rs.close(); } catch (Exception e) { /* ignored */ }
		  try { stmt.close(); } catch (Exception e) { /* ignored */ }
		  try { myConn.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	
	
	//weapon_category
	public static ArrayList<String> SQLgetWeaponCategories(long guild_id, boolean overrideSkill) {
		final var weaponCategories = Hashes.getWeaponCategories(guild_id);
		if(weaponCategories == null) {
			logger.trace("SQLgetWeaponCategories launched. Params passed {}, {}", guild_id, overrideSkill);
			ArrayList<String> categories = new ArrayList<String>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT DISTINCT(name) FROM weapon_category WHERE fk_guild_id = ? "+(overrideSkill == false ? "AND skill = 0" : ""));
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					categories.add(rs.getString(1));
				}
				Hashes.addWeaponCategories(guild_id, categories);
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
		return weaponCategories;
	}
	
	//weapon_abbreviation
	public static ArrayList<WeaponAbbvs> SQLgetWeaponAbbvs(long guild_id) {
		final var weapons = Hashes.getWeaponAbbreviations(guild_id);
		if(weapons == null) {
			logger.trace("SQLgetWeaponAbbvs launched. Params passed {}", guild_id);
			ArrayList<WeaponAbbvs> abbreviations = new ArrayList<WeaponAbbvs>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT abbv, description FROM weapon_abbreviation WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					WeaponAbbvs abbreviation = new WeaponAbbvs(rs.getString(1), rs.getString(2));
					abbreviations.add(abbreviation);
				}
				Hashes.addWeaponAbbreviation(guild_id, abbreviations);
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
		return weapons;
	}
	
	//weapon_stats
	public static ArrayList<WeaponStats> SQLgetWeaponStats(long guild_id) {
		final var weaponStats = Hashes.getWeaponStats(guild_id);
		if(weaponStats == null) {
			logger.trace("SQLgetWeaponStats launched. Params passed {}", guild_id);
			ArrayList<WeaponStats> stats = new ArrayList<WeaponStats>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT stat_id, stat FROM weapon_stats WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
				rs = stmt.executeQuery();
				while(rs.next()){
					WeaponStats stat = new WeaponStats(rs.getInt(1), rs.getString(2));
					stats.add(stat);
				}
				Hashes.addWeaponStat(guild_id, stats);
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
		return weaponStats;
	}
	
	//JOINS
	public static ArrayList<Weapons> SQLgetWholeWeaponShop(long guild_id) {
		final var shop = Hashes.getWeaponShopContent(guild_id);
		if(shop == null) {
			logger.trace("SQLgetWholeWeaponShop launched. Params passed {}", guild_id);
			ArrayList<Weapons> weapons = new ArrayList<Weapons>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				String sql = ("SELECT * FROM all_weapons WHERE guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
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
						rs.getInt(9),
						rs.getString(10),
						rs.getInt(11),
						rs.getString(12),
						rs.getBoolean(13),
						rs.getString(14),
						rs.getString(15),
						rs.getBoolean(16),
						rs.getBoolean(17),
						rs.getBoolean(18),
						rs.getInt(19),
						rs.getInt(20),
						rs.getString(21),
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
						rs.getString(40),
						rs.getString(41),
						rs.getInt(42),
						rs.getInt(43),
						rs.getInt(44),
						rs.getInt(45),
						rs.getInt(46),
						rs.getInt(47),
						rs.getInt(48),
						rs.getInt(49),
						rs.getString(50),
						rs.getString(51),
						rs.getInt(52),
						rs.getInt(53),
						rs.getInt(54),
						rs.getInt(55),
						rs.getInt(56),
						rs.getInt(57),
						rs.getInt(58),
						rs.getInt(59),
						rs.getString(60),
						rs.getString(61),
						rs.getInt(62),
						rs.getInt(63),
						rs.getInt(64),
						rs.getInt(65),
						rs.getInt(66),
						rs.getInt(67),
						rs.getInt(68),
						rs.getInt(69),
						rs.getString(70),
						rs.getString(71),
						rs.getInt(72),
						rs.getInt(73),
						rs.getInt(74),
						rs.getInt(75),
						rs.getInt(76),
						rs.getInt(77),
						rs.getInt(78),
						rs.getInt(79),
						rs.getString(80),
						rs.getString(81),
						rs.getInt(82),
						rs.getInt(83),
						rs.getInt(84),
						rs.getInt(85),
						rs.getInt(86),
						rs.getInt(87),
						rs.getInt(88),
						rs.getInt(89),
						rs.getString(90),
						rs.getString(91),
						rs.getInt(92),
						rs.getInt(93),
						rs.getInt(94),
						rs.getInt(95),
						rs.getInt(96),
						rs.getInt(97),
						rs.getInt(98),
						rs.getInt(99),
						rs.getString(100),
						rs.getString(101),
						rs.getString(102),
						rs.getString(103)
					);
					weapons.add(weapon);
				}
				Hashes.addWeaponShopContent(guild_id, weapons);
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
		return shop;
	}
	
	public static ArrayList<Skills> SQLgetSkills(long guild_id) {
		final var shop = Hashes.getSkillShop(guild_id);
		if(shop == null) {
			logger.trace("SQLgetSkills launched. Params passed {}", guild_id);
			ArrayList<Skills> skills = new ArrayList<Skills>();
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
				//TODO: add missing ability field
				String sql = ("SELECT skill_id, description, full_description, price, thumbnail, enabled FROM skill_shop_content WHERE fk_guild_id = ?");
				stmt = myConn.prepareStatement(sql);
				stmt.setLong(1, guild_id);
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
				Hashes.addSkillShop(guild_id, skills);
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
		return shop;
	}
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long user_id, long guild_id, int item_id, String status, boolean weapon) {
		logger.trace("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}, {}", user_id, guild_id, item_id, status, weapon);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = "";
			if(weapon) 
				sql = ("SELECT number, expires FROM inventory INNER JOIN weapon_shop_content ON fk_weapon_id = weapon_id AND inventory.fk_guild_id = weapon_shop_content.fk_guild_id WHERE fk_user_id = ? AND fk_weapon_id = ? AND fk_status = ? AND weapon_shop_content.fk_guild_id = ?");
			
			else 
				sql = ("SELECT number, expires FROM inventory INNER JOIN skill_shop_content ON fk_skill_id = skill_id AND inventory.fk_guild_id = skill_shop_content.fk_guild_id WHERE fk_user_id = ? AND fk_skill_id = ? AND fk_status = ? AND skill_shop_content.fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setString(3, status);
			stmt.setLong(4, guild_id);
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
	
	public static String SQLgetEquippedWeaponDescription(long user_id, long guild_id, int slot) {
		logger.trace("SQLgetEquippedWeaponDescription launched. Params passed {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT u."+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+", w.description, s.stat FROM users u LEFT JOIN inventory i ON u.user_id = i.fk_user_id AND u.fk_guild_id = i.fk_guild_id AND u."+(slot == 1 ? "weapon1" : (slot == 2 ? "weapon2" : "weapon3"))+" = i.fk_weapon_id LEFT JOIN weapon_shop_content w ON w.weapon_id = i.fk_weapon_id LEFT JOIN weapon_stats s ON w.weapon_stat = s.stat_id AND w.fk_guild_id = s.fk_guild_id WHERE u.user_id = ? AND u.fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static String SQLgetEquippedSkillDescription(long user_id, long guild_id) {
		logger.trace("SQLgetEquippedSkillDescription launched. Params passed {}, {}", user_id, guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT u.skill, s.description FROM users u LEFT JOIN inventory i ON u.user_id = i.fk_user_id AND u.fk_guild_id = i.fk_guild_id AND u.skill = i.fk_skill_id LEFT JOIN skill_shop_content s ON s.skill_id = i.fk_skill_id AND s.fk_guild_id = i.fk_guild_id WHERE u.user_id = ? AND u.fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
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
	
	public static ArrayList<ItemEquip> SQLfilterInventoryWeapons(long user_id, long guild_id, String item) {
		logger.trace("SQLfilterInventoryWeapons launched. Params passed {}, {}, {}", user_id, guild_id, item);
		ArrayList<ItemEquip> items = new ArrayList<ItemEquip>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT w.weapon_id, w.description, s.stat, w.weapon_abbv FROM inventory i INNER JOIN weapon_shop_content w ON i.fk_weapon_id = w.weapon_id LEFT JOIN weapon_stats s ON w.weapon_stat = s.stat_id AND w.fk_guild_id = s.fk_guild_id WHERE i.fk_user_id = ? && i.fk_guild_id = ? && w.description = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, "%"+item+"%");
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
	
	public static ArrayList<ItemEquip> SQLfilterInventorySkills(long user_id, long guild_id, String item) {
		logger.trace("SQLfilterInventorySkills launched. Params passed {}, {}, {}", user_id, guild_id, item);
		ArrayList<ItemEquip> items = new ArrayList<ItemEquip>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			String sql = ("SELECT i.fk_skill_id, s.description FROM inventory i INNER JOIN skill_shop_content s ON i.fk_skill_id = s.skill_id AND i.fk_guild_id = s.fk_guild_id WHERE i.fk_user_id = ? && i.fk_guild_id = ? && s.description = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, user_id);
			stmt.setLong(2, guild_id);
			stmt.setString(3, "%"+item+"%");
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
	public static int SQLUpdateCurrencyAndInsertTimedInventory(long user_id, long guild_id, long currency, int item_id, long position, long expires, int number, boolean weapon, long extend) {
		logger.trace("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}, {}", user_id, guild_id, currency, item_id, position, expires, number, weapon, extend);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, currency);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			stmt.executeUpdate();
			
			String sql2 = "";
			if(weapon)
				sql2 = ("INSERT INTO inventory (fk_user_id, fk_weapon_id, position, number, fk_status, expires, fk_guild_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires)");
			else
				sql2 = ("INSERT INTO inventory (fk_user_id, fk_skill_id, position, number, fk_status, expires, fk_guild_id) VALUES(?, ?, ?, ?, \"limit\", ?, ?) ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number), expires=VALUES(expires)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, user_id);
			stmt.setInt(2, item_id);
			stmt.setTimestamp(3, new Timestamp(position));
			stmt.setInt(4, number);
			stmt.setTimestamp(5, new Timestamp(expires+(extend == 0 ? TimeUnit.DAYS.toMillis(7) : extend)));
			stmt.setLong(6, guild_id);
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
	public static int SQLUpdateCurrencyAndInsertWeaponRandomshop(long user_id, long guild_id, long currency, int weapon_id, Timestamp timestamp, int number) {
		logger.trace("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}", user_id, guild_id, currency, weapon_id, timestamp, number);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection(STATIC.getDatabaseURL("RankingSystem", ip), username, password);
			myConn.setAutoCommit(false);
			String sql = ("UPDATE user_details SET currency = ? WHERE fk_user_id = ? AND fk_guild_id = ?");
			stmt = myConn.prepareStatement(sql);
			stmt.setLong(1, currency);
			stmt.setLong(2, user_id);
			stmt.setLong(3, guild_id);
			stmt.executeUpdate();
			
			String sql2 = ("INSERT INTO inventory (fk_user_id, fk_weapon_id, position, number, fk_status, fk_guild_id) SELECT ?, weapon_id, ?, ?, 'perm', fk_guild_id FROM weapon_shop_content WHERE weapon_id = ? AND fk_guild_id = ? ON DUPLICATE KEY UPDATE position=VALUES(position), number=VALUES(number)");
			stmt = myConn.prepareStatement(sql2);
			stmt.setLong(1, user_id);
			stmt.setTimestamp(2, timestamp);
			stmt.setInt(3, number);
			stmt.setInt(4, weapon_id);
			stmt.setLong(5, guild_id);
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
