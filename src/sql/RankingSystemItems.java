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
	
	//users
	public static int SQLRemoveEquippedWeapon(long _user_id, long _guild_id, int _slot) {
		logger.debug("SQLRemoveEquippedWeapon launched. Passed params {}, {}, {}", _user_id, _guild_id, _slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLRemoveEquippedSkill launched. Passed params {}, {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLEquipWeapon launched. Passed params {}, {}, {}, {}", _user_id, _guild_id, _item_id, _slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUnequipWeapon launched. Passed params {}, {}, {}", _user_id, _guild_id, _slot);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLUnequipWeapon launched. Passed params {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLEquipSkill launched. Passed params {}, {}, {}", _user_id, _guild_id, _item_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
	public static int SQLgetRandomWeaponIDByAbbv(String _abbv, int _stat_id, int _theme_id) {
		logger.debug("SQLgetRandomWeaponIDByAbbv launched. Params passed {}, {}, {}", _abbv, _stat_id, _theme_id);
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
	public static ArrayList<String> SQLgetWeaponCategories(long _guild_id, int _theme_id, boolean _overrideSkill){
		logger.debug("SQLgetWeaponCategories launched. Params passed {}, {}", _guild_id, _theme_id);
		ArrayList<String> categories = new ArrayList<String>();
		if(Hashes.getWeaponCategories(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
	public static ArrayList<Weapons> SQLgetWholeWeaponShop(long _guild_id, int _theme_id) {
		logger.debug("SQLgetWholeWeaponShop launched. Params passed {}", _guild_id);
		ArrayList<Weapons> weapons = new ArrayList<Weapons>();
		if(Hashes.getWeaponShopContent(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
				String sql = ("SELECT s.weapon_id, s.description, s.price, s.weapon_abbv, s.fk_skin, s.weapon_stat, w.stat, s.fk_category_id, c.name, r.overlay, s.enabled, full_description, thumbnail, c.skill, c.close_range, c.long_range, magazine, ammunition, k.fk_attack_type, k.damage_plus, k.damage_plus_percent, k.damage_drop, k.damage_drop_percent, k.hit_chance_close, k.hit_chance_medium, k.hit_chance_distant, k.sp_consumption, k.fk_effect, m.fk_attack_type, m.damage_plus, m.damage_plus_percent, m.damage_drop, m.damage_drop_percent, m.hit_chance_close, m.hit_chance_medium, m.hit_chance_distant, m.sp_consumption, m.fk_effect, n.fk_attack_type, n.damage_plus, n.damage_plus_percent, n.damage_drop, n.damage_drop_percent, n.hit_chance_close, n.hit_chance_medium, n.hit_chance_distant, n.sp_consumption, n.fk_effect, o.fk_attack_type, o.damage_plus, o.damage_plus_percent, o.damage_drop, o.damage_drop_percent, o.hit_chance_close, o.hit_chance_medium, o.hit_chance_distant, o.sp_consumption, o.fk_effect FROM weapon_shop_content s INNER JOIN weapon_stats w ON s.weapon_stat = w.stat_id INNER JOIN weapon_category c ON s.fk_category_id = c.category_id && s.fk_theme_id = c.fk_theme_id INNER JOIN randomshop_reward_overlays r ON s.fk_overlay_id = r.overlay_id  && s.fk_theme_id = r.fk_theme_id INNER JOIN weapon_abbreviation a ON s.weapon_abbv = a.abbv && s.fk_theme_id = a.fk_theme_id LEFT JOIN weapon_attack k ON a.attack1 = k.attack_id LEFT JOIN weapon_attack m ON a.attack2 = m.attack_id LEFT JOIN weapon_attack n ON a.attack3 = n.attack_id LEFT JOIN weapon_attack o ON a.attack4 = o.attack_id WHERE s.fk_theme_id = ?");
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
						rs.getInt(18)
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
	
	public static ArrayList<Skills> SQLgetSkills(long _guild_id, int _theme_id) {
		logger.debug("SQLgetSkills launched. Params passed {}, {}", _guild_id, _theme_id);
		ArrayList<Skills> skills = new ArrayList<Skills>();
		if(Hashes.getSkillShop(_guild_id) == null) {
			Connection myConn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
				return skills;
			} finally {
				try { rs.close(); } catch (Exception e) { /* ignored */ }
			  try { stmt.close(); } catch (Exception e) { /* ignored */ }
			  try { myConn.close(); } catch (Exception e) { /* ignored */ }
			}
		}
		return Hashes.getSkillShop(_guild_id);
	}
	
	public static InventoryContent SQLgetNumberAndExpirationFromInventory(long _user_id, long _guild_id, int _item_id, String _status, int _theme_id, boolean _weapon){
		logger.debug("SQLgetNumberAndExpirationFromInventory launched. Passed params {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _item_id, _status, _theme_id, _weapon);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetEquippedWeaponDescription launched. Params passed {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLgetEquippedSkillDescription launched. Params passed {}, {}", _user_id, _guild_id);
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
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
		logger.debug("SQLfilterInventoryWeapons launched. Params passed {}, {}, {}", _user_id, _guild_id, _item);
		ArrayList<ItemEquip> items = new ArrayList<ItemEquip>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT w.weapon_id, w.description, s.stat, w.weapon_abbv FROM inventory i INNER JOIN weapon_shop_content w ON i.fk_weapon_id = w.weapon_id INNER JOIN weapon_stats s ON w.weapon_stat = s.stat_id WHERE i.fk_user_id = ? && i.fk_guild_id = ? && w.description LIKE ?");
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
		logger.debug("SQLfilterInventorySkills launched. Params passed {}, {}, {}", _user_id, _guild_id, _item);
		ArrayList<ItemEquip> items = new ArrayList<ItemEquip>();
		Connection myConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/RankingSystem?autoReconnect=true&useSSL=false", username, password);
			String sql = ("SELECT i.fk_skill_id, s.description FROM inventory i INNER JOIN skill_shop_content s ON i.fk_skill_id = s.skill_id WHERE i.fk_user_id = ? && i.fk_guild_id = ? && s.description LIKE ?");
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
		logger.debug("SQLUpdateCurrencyAndInsertTimedInventory launched. Passed params {}, {}, {}, {}, {}, {}, {}, {}, {}", _user_id, _guild_id, _currency, _item_id, _position, _expires, _number, _theme_id, _weapon);
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
