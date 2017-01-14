package android.parse;


import java.util.HashMap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;

import android.JUtils;
import android.NActivity.NarratorConnectListener;
import android.SuccessListener;
import android.screens.ActivityHome;
import android.support.annotation.NonNull;
import android.texting.StateObject;
import json.JSONException;
import json.JSONObject;


public class Server implements FirebaseAuth.AuthStateListener{

    public interface LoginListener{
        void onSuccess();
        void onBadPassword();
        void onBadEmail();
        void onEmailTaken();
        void onUsernameTaken();
    }


    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    public Server(FirebaseAuth.AuthStateListener ol){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = ol;

        Start();
        if(testing)
        	mAuthListener.onAuthStateChanged(mAuth);
    }
    
    public static boolean testing = false;
    
    NarratorConnectListener ncl;
    public synchronized void onConnected(NarratorConnectListener ncl) {
    	if(getStatus() != NOT_STARTED)
    		ncl.onConnect();
    	else
    		this.ncl = ncl;
	}

    public void getAuthToken(OnCompleteListener<GetTokenResult> x){
        Task<GetTokenResult> task = mAuth.getCurrentUser().getToken(true);
        task.addOnCompleteListener(x);
    }

    
    public void Destroy(){
        mAuth.removeAuthStateListener(mAuthListener);
        mAuth.removeAuthStateListener(this);
    	mAuth = null;
    	mAuthListener = null;
    }

    public boolean IsLoggedIn(){
        return mAuth.getCurrentUser() != null;
    }

    public void Start(){
        mAuth.addAuthStateListener(this);
    }

    public static final int NOT_STARTED = -1, LOGGED_IN = 0, LOGGED_OUT = 1;
    private int state = NOT_STARTED;
    public void onAuthStateChanged(FirebaseAuth fa){
        synchronized(this){
            if(fa.getCurrentUser() == null){
                state = LOGGED_OUT;
            }else
                state = LOGGED_IN;
            if(ncl != null) {
                //this is the narrator service waiting for firebase to hit
                ncl.onConnect();
                ncl = null;
            }
        }

        //this is the activity listening for stuff
        if(mAuthListener != null)
            mAuthListener.onAuthStateChanged(fa);
    }

    public synchronized int getStatus(){
        return state;
    }

    public void Stop(){
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void LogOut(){
        FirebaseAuth.getInstance().signOut();
    }



    public void Login(String username, String password, final ActivityHome ah, final SuccessListener sL){
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

    public void SignUp(String username, final String password, final SuccessListener sL, final ActivityHome ah){
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


    public String GetCurrentUserName(){
        if(IsLoggedIn())
            return mAuth.getCurrentUser().getDisplayName();
        return "";
    }








    public static void CheckVersion(int version){
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

	public static void Greet(ActivityHome activity) {
		activity.ns.connectWebSocket();
		
	}

	public static void JoinPublic(ActivityHome aHome) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("action", true);
			jo.put("message", "joinPublic");
            aHome.ns.addActivity(aHome);
			aHome.ns.sendMessage(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        jo = new JSONObject();
        JUtils.put(jo, StateObject.message, StateObject.requestGameState);
        aHome.ns.sendMessage(jo);
	}

	public static void HostPublic(ActivityHome aHome) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("action", true);
			jo.put("message", "hostPublic");
            aHome.ns.addActivity(aHome);
			aHome.ns.sendMessage(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
