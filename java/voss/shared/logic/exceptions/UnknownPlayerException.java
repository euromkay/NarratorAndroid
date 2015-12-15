package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class UnknownPlayerException extends Error{

	public UnknownPlayerException(String s){
		super(s);
	}
	
	public UnknownPlayerException() {
		this("");
	}

}
