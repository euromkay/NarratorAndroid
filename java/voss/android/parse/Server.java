package voss.android.parse;


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
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import voss.shared.logic.support.RoleTemplate;

public class Server {

    public interface LoginListener{
        void onSuccess();
        void onBadPassword();
        void onBadEmail();
        void onEmailTaken();
    }

    public interface GameRegister{
        void onSuccess(String id);
        void onFailure(String t);
    }

    public interface GameFoundListener{
        void onGamesFound(ArrayList<GameListing> list);
        void noGamesFound();
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

    public static void SignUp(String username, String password, String email, final LoginListener loginListener){
        if (username.length() == 0 || password.length() == 0){
            return;
        }
        ParseUser user = new ParseUser();
        user.setUsername(username.toLowerCase());
        user.setEmail(email);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    loginListener.onSuccess();
                } else {
                    switch (e.getCode()) {
                        case ParseException.INVALID_EMAIL_ADDRESS:
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


    public static void RegisterGame(final GameRegister g){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereEqualTo(ParseConstants.INSTANCE_HOST_KEY, ParseUser.getCurrentUser().getUsername());
        query.whereEqualTo(ParseConstants.ACTIVE, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gameLists, ParseException e) {
                if (e == null) {
                    if (gameLists.size() == 0) {
                        CreateGame(g);
                    } else {
                        g.onFailure("You can't host more then one game at a time!");
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
        game.put(ParseConstants.EVENTS, new ArrayList<String>());


        ArrayList<String> list = new ArrayList<>();
        game.put(ParseConstants.ROLES, list);

        list = new ArrayList<>();
        list.add(GetCurrentUserName());
        game.put(ParseConstants.PLAYERS, list);
        game.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    g.onSuccess(game.getObjectId());
                    ParsePush.subscribeInBackground(game.getObjectId());
                } else
                    g.onFailure(e.getMessage());
            }
        });
    }

    public static String GetCurrentUserName(){
        return ParseUser.getCurrentUser().getUsername();
    }

    public static void GetAllGames(int limit, final GameFoundListener gf){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereNotEqualTo(ParseConstants.INSTANCE_HOST_KEY, GetCurrentUserName());
        query.whereNotEqualTo(ParseConstants.PLAYERS, GetCurrentUserName());//not in the game
        query.whereEqualTo(ParseConstants.STARTED, Boolean.FALSE);
        query.setLimit(limit);

        GetGames(query, gf);
    }

    public static void GetMyGames(final GameFoundListener gf){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereEqualTo(ParseConstants.PLAYERS, GetCurrentUserName());

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
                    for (ParseObject po : list)
                        games.add(new GameListing(po));


                    gf.onGamesFound(games);
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
        ParsePush.subscribeInBackground(gl.getID(), new SaveCallback() {
            public void done(ParseException e) {
                HashMap<String, Object> params = new HashMap<>();
                params.put(ParseConstants.NARRATOR_INSTANCE, gl.getID());
                ParseCloud.callFunctionInBackground(ParseConstants.ADD_PLAYER, params, new FunctionCallback<ParseObject>() {
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e != null) {
                            Log.e("Server", e.getMessage());
                            ParsePush.unsubscribeInBackground(gl.getID());
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
                }else{
                    ParsePush.unsubscribeInBackground(gl.getID());
                    Log.e("Server", parseObject+ "");
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
}
