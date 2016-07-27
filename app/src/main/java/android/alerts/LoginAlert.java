package android.alerts;


import android.SuccessListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.parse.Server;
import android.screens.ActivityHome;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import voss.narrator.R;

public class LoginAlert extends DialogFragment implements View.OnClickListener, SuccessListener{

    public View mainView;
    private ActivityHome activity;
    public void setServer(ActivityHome a){
        activity = a;
    }

    public EditText userET, pwET;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        mainView = inflater.inflate(R.layout.home_login, null);
        mainView.findViewById(R.id.login_loginButton).setOnClickListener(this);
        mainView.findViewById(R.id.login_signup).setOnClickListener(this);
        mainView.findViewById(R.id.login_cancelLogin).setOnClickListener(this);

        builder.setView(mainView);

        userET = (EditText) mainView.findViewById(R.id.login_username);
        pwET   = (EditText) mainView.findViewById(R.id.login_password);
        
        return builder.create();
    }



    public void onClick(View v){
        switch (v.getId()){

            case R.id.login_loginButton:

                String username = userET.getText().toString();
                String password = pwET.getText().toString();

                Server.Login(username, password, activity, this);
                break;
            case R.id.login_signup:
                userET = (EditText) mainView.findViewById(R.id.login_username);
                pwET = (EditText) mainView.findViewById(R.id.login_password);

                username = userET.getText().toString();
                for(char c: username.toCharArray()){
                    if (!Character.isLetterOrDigit(c)){
                        setHeader("Only letters and numbers in username please!");
                        return;
                    }
                }

                password = pwET.getText().toString();

                Server.SignUp(username, password, this, activity);
                break;
            case R.id.login_cancelLogin:
                getDialog().cancel();
                break;
        }
    }

    public void onSuccess(){
        activity.toast("Successfully logged in");
        Button v = (Button) activity.findViewById(R.id.home_login_signup);
        v.setText("Signout");
        try {
            getDialog().cancel();
        }catch(NullPointerException e){}
    }

    public void onFailure(String message){
        setHeader(message);
    }

    /*public void onBadPassword(){
        setHeader("Incorrect password");
    }

    public void onBadEmail(){
        setHeader("Invalid email.");
    }

    public void onEmailTaken(){
        setHeader("Email is already in use!");
    }

    public void onUsernameTaken(){
        setHeader("Username is already taken!");
    }*/

    public void setHeader(String s){
        TextView tv = (TextView) mainView.findViewById(R.id.home_loginErrorHandler);
        tv.setText(s);
        if (tv.getVisibility() != View.VISIBLE)
            tv.setVisibility(View.VISIBLE);
    }
}
