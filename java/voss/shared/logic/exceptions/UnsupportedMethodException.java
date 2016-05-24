package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class UnsupportedMethodException extends Error{

	public UnsupportedMethodException(String role) {
		super(role);
	}
	
	public UnsupportedMethodException(){
		super();
	}

}
