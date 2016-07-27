package android.parse;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.NarratorService;
import android.SuccessListener;
import android.alerts.GameBookPopUp;
import android.app.Activity;
import android.net.Uri;
import android.screens.ActivityHome;
import android.setup.SetupDeliverer;
import android.setup.SetupManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.support.RoleTemplate;
import shared.logic.support.rules.Rules;
import shared.packaging.Packager;
import voss.narrator.R;


@SuppressWarnings("unused")
public class Server {

    public interface LoginListener{
        void onSuccess();
        void onBadPassword();
        void onBadEmail();
        void onEmailTaken();
        void onUsernameTaken();
    }



    public interface GameRegister{
        void onSuccess(GameListing gl);
        void onFailure(String t);
    }

    public interface GameFoundListener{
        void onGamesFound(ArrayList<GameListing> list);
        void noGamesFound();
        void onInvalidToken();
        void onError(String s);
    }

    private static FirebaseAuth mAuth;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    public static void Init(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
                // ...
            }
        };
    }

    public static boolean IsLoggedIn(){
        return mAuth.getCurrentUser() != null;
    }

    public static void Start(){
        mAuth.addAuthStateListener(mAuthListener);
    }

    public static void Stop(){
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public static void LogOut(){
        FirebaseAuth.getInstance().signOut();
    }



    public static void Login(String username, String password, final ActivityHome ah, final SuccessListener sL){
        if (username.length() == 0 || password.length() == 0){
            return;
        }
        username = username + "@sc2mafia.com";
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(ah, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            sL.onFailure("Signin failed: " + task.getException());
                        }else
                            sL.onSuccess();
                    }
                });
    }

    public static void SignUp(String username, final String password, final SuccessListener sL, final ActivityHome ah){
        if(username.contains("@")){
            sL.onFailure("Username cannot be an email.");
        }
        if (username.length() == 0 || password.length() == 0){
            return;
        }
        final String displayName = username.replaceAll("\\s","");
        final String email = displayName + "@sc2mafia.com";


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ah, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            sL.onFailure("Signup failed");
                        }else{
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sL.onSuccess();
                                            }
                                        }
                                    });
                        }

                    }
                });
    }


    public static void RegisterGame(final ActivityHome a, final GameRegister g){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereEqualTo(ParseConstants.INSTANCE_HOST_KEY, ParseUser.getCurrentUser().getUsername());
        query.whereEqualTo(ParseConstants.ACTIVE, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gameLists, ParseException e) {
                if (e == null) {
                    if (gameLists.size() == 0) {
                        CreateGame(g);
                    } else {
                        GameListing gl = new GameListing(gameLists.get(0));
                        GameBookPopUp.joinGame(gl, a, GameBookPopUp.RESUME);
                    }
                } else {
                    g.onFailure(e.getMessage());
                }
            }
        });
    }

    private static void CreateGame(final GameRegister g){
        final ParseObject game = new ParseObject(ParseConstants.NARRATOR_INSTANCE);
        game.put(ParseConstants.INSTANCE_HOST_KEY, GetCurrentUserName());
        game.put(ParseConstants.ACTIVE, true);
        game.put(ParseConstants.STARTED, false);
        game.put(ParseConstants.SEED, 0);
        game.put(ParseConstants.WHEN, 0);
        game.put(ParseConstants.EVENTS, new ArrayList<String>());

        ArrayList<String> list = new ArrayList<>();
        game.put(ParseConstants.ROLES, list);

        list = new ArrayList<>();
        list.add(GetCurrentUserName());
        game.put(ParseConstants.PLAYERS, list);

        game.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    GameListing gl = new GameListing(game);
                    g.onSuccess(gl);
                    Channel(gl);
                } else
                    g.onFailure(e.getMessage());
            }
        });
    }

    public static String GetCurrentUserName(){
        if(Server.IsLoggedIn())
            return mAuth.getCurrentUser().getDisplayName();
        return "";
    }

    public static void GetAllGames(int limit, final GameFoundListener gf){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereNotEqualTo(ParseConstants.INSTANCE_HOST_KEY, GetCurrentUserName());
        query.whereNotEqualTo(ParseConstants.PLAYERS, GetCurrentUserName());//not in the game
        query.whereEqualTo(ParseConstants.STARTED, Boolean.FALSE);
        query.whereEqualTo(ParseConstants.ACTIVE, Boolean.TRUE);
        query.setLimit(limit);

        GetGames(query, gf);
    }

    public static void GetMyGames(final GameFoundListener gf){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereEqualTo(ParseConstants.PLAYERS, GetCurrentUserName());
        query.whereEqualTo(ParseConstants.ACTIVE, Boolean.TRUE);

        GetGames(query, gf);
    }

    private static void GetGames(ParseQuery<ParseObject> query, final GameFoundListener gf){
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    if (list == null || list.size() == 0) {
                        gf.noGamesFound();
                        return;
                    }
                    ArrayList<GameListing> games = new ArrayList<>();
                    GameListing gl;
                    for (ParseObject po : list) {
                        gl = new GameListing(po);
                        if(gl.getPlayerNames().contains(Server.GetCurrentUserName())) {
                            games.add(gl);
                            continue;
                        }
                        if(gl.getRoleNames().isEmpty())
                            continue;
                        if(gl.getRoleNames().size() == gl.getPlayerNames().size())
                            continue;
                        games.add(gl);
                    }
                    if(games.isEmpty())
                        gf.noGamesFound();
                    else
                        gf.onGamesFound(games);
                } else if (e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                    gf.onInvalidToken();
                } else {
                    gf.onError(e.getMessage());
                }
            }
        });
    }

    public static void GetNarratorInfo(String id, GetCallback<ParseObject> gcb){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.getInBackground(id, gcb);
    }

    public static void AddPlayer(final GameListing gl){
        Channel(gl, new SaveCallback() {
			public void done(ParseException e) {
                HashMap<String, Object> params = new HashMap<>();
                params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
                ParseCloud.callFunctionInBackground(ParseConstants.ADD_PLAYER, params, new FunctionCallback<ParseObject>() {
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e != null) {
                            Log.e("Server", e.getMessage());
                            Unchannel(gl);
                        } else {
                            Log.e("Server", parseObject + "");
                        }
                    }
                });
            }
        });
    }

    public static void LeaveGame(final GameListing gl){
        HashMap<String, Object> params = new HashMap<>();
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_PLAYER, params, new FunctionCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.e("Server", e.getMessage());
                } else {
                    Unchannel(gl);
                    Log.e("Server", parseObject + "");
                }
            }
        });
    }


    public static void AddRole(RoleTemplate rt, GameListing gl, final Activity a){
        HashMap<String, Object> params = new HashMap<>();
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        params.put(ParseConstants.ROLES, rt.toIpForm());
        ParseCloud.callFunctionInBackground(ParseConstants.ADD_ROLE, params, new FunctionCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Toast.makeText(a, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Server", parseObject + "");
                }
            }
        });
    }



    public static void RemoveRole(String roleName, String roleColor, GameListing gl, final Activity a){
        HashMap<String, Object> params = new HashMap<>();
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        //params.put(ParseConstants.ROLES, rt.toIpForm());//change this to work with server
        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_ROLE, params, new FunctionCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Toast.makeText(a, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Server", parseObject + "");
                }
            }
        });
    }


    public static void Unchannel(GameListing l){
        ParsePush.unsubscribeInBackground("c" + l.getID());
    }

    public static void Channel(GameListing gl){
        Channel(gl, new SaveCallback() {
			public void done(ParseException e) {

            }
        });
    }

    public static void Channel(GameListing gl, SaveCallback sc){
        ParsePush.subscribeInBackground("c" + gl.getID(), sc);
    }

    public static void StartGame(Narrator narrator, GameListing gl, final SuccessListener sl){
        List<String> players = new ArrayList<String>();
        List<String> roles = new ArrayList<String>();
        for(Player p: narrator.getAllPlayers())
            players.add(p.getName());

        for(RoleTemplate rt: narrator.getAllRoles())
            roles.add(rt.toIpForm());

        SetupDeliverer sd = new SetupDeliverer();
        Packager p = new Packager(sd);
        narrator.getRules().writeToPackage(p);


        HashMap<String, Object> params = new HashMap<>();
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        params.put(ParseConstants.ROLES, roles);
        params.put("ruless", sd.toString());
        params.put(ParseConstants.PLAYERS, players);
        Log.i("Server start game", gl.getPlayerNames().size() + "/" + gl.getRoleNames().size());
        params.put(ParseConstants.WHEN, narrator.getRules().getBool(Rules.DAY_START));
        ParseCloud.callFunctionInBackground(ParseConstants.STARTGAME, params, new FunctionCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    sl.onFailure(e.getMessage());
                } else {
                    sl.onSuccess();
                }
            }
        });
    }

    public static void UpdateGame(final NarratorService ns, final SuccessListener sl){
        GetNarratorInfo(ns.getGameListing().getID(), new GetCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    sl.onFailure(e.getMessage());
                } else {
                    GameListing gl = new GameListing(parseObject);
                    ns.setGameListing(gl);
                    //ns.refresh();
                    for (String s : gl.getPlayerNames()) {
                        ns.local.addPlayer(s);
                    }
                    for (String s : gl.getRoleNames()) {
                        RoleTemplate rt = RoleTemplate.FromIp(s);
                        rt = SetupManager.TranslateRole(rt);
                        ns.local.addRole(rt);
                    }
                    ns.local.setSeed(gl.getSeed());
                    sl.onSuccess();
                }
            }
        });

    }

    public static void PushCommand(GameListing gl, final String s, final double when){
        HashMap<String, Object> params = new HashMap<>();
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        params.put(ParseConstants.PUSH, s);
        params.put(ParseConstants.WHEN, when);
        ParseCloud.callFunctionInBackground(ParseConstants.PUSH, params);
    }


    public static void SetGameInactive(GameListing gl){
        gl.getParseObject().put(ParseConstants.ACTIVE, false);
        gl.getParseObject().saveInBackground();
    }


    public static void ResumeGame(String id, final NarratorService ns, final SuccessListener ls){
        GetNarratorInfo(id, new GetCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    //ns.refresh();
                    GameListing gl = new GameListing(parseObject);
                    ns.setGameListing(gl);

                    for (String p : gl.getPlayerNames())
                        ns.local.addPlayer(p);

                    for (String r : gl.getRoleNames())
                        ns.local.addRole(SetupManager.TranslateRole(RoleTemplate.FromIp(r)));

                    ns.local.setSeed(gl.getSeed());
                    ns.local.startGame();

                    int i;
                    for (String c : gl.getCommands()) {
                        i = c.indexOf(",");
                        c = c.substring(i + 1);
                        ns.onRead(c, null);
                    }
                    ls.onSuccess();
                } else
                    ls.onFailure(e.getMessage());
            }
        });
    }

    public static final void UpdateRules(GameListing gl, Rules r){
        SetupDeliverer sd = new SetupDeliverer();
        Packager p = new Packager(sd);
        r.writeToPackage(p);

        HashMap<String, Object> params = new HashMap<>();
        params.put("ruless", sd.toString());
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        ParseCloud.callFunctionInBackground(ParseConstants.RULES, params);
    }


    public static final void CheckVersion(int version, final FunctionCallback<?> t){
        HashMap<String, Object> params = new HashMap<>();
        params.put("v", version);
        /*ParseCloud.callFunctionInBackground("checkVersion", params, new FunctionCallback<Object>() {
            public void done(Object o, ParseException e) {
                if(e == null) {
                    if(o.getClass() == Boolean.class)
                        if(!(Boolean) o)
                            t.done(null, null);
                }
            }
        });*/
    }

	public static boolean isHost() {
		return false;
	}
}
