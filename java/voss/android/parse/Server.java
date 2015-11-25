package voss.android.parse;


import android.util.Log;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class Server {

    public interface LoginListener{
        void onSuccess();
        void onBadPassword();
        void onBadEmail();
        void onEmailTaken();
    }

    public interface GameRegister{
        void onSuccess();
        void onFailure(String t);
    }

    public interface GameFoundListener{
        void onGamesFound(ArrayList<GameListing> list);
        void noGamesFound();
    }

    public void logOut(){
        ParseUser.logOut();
    }

    public void login(String username, String password, final LoginListener loginListener){
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

    public void signUp(String username, String password, String email, final LoginListener loginListener){
        if (username.length() == 0 || password.length() == 0){
            return;
        }
        ParseUser user = new ParseUser();
        user.setUsername(username);
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


    public void registerGame(final GameRegister g){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereEqualTo(ParseConstants.INSTANCE_HOST_KEY, ParseUser.getCurrentUser().getUsername());
        query.whereEqualTo(ParseConstants.ACTIVE, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gameLists, ParseException e) {
                if (e == null) {
                    if (gameLists.size() == 0) {
                        addGame(g);
                        return;
                    } else {
                        g.onFailure("You can't host more then one game at a time!");
                    }
                } else {
                    g.onFailure(e.getMessage());
                }
            }
        });
    }

    private void addGame(final GameRegister g){
        ParseObject game = new ParseObject(ParseConstants.NARRATOR_INSTANCE);
        game.put(ParseConstants.INSTANCE_HOST_KEY, getCurrentUserName());
        game.put(ParseConstants.ACTIVE, true);
        ArrayList<String> list = new ArrayList<>();
        list.add("dshfi");
        list.add("haskdfasd");
        game.put(ParseConstants.PLAYERS, list);
        game.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    g.onSuccess();
                } else
                    g.onFailure(e.getMessage());
            }
        });
    }

    private static String getCurrentUserName(){
        return ParseUser.getCurrentUser().getUsername();
    }

    public static void GetAllGames(int limit, final GameFoundListener gf){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.NARRATOR_INSTANCE);
        query.whereEqualTo(ParseConstants.INSTANCE_HOST_KEY, getCurrentUserName());
        query.setLimit(limit);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    if (list.size() == 0){
                        gf.noGamesFound();
                        return;
                    }
                    GameListing gl;
                    ArrayList<GameListing> games = new ArrayList<>();
                    for (ParseObject po: list){
                        gl = new GameListing();

                        String hostname = po.getString(ParseConstants.INSTANCE_HOST_KEY);
                        gl.setName(hostname);

                        //ArrayList
                        //gl.setPlayers()

                        games.add(gl);
                    }

                    gf.onGamesFound(games);
                }else{

                }
            }
        });
    }
}
