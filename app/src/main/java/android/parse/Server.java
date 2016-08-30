package android.parse;


import java.util.HashMap;

import json.JSONException;
import json.JSONObject;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import android.JUtils;
import android.SuccessListener;
import android.screens.ActivityHome;
import android.support.annotation.NonNull;
import android.texting.StateObject;


@SuppressWarnings("unused")
public class Server {

    public interface LoginListener{
        void onSuccess();
        void onBadPassword();
        void onBadEmail();
        void onEmailTaken();
        void onUsernameTaken();
    }


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public void Init(){
    	if(mAuth != null)
    		return;
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
    
    public void Destroy(){
    	mAuth = null;
    	mAuthListener = null;
    }

    public boolean IsLoggedIn(){
    	if(mAuth == null)
    		Init();
        return mAuth.getCurrentUser() != null;
    }

    public void Start(){
        if(mAuth == null)
            Init();
        mAuth.addAuthStateListener(mAuthListener);
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
        if(mAuth == null)
            Init();
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
		activity.ns.connectWebSocket(null);
		
	}

	public static void JoinPublic(ActivityHome aHome) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("action", true);
			jo.put("message", "joinPublic");
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
			aHome.ns.sendMessage(jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
