package voss.shared.logic.support;

public class Equals {
	
	
	public static boolean notEqual(Object o, Object p){
		if(o == null){
			if(p != null){
				return true;
			}
		}else{
			if(!o.equals(p))
				return true;	
		}
		return false;
	}
	
	public static boolean notEqualArrays(Object[] o, Object[] p){
		if(o == null){
			if(p != null){
				return true;
			}
		}
		else{
			if(o.length != p.length){
				return true;
			}
			for(int i = 0; i < o.length; i++){
				if(notEqual(o[i], p[i]))
					return true;
			}	
		}
		return false;
			
	}

	public static boolean notEqualArrays(int[] o, int[] p) {
		if(o == null){
			if(p != null){
				return true;
			}
		}
		else{
			if(o.length != p.length){
				return true;
			}
			for(int i = 0; i < o.length; i++){
				if(o[i] != p[i])
					return true;
			}	
		}
		return false;
	}
}
