package android.parse;


import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.NarratorService;
import android.SuccessListener;
import android.alerts.GameBookPopUp;
import android.screens.ActivityHome;
import android.setup.SetupDeliverer;
import android.setup.SetupManager;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.Rules;
import shared.logic.support.RoleTemplate;
import shared.packaging.Packager;

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

    public static boolean IsLoggedIn(){
        return ParseUser.getCurrentUser() != null;
    }

    public static void LogOut(){
        ParseUser.logOut();
    }



    public static void Login(String username, String password, final LoginListener loginListener){
        if (username.length() == 0 || password.length() == 0){
            return;
        }
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (e == null && user != null) {
                    loginListener.onSuccess();
                } else if (user == null) {
                    loginListener.onBadPassword();
                } else {
                    switch (e.getCode()) {
                        case ParseException.INVALID_EMAIL_ADDRESS:
                            loginListener.onBadEmail();
                            break;
                        case ParseException.EMAIL_MISSING:
                            loginListener.onBadEmail();
                            break;
                        case ParseException.EMAIL_TAKEN:
                            loginListener.onEmailTaken();
                            break;
                    }
                }
            }
        });
    }

    public static void SignUp(String usern, final String password, String email, final LoginListener loginListener){
        final String username = usern.replaceAll("\\s","");
        if (username.length() == 0 || password.length() == 0){
            return;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("email", email);
        ParseCloud.callFunctionInBackground("signup", params, new FunctionCallback<ParseObject>() {
            public void done(ParseObject x, ParseException e) {
                if (e == null) {
                    try {
                        ParseUser.logIn(username, password);
                    } catch (ParseException f) {
                        Log.e("Server login", f.getMessage());
                    }
                    loginListener.onSuccess();
                } else {
                    Log.e("Server signup error", e.getMessage() + e.getCode());
                    switch (e.getCode()) {
                        case ParseException.INVALID_EMAIL_ADDRESS:
                            loginListener.onBadEmail();
                            break;
                        case ParseException.EMAIL_TAKEN:
                            loginListener.onEmailTaken();
                            break;
                        case 141:
                            loginListener.onUsernameTaken();
                            break;
                        case ParseException.USERNAME_TAKEN:
                            loginListener.onUsernameTaken();
                            break;

                    }
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
        if(ParseUser.getCurrentUser() != null)
            return ParseUser.getCurrentUser().getUsername();
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



    public static void RemoveRole(RoleTemplate rt, GameListing gl, final Activity a){
        HashMap<String, Object> params = new HashMap<>();
        params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
        params.put(ParseConstants.ROLES, rt.toIpForm());
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
        params.put(ParseConstants.WHEN, narrator.getRules().DAY_START);
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
                    ns.refresh();
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
                    ns.refresh();
                    GameListing gl = new GameListing(parseObject);
                    ns.setGameListing(gl);

                    for (String p : gl.getPlayerNames())
                        ns.local.addPlayer(p);

                    for (String r : gl.getRoleNames())
                        ns.local.addRole(SetupManager.TranslateRole(RoleTemplate.FromIp(r)));

                    ns.local.setSeed(gl.getSeed());
                    ns.local.setRules(gl.getRules());
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


    public static final void CheckVersion(int version, final FunctionCallback t){
        HashMap<String, Object> params = new HashMap<>();
        params.put("v", version);
        ParseCloud.callFunctionInBackground("checkVersion", params, new FunctionCallback<Object>() {
            public void done(Object o, ParseException e) {
                if(e == null) {
                    if(o.getClass() == Boolean.class)
                        if(!(Boolean) o)
                            t.done(null, null);
                }
            }
        });
    }
}
