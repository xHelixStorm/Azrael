package rankingSystem;

import net.dv8tion.jda.core.entities.Member;

public class Rank {
	private long user_id;
	private int level;
	private long experience;
	private int rank;
	
	private int ranking_level;
	private int ranking_rank;
	private int ranking_profile;
	private int ranking_icon;
	private String description;
	private int price;
	
	private long role_id;
	private String role_name;
	private int level_requirement;
	
	private Member member;
	
	public void setUser_id(long _user_id){
		user_id = _user_id;
	}
	public void setLevel(int _level){
		level = _level;
	}
	public void setExperience(long _experience){
		experience = _experience;
	}
	public void setRank(int _rank){
		rank = _rank;
	}
	public void setRankingLevel(int _ranking_level){
		ranking_level = _ranking_level;
	}
	public void setRankingRank(int _ranking_rank){
		ranking_rank = _ranking_rank;
	}
	public void setRankingProfile(int _ranking_profile){
		ranking_profile = _ranking_profile;
	}
	public void setRankingIcon(int _ranking_icon){
		ranking_icon = _ranking_icon;
	}
	public void setDescription(String _description){
		description = _description;
	}
	public void setPrice(int _price){
		price = _price;
	}
	public void setRoleID(long _role_id){
		role_id = _role_id;
	}
	public void setRole_Name(String _role_name){
		role_name = _role_name;
	}
	public void setLevel_Requirement(int _level_requirement){
		level_requirement = _level_requirement;
	}
	public void setMember(Member _member){
		member = _member;
	}
	
	public Long getUser_id(){
		return user_id;
	}
	public Integer getLevel(){
		return level;
	}
	public Long getExperience(){
		return experience;
	}
	public Integer getRank(){
		return rank;
	}
	public int getRankingLevel(){
		return ranking_level;
	}
	public int getRankingRank(){
		return ranking_rank;
	}
	public int getRankingProfile(){
		return ranking_profile;
	}
	public int getRankingIcon(){
		return ranking_icon;
	}
	public String getDescription(){
		return description;
	}
	public int getPrice(){
		return price;
	}
	public long getRoleID(){
		return role_id;
	}
	public String getRole_Name(){
		return role_name;
	}
	public int getLevel_Requirement(){
		return level_requirement;
	}
	public Member getMember(){
		return member;
	}
}
