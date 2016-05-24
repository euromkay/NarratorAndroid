package voss.shared.logic.support;

import voss.shared.roles.Role;

public class RolePackage {

	private Role r;
	private int i;
	
	public RolePackage(Role r, int i){
		this.r = r;
		this.i = i;
	}
	
	public Role getRole(){
		return r;
	}
	
	public int getTeam(){
		return i;
	}
	
}
