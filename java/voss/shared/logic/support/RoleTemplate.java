package voss.shared.logic.support;

import java.util.Comparator;

import voss.shared.logic.Member;
import voss.shared.logic.Player;
import voss.shared.roles.RandomRole;
import voss.shared.roles.Role;

public abstract class RoleTemplate {

	public abstract RolePackage getRole(Player p);
	//TODO
	
	public abstract String getName();
	
	public abstract int getColor();

	public static Comparator<? super RoleTemplate> RandomComparator() {
		return new Comparator<RoleTemplate>(){
		public int compare(RoleTemplate r1, RoleTemplate r2){
				int color1 = r1.getColor();
				int color2 = r2.getColor();

				if (color1 != color2){
					return teamColorOrdering(color1, color2);
				}


				boolean l1 = isRandom(r1);
				boolean l2 = isRandom(r2);
				if(l1 && !l2)
					return 1;
				if(!l1 && l2)
					return -1;


				return r1.getName().compareTo(r2.getName());
			}
		};
	}
	
	public static RoleTemplate FromIp(String s){
		String[] sects = s.split(Constants.SEPERATOR);
		int color = Integer.parseInt(sects[0]);
		String name = sects[1];
		return Creator(name, color);
	}
	
	public String toIpForm(){
		return getColor() + Constants.SEPERATOR + getName();
	}
	
	public boolean isRandom(){
		return RoleTemplate.isRandom(this);
	}
	
	public static RoleTemplate Creator(String name, int color){
		if (Role.isRole(name)){
			return new Member(name, color);
		}else{
			return new RandomRole(name, color);
		}
	}
	
	public static boolean isRandom(RoleTemplate rT){
		return !Role.isRole(rT.getName());
	}
	
	public static final Integer[] teamOrderings = new Integer[]{Constants.A_TOWN, Constants.A_MAFIA, Constants.A_YAKUZA, Constants.A_BENIGN, Constants.A_CULT, Constants.A_OUTCASTS, Constants.A_SK, Constants.A_ARSONIST, Constants.A_MM, Constants.A_NEUTRAL, Constants.A_RANDOM};

	public static int teamColorOrdering(int t1, int t2){
		int i = java.util.Arrays.asList(teamOrderings).indexOf(t1);
		int j = java.util.Arrays.asList(teamOrderings).indexOf(t2);
		if(i == -1 || j == -1){
			System.out.println();
		}
		return difference(i, j);
	}
	
	public static int difference(int i, int j){
		if (i < j)
			return -1;
		if (i > j)
			return 1;


		return 0;
	}
	
}
