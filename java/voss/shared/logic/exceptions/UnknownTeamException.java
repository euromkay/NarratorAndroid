package voss.shared.logic.exceptions;

import voss.shared.logic.Player;

@SuppressWarnings("serial")
public class UnknownTeamException extends Error {

	public UnknownTeamException(String string) {
		super(string);
	}

	public UnknownTeamException() {
		super();
	}

	public UnknownTeamException(Player owner, Player target, int ability) {
		this(owner.getName() + " is trying to target " + target.getName() + " using ability " + ability);
	}

}
