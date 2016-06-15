package android.screens;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseException;

import android.ActivityTutorial;
import android.CommunicatorPhone;
import android.NActivity;
import voss.narrator.R;
import android.SuccessListener;
import android.alerts.GameBookPopUp;
import android.alerts.IpPrompt;
import android.alerts.IpPrompt.IpPromptListener;
import android.alerts.LoginAlert;
import android.alerts.NamePrompt;
import android.alerts.NamePrompt.NamePromptListener;
import android.alerts.PhoneBookPopUp;
import android.alerts.PhoneBookPopUp.AddPhoneListener;
import android.day.ActivityDay;
import android.parse.GameListing;
import android.parse.Server;
import android.setup.ActivityCreateGame;
import android.texting.CommunicatorText;
import android.texting.PhoneNumber;
import android.texting.ReceiverText;
import android.wifi.SocketClient;
import shared.logic.Narrator;
import shared.logic.Player;

public class ActivityHome extends NActivity implements OnClickListener, IpPromptListener, NamePromptListener, AddPhoneListener {


	public void creating(Bundle b){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
		
		setText(R.id.home_join);
		setText(R.id.home_host);
		setText(R.id.home_login_signup);
		setText(R.id.home_tutorial);
		setText(R.id.home_currentGames);


		if(isLoggedIn()){
			TextView tv = (TextView) findViewById(R.id.home_login_signup);
			tv.setText("Sign Out");
		}else{

		}


		try {
			final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			Server.CheckVersion(pInfo.versionCode, new FunctionCallback() {
				public void done(Object o, ParseException e) {
					displayUpdate();
				}

				public void done(Object o, Throwable throwable) {
					displayUpdate();
				}
			});
		}catch(PackageManager.NameNotFoundException e){}
		connectNarrator(null);
	}

	private void displayUpdate(){
		findViewById(R.id.home_update).setVisibility(View.VISIBLE);
	}
	
	public Narrator getNarrator(){
		return ns.getNarrator();
	}

	protected void onCreate(Bundle b) {
		super.onCreate(b);
		creating(b);
	}

	private void setText(int id){
		SetFont(id, this, true);
		findViewById(id).setOnClickListener(this);
	}

	


	public Context passContext(){
		return this;
	}

	private boolean isLoggedIn(){
		return Server.IsLoggedIn();
	}

	public static int buildNumber(){
		return Build.VERSION.SDK_INT;
	}

	public void onClick(View v) {
		switch(v.getId()){

			case R.id.home_host:
				if(isLoggedIn()) {
					Server.RegisterGame(this, new Server.GameRegister() {
						public void onSuccess(GameListing gl) {
							ns.refresh();
							ns.addPlayer(Server.GetCurrentUserName(), new CommunicatorPhone());
							start(gl);
						}

						public void onFailure(String t) {
							toast(t);
						}
					});
				}else{
					if(buildNumber() < 16)
						toast("You will not be able to participate in wireless games.");
					showNamePrompt("Host");
				}
				break;

			case R.id.home_join:
				if(isLoggedIn()) {
					displayGames(GameBookPopUp.JOIN);
				}else if(networkCapable()){
					showIpPrompt();
					//showNamePrompt("Join");
				}else{
					toast("Your device isn't capable of local hosting.");
				}
				break;

			case R.id.home_currentGames:
				if(isLoggedIn()){
					displayGames(GameBookPopUp.RESUME);
				}else{
					if(ns.local.getAllPlayers().isEmpty())
						onClick(findViewById(R.id.home_host));
					else
						start(null);
				}
				break;

			case R.id.home_tutorial:
				startTutorial();
				break;

			case R.id.home_login_signup:
				toast("Temporarily unavailable.");
				break;
				/*if (isLoggedIn()){
					Server.LogOut();
					TextView tv = (TextView) v;
					tv.setText("Login/Signup");
				}else if(!isInternetAvailable()){
					LoginAlert loginer = new LoginAlert();
					loginer.setServer(this);
					loginer.show(getFragmentManager(), "logginer");
				}else{
					toast("You must be connected to the internet to login.");
				}
				break;*/

		}
	}

	public void displayGames(int mode){
		GameBookPopUp gb = new GameBookPopUp();
		gb.setMode(mode);
		gb.show(getFragmentManager(), "gamebook");
	}

