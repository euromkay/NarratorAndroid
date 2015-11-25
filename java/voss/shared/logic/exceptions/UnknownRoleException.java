package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class UnknownRoleException extends Error{

	public UnknownRoleException(String role) {
		super(role);
	}

}
