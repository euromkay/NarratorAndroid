package voss.shared.logic.support;

import voss.shared.logic.Narrator;

public interface Alignment {

	boolean opposes(Alignment a2, Narrator n);

	int[] getTeams();
	
}