	public boolean isInternetAvailable() {
		try {
			InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
			if (ipAddr.equals("")) 
				return false;
			else 
				return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void startTutorial(){
		Intent i = new Intent(this, ActivityTutorial.class);
		startActivity(i);
		//finish();
	}

	

	private void showNamePrompt(String buttonText){
		Bundle bundle = new Bundle();
		String name = retrName();
		if(name != null)
			bundle.putString(HOST_NAME, name);
		bundle.putString(NamePrompt.GO_BUTTON, buttonText);

		DialogFragment pList = new NamePrompt();
		pList.setArguments(bundle);
		pList.show(getFragmentManager(), "namePrompt");
	}

	//this asks joiners what the host ip address is
	private void showIpPrompt(){
		DialogFragment pList = new IpPrompt();
		pList.show(getFragmentManager(), "ipprompt");
	}

	public void onNamePromptConfirm(final NamePrompt np, String name, boolean isHost){
		SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
		prefs.putString(HOST_NAME, name);
		prefs.commit();

		ns.refresh();

		if (isHost){
			np.dismiss();
			ns.addPlayer(name, new CommunicatorPhone());
			if(networkCapable())
				ns.startHost(this, name);

			if(Build.VERSION.SDK_INT >= 18 && hasPhoneInvite()) {
				PhoneBookPopUp pList = new PhoneBookPopUp();
				pList.setIsHost();
				pList.show(getFragmentManager(), "phoneBookPopup");
				toast("Clicking people and pressing invite will send them texts.");
			}else{
				start(null);
			}
		}else{
			ns.submitName(name, new SuccessListener(){
				public void onSuccess() {
					np.dismiss();
					start(null);
				}

				public void onFailure(String m) {
					toast("That name's already taken.");
				}
				
			});
		}
	}

	private boolean hasPhoneInvite(){
		return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, "android.permission.READ_CONTACTS");
	}

	public class IpChecker extends AsyncTask<IpPrompt, IpPrompt, IpPrompt> {
		protected IpPrompt doInBackground(IpPrompt ... i){
			IpPrompt iprompt = i[0];
			String ip = iprompt.getIP();
			boolean b;
			try{
				b = InetAddress.getByName(ip).isReachable(3000);
			} catch (IOException e){
				b = false;
			}

			if(b)
				return iprompt;
			else
				return null;
		}
		public void onPostExecute(final IpPrompt ip){
			boolean b = ip == null;
			if (b){
				toast("wrong code, try again");
			}else{
				ns.startClient(ActivityHome.this, ip.getIP(), new SocketClient.ClientListener(){
					public void onHostConnect() {
						ip.dismiss();
						showNamePrompt("Join");
					}
					
				});
			}
		}
	}

	public void onIpPromptConfirm(IpPrompt np){
		new IpChecker().execute(np);
	}

	protected void joinText(){
		PhoneBookPopUp pList = new PhoneBookPopUp();
		pList.setButton("Join");
		pList.show(getFragmentManager(), "stuff2");
	}

	public void startJoinProcess(PhoneNumber number){
		toast("Joining Game");
	}

	public void startGame(PhoneBookPopUp popup, HashMap<String, PhoneNumber> contacts, boolean isHost){
		if(isHost) {
			PhoneNumber number;
			for (String name : contacts.keySet()) {
				Log.e("PhoneInvite", name);
				number = contacts.get(name);
				ReceiverText.sendText(number, "You have been invited to the Narrator.  You are " + name + ". If that's not who you are, tell me your name.");
				CommunicatorText cp = new CommunicatorText(number);
				ns.addPlayer(name, cp);
			}
			popup.dismiss();
			start(null);
		}
	}




	

	public static final String HOST_NAME = "host_name";
	private String retrName(){
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		return prefs.getString(HOST_NAME, null);
	}

	public void toast(String s){
		Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
	}



	public static final String ISHOST = "ishost_activityhome";
	public static final String MYNAME = "myname_activityhoome";
	public void start(GameListing gl){
		if(isLoggedIn() && gl != null)
			ns.setGameListing(gl);
		Class<?> activ;
		if(ns.getNarrator().isStarted())
			activ = ActivityDay.class;
		else {
            activ = ActivityCreateGame.class;
        }
		Intent i = new Intent(this, activ);
		startActivity(i);
		//finish();
	}
	
	public void onBackPressed(){
		finish();
		System.exit(0);
	}

	/** register the BroadcastReceiver with the intent values to be matched */
	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}



	public void onRead(String s, Player c){
		toast(s);
	}

	public Activity getActivity(){
		return this;
	}

	public void notifyReady(){

	}

	public static final String PLAYER_NAME = "player_name_forwifihost";

	public void onDestroy(){

		if(ns!=null) {
			ns.onDestroy();
		}this.unbindNarrator();
		super.onDestroy();
	}
}