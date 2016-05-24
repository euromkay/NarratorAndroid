package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class IllegalRoleCombinationException extends Error{

	public IllegalRoleCombinationException(String string) {
		super(string);
	}

	public IllegalRoleCombinationException() {
		super();
	}

}
