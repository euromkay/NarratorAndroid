package voss.shared.logic.support;

public class Constants {

	public static final String ANY_RANDOM_ROLE_NAME = "Any Random";
	
	public static final String TOWN_RANDOM_ROLE_NAME        = "Town Random";
	public static final String TOWN_PROTECTIVE_ROLE_NAME    = "Town Protective";
	public static final String TOWN_INVESTIGATIVE_ROLE_NAME = "Town Investigative";
	public static final String TOWN_KILLING_ROLE_NAME       = "Town Killing";
	public static final String TOWN_GOVERNMENT_ROLE_NAME    = "Town Government";
	
	public static final String MAFIA_RANDOM_ROLE_NAME  = "Mafia Random";
	public static final String YAKUZA_RANDOM_ROLE_NAME = "Yakuza Random";
	
	public static final String NEUTRAL_RANDOM_ROLE_NAME = "Neutral Random";
	
	public static final String NO_NIGHT_ACTION       = "You have no night action!  Type \'end night\' to end the night";
	public static final String NO_VALID_NIGHT_ACTION = "You don't have any night actions";
	//public static final String END_NIGHT             = "You have voted to end the night";

	public static final String CANCEL = "cancel";


	public static final String NAME_SPLIT = ":\t";
	public static final String SEPERATOR = "#";
	public static final String INET_SEPERATOR = "\t:";  //LENGTH OF 2

	public static final String SUBMIT_NAME = "SUBMIT_NAME ";
	public static final String NAME_OK = "NAME_OKAY ";
	public static final String NAME_BAD = "NAME_BAD ";
	public static final String NEW_PLAYER_ADDITION = "NEW_PLAYER ";
	public static final String ALLOW_CONTROL = "ALLOW_CONTROL ";
	public static final String NAME_CHANGE = "NAME_CHANGE ";
	public static final String REMOVE_PLAYER = "REMOVE_PLAYER ";

	public static final String ADD_ROLE = "ADD_role";
	public static final String REMOVE_ROLE = "REMOVE_ROLE ";

	public static final String REQUEST_INFO = "request_info";
	public static final String FINISH_INITIAL_REQUEST = "FINISH_initial_req";

	public static final String START_GAME = "START_GAME ";

	public static final String SET_RULES = "SET_RULES ";

	//Commands
    public static final String MODKILL = "modkill";
    public static final String END_NIGHT = "end night";
    public static final String VOTE = "vote";
    public static final String UNVOTE = "unvote";
    public static final String UNTARGET = "untarget";
    public static final String SKIP_VOTE = "skip day";
    public static final String SAY = "say";
	
	
	public static final String NOTHING_DONE = "You chose to do nothing tonight.";

	
	public static final String HEAL_FEEDBACK_DOCTOR = "You were healed by a doctor!";

	public static final String NIGHT_IMMUNE_FEEDBACK = "Your target was immune at night!";
	public static final String NIGHT_IMMUNE_TARGET_FEEDBACK = "You were attacked last night but you survived the encounter.";

	public static final String MAFIA_SENT_FEEDBACK = " was sent to kill ";

	//winstrings
	public static final String NO_WINNER = "Nobody won!";
	
	public static final String MODKILL_ROLE_NAME = "ModKill";
	
	//FLAGS @ 4
	public static final int DOCTOR_HEAL_FLAG = 1;
	public static final int BODYGUARD_KILL_FLAG = 2;
	
	public static final int VIGILANTE_KILL_FLAG = 3;
	public static final int SK_KILL_FLAG = 4;
	public static final int JESTER_KILL_FLAG = 5;
	
	public static final int MODKILL_FLAG = 6;
	
	public static final int LYNCH_FLAG = 7;
	
	public static final int VETERAN_KILL_FLAG = 8;
	public static final int MASS_MURDERER_FLAG = 9;

	public static final int NOT_ATTACKED = 10;
	
	public static final int ARSON_KILL_FLAG = 11;

	
	
	
	public static final String RULES_DAYSTART = "rulesdaystart";
	
	
	public static final String RULES_DOCTOR_HEAL_SELF = "rulesDocSelfheals";
	public static final String RULES_DOCTOR_SUCCESS_NOTIFICATION = "rulesDocSuccessNotif";
	
	public static final String RULES_EXEC_INVUL = "execInvul";
	
	public static final String RULES_SK_INVULN = "rulesSkInvul";

	public static final int A_NORMAL    = 0;
	public static final int A_OUTCASTS  = -10066432;
	public static final int A_TOWN      = -10185235;
	public static final int A_BENIGN    = -2252579;
	public static final int A_YAKUZA    = -10496;
	public static final int A_MAFIA     = -65536;
	public static final int A_SK        = -7650029;
	public static final int A_SKIP      = -2;
	public static final int A_RANDOM    = -1;
	public static final int A_NEUTRAL   = -7829368;//used for the neutral random color
	public static final int A_INVALID   = 37;
	public static final int A_MM        = -3308226;
	public static final int A_CULT      = -16711936;
	public static final int A_ARSONIST  = -29696;
	
	public static final String PUBLIC      = "PUBLIC";
	public static final String PRIVATE     = "PRIVATE";








	



	
	
	

}
