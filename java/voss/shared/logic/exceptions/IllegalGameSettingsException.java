package voss.shared.logic.exceptions;

@SuppressWarnings("serial")
public class IllegalGameSettingsException extends Error{

	public IllegalGameSettingsException(String string) {
		super(string);
	}

	public IllegalGameSettingsException() {
		super();
	}

}
