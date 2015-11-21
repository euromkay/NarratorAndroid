package voss.android;

import java.util.Comparator;

import voss.logic.support.Constants;
import voss.logic.support.Equals;
import voss.logic.support.RoleTemplate;
import voss.roles.Role;

public class Listing {
	
	private String name;
	private int color;
	
	public Listing(String name, int color){
		this.name = name;
		this.color = color;
	}

	public String getName() {

		return name;
	}

	public int getColor() {
		return color;
	}
	
	public boolean isRandom(){
		return !Role.isRole(name);
	}

	public String toIpForm(){
		return color + Constants.SEPERATOR + name;
	}

	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		Listing l = (Listing) o;
		
		if(color != l.color)
			return false;
		if(Equals.notEqual(name,  l.name))
			return false;
		
		return true;
	}





}
