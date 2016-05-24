package voss.shared.logic.support;

import voss.shared.logic.Narrator;
import voss.shared.packaging.Packager;

public interface CommunicatorHandler {

	public Communicator getComm(Packager in, Narrator narrator);

	public void writeHeading(Packager p, Communicator comm);

}
