package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class PhaseException extends Error{

	public PhaseException(String role) {
		super(role);
	}

}
