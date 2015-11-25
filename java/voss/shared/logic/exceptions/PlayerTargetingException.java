package voss.shared.logic.exceptions;

import voss.shared.logic.Player;

@SuppressWarnings("serial")
public class PlayerTargetingException extends Error {

	public PlayerTargetingException(String string) {
		super(string);
	}

	public PlayerTargetingException() {
		super();
	}

	public PlayerTargetingException(Player owner, Player target, int ability) {
		this(owner.getName() + " is trying to target " + target.getName() + " using ability " + ability);
	}

}
