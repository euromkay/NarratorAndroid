package voss.android.parse;


public class ParseConstants {
    public static final String NARRATOR_INSTANCE = "Instances";
    public static final String INSTANCE_HOST_KEY = "hostName";
    public static final String ACTIVE = "active";
    public static final String PLAYERS = "playersInGame";
    public static final String ROLES = "rolesInGame";
    public static final String STARTED = "inProgress";//really unnecessary because you can just check if seed is 0, but maybe not because then you'd have to make sure seed is always 0
    public static final String SEED = "seed";
    public static final String EVENTS = "events";

    public static final String ADD_PLAYER    = "addPlayer";
    public static final String REMOVE_PLAYER = "removePlayer";
    public static final String ADD_ROLE      = "addRole";
    public static final String REMOVE_ROLE   = "removeRole";

    public static final String PARSE_FILTER = "PARSE_RECEIVED_ACTION";

    public static final String STARTGAME = "startGame";
    public static final String PUSH      = "push";

    public static final String WHEN = "when";
}
