package voss.shared.logic;


import voss.shared.logic.support.Equals;
import voss.shared.logic.support.RolePackage;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.packaging.Packager;
import voss.shared.roles.Role;

public class Member extends RoleTemplate{
	private String name;
	private int color;
	private int weight;
	
	public Member(String name, int team){
		this.name = name;
		this.color = team;
	}
	
	public Member(String name, int team, int weight){
		this(name, team);
		this.weight = weight;
	}
	
	public String getName(){
		return name;
	}
	public int getColor(){
		return color;
	}
	public int getWeight(){
		return weight;
	}
	
	public RolePackage getRole(Player p){
		Role r = Role.CREATOR(name, p);
		return new RolePackage(r, color);
		//TODO
	}
	
	public String toString(){
		return name + Integer.toString(color);
	}
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		Member m = (Member) o;
		if(notEqual(name, m.name))
			return false;
		if(color != m.color)
			return false;
		if(weight != m.weight)
			return false;
		
		return true;
	}
	private boolean notEqual(Object o, Object p){
		return Equals.notEqual(o, p);
	}
	
	public int hashCode(){
		int hash = 5;
		hash = 41 * hash + this.name.hashCode();
		hash = 41 * hash + this.color;
		hash = 41 * hash + this.weight;
		
		return hash;
	}
	
	
	public Member(Packager in){
		name = in.readString();
		color = in.readInt();
		weight = in.readInt();
	}

	public void writeToPackage(Packager dest) {
		dest.write(name);
		dest.write(color);
		dest.write(weight);
		
	}
	
}
