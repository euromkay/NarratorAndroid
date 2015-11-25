package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class VotingException extends Error{

	public VotingException(String string) {
		super(string);
	}

	public VotingException() {
		super();
	}

}
