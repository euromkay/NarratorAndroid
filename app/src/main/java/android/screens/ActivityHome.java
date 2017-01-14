package android.screens;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;

import android.ActivityTutorial;
import android.CommunicatorPhone;
import android.NActivity;
import android.SuccessListener;
import android.alerts.IpPrompt;
import android.alerts.IpPrompt.IpPromptListener;
import android.alerts.LoginAlert;
import android.alerts.NamePrompt;
import android.alerts.NamePrompt.NamePromptListener;
import android.alerts.PhoneBookPopUp;
import android.alerts.PhoneBookPopUp.AddPhoneListener;
import android.alerts.RoleCardPopUp;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.day.ActivityDay;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.parse.Server;
import android.setup.ActivityCreateGame;
import android.support.v4.content.ContextCompat;
import android.texting.CommunicatorText;
import android.texting.PhoneNumber;
import android.texting.ReceiverText;
import android.texting.StateObject;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.wifi.NodeListener;
import json.JSONException;
import json.JSONObject;
import shared.logic.Member;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.templates.BasicRoles;
import voss.narrator.R;

public class ActivityHome extends NActivity implements OnClickListener, IpPromptListener, NamePromptListener, AddPhoneListener, NodeListener {

	private static final int[] home_buttons = {R.id.home_roleCard, R.id.home_join, R.id.home_host, R.id.home_login_signup, R.id.home_tutorial};

	public void creating(Bundle b){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);

		setFont(home_buttons);


		/*try {
			final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			//Server.CheckVersion(pInfo.versionCode, new FunctionCallback() {
				public void done(Object o, ParseException e) {
					displayUpdate();
				}

				public void done(Object o, Throwable throwable) {
					displayUpdate();
				}
			});
		}catch(PackageManager.NameNotFoundException e){}*/


	}

	/*
	private void displayUpdate(){
		findViewById(R.id.home_update).setVisibility(View.VISIBLE);
	}*/

	public void onStart(){
		super.onStart();
		connectNarrator(new NarratorConnectListener() {
			public void onConnect() {
				if (ns.server.IsLoggedIn()) {
					TextView tv = (TextView) findViewById(R.id.home_login_signup);
					tv.setText("Sign Out");
				}
			}
		});
	}



	private void authLog(String x){

		Log.d("myAuth",x);
	}

	//public void onpause

	private Toast loadingGameInfoToast;
	public void onAuthStateChanged(FirebaseAuth fa){

		authLog("auth hit");
		if(fa.getCurrentUser() == null){
			authLog("not logged in");
			Runnable r = new Runnable(){
				public void run(){
					findViewById(R.id.home_host).setVisibility(View.VISIBLE);
					findViewById(R.id.home_join).setVisibility(View.VISIBLE);
				}
			};

			//hide the buttons
			runOnUiThread(r);
			return;
		}

		Runnable r = new Runnable(){
			public void run(){
				loadingGameInfoToast = Toast.makeText(ActivityHome.this, "Loading game information", Toast.LENGTH_SHORT);
				loadingGameInfoToast.show();
			}
		};

		//hide the buttons
		runOnUiThread(r);

		ns.addNodeListener(this);

	}

	public boolean onMessageReceive(JSONObject jo) throws JSONException {

		authLog("got the message");
		authLog(jo.toString());
		if(jo.has("lobbyUpdate") && jo.getBoolean("lobbyUpdate")){
			Runnable r = new Runnable(){
				public void run(){
					if(loadingGameInfoToast != null) {
						loadingGameInfoToast.cancel();
						loadingGameInfoToast.setText("");
					}
					findViewById(R.id.home_host).setVisibility(View.VISIBLE);
					findViewById(R.id.home_join).setVisibility(View.VISIBLE);
				}
			};
			ActivityHome.this.runOnUiThread(r);

			//wont fire again
			return true;
		}else if(jo.has(StateObject.guiUpdate) && jo.has(StateObject.gameStart)){
			Class<?> activ;
			if(jo.getBoolean(StateObject.gameStart))
				activ = ActivityDay.class;
			else {
				activ = ActivityCreateGame.class;
			}
			//ns.removeActivity(this);
			Intent i = new Intent(this, activ);
			startActivity(i);
			return true;
		}
		return false;
	}


	public Narrator getNarrator(){
		return ns.getNarrator();
	}

	protected void onCreate(Bundle b) {
		super.onCreate(b);
		creating(b);
	}

	protected void onStop(){
		if(ns.server != null)
			ns.server.Stop();
		super.onStop();
	}

	private void addClickListeners(int ... ids){
		View v;
		for(int id: ids){
			v = findViewById(id);
			if(!v.hasOnClickListeners())
				v.setOnClickListener(this);
		}
	}

	private void setFont(int ... ids){
		for(int id: ids){
			SetFont(id, this, true);
		}
	}

	private void onEnterGameClick(){
		findViewById(R.id.home_host).setVisibility(View.INVISIBLE);
		findViewById(R.id.home_join).setVisibility(View.INVISIBLE);

		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(5000);
					runOnUiThread(new Runnable() {
						public void run() {
							ActivityHome.this.findViewById(R.id.home_host).setVisibility(View.VISIBLE);
							ActivityHome.this.findViewById(R.id.home_join).setVisibility(View.VISIBLE);
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		toast("Loading Game information");
	}


	public Context passContext(){
		return this;
	}

	private boolean isLoggedIn(){
		return ns.server.IsLoggedIn();
	}

	public static int buildNumber(){
		return Build.VERSION.SDK_INT;
	}

	public synchronized void onClick(View v) {
		int id = v.getId();
		switch(id){

			case R.id.home_host:
				if(isLoggedIn()) {
					onEnterGameClick();
					Server.HostPublic(this);
				}else{
					if(buildNumber() < 16)
						toast("You will not be able to participate in wireless games.");
					showNamePrompt("Host");
				}
				break;

			case R.id.home_join:
				if(isLoggedIn()) {
					Server.JoinPublic(this);
					onEnterGameClick();
				}else if(networkCapable()){
					showIpPrompt();
				}else{
					toast("Your device isn't capable of local hosting.");
				}
				break;

			case R.id.home_roleCard:

				roleCardPopUp = new RoleCardPopUp();
				roleCardPopUp.show(getFragmentManager(), "roleCardPopUp");
				break;

			case R.id.home_tutorial:
				startTutorial();
				break;

			case R.id.home_login_signup:
				if (isLoggedIn()){
					ns.server.LogOut();
					ns.closeWebSocket();
					TextView tv = (TextView) v;
					tv.setText("Login/Signup");
				}else if(isInternetAvailable()){
					LoginAlert loginer = new LoginAlert();
					loginer.show(getFragmentManager(), "logginer");
				}else{
					toast("You must be connected to the internet to login.");
				}
				break;
		}
	}



	public boolean isInternetAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

			if(Build.VERSION.SDK_INT >= 18 && hasPhoneInvite()) {
				PhoneBookPopUp pList = new PhoneBookPopUp();
				pList.setIsHost();
				pList.show(getFragmentManager(), "phoneBookPopup");
				toast("Clicking people and pressing invite will send them texts.");
			}else{
				start();
			}
		}else{
			ns.submitName(name, new SuccessListener(){
				public void onSuccess() {
					np.dismiss();
					start();
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
				/*ns.startClient(ActivityHome.this, ip.getIP(), new SocketClient.ClientListener(){
					public void onHostConnect() {
						ip.dismiss();
						showNamePrompt("Join");
					}
					
				});*/
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
			start();
		}
	}




	

	public static final String HOST_NAME = "host_name";
	private String retrName(){
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		return prefs.getString(HOST_NAME, null);
	}

	public void start(){
		Class<?> activ;
		if(ns.isStarted())
			activ = ActivityDay.class;
		else {
            activ = ActivityCreateGame.class;
        }
		Intent i = new Intent(this, activ);
		startActivity(i);
	}
	
	public void onBackPressed(){
		finish();
		System.exit(0);
	}

	/** register the BroadcastReceiver with the intent values to be matched */

	public void onPause() {
		super.onPause();
	}


	public void onResume(){
		super.onResume();
		addClickListeners(home_buttons);
		connectNarrator(null);
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

	public List<Member> setMembers(){
		List<Member> list = new ArrayList<>();

		list.add(BasicRoles.Armorsmith());
		list.add(BasicRoles.Baker());
		list.add(BasicRoles.Bodyguard());
		list.add(BasicRoles.BusDriver());
		list.add(BasicRoles.Citizen());
		list.add(BasicRoles.Detective());
		list.add(BasicRoles.Doctor());
		list.add(BasicRoles.Escort());
		list.add(BasicRoles.Gunsmith());
		list.add(BasicRoles.Lookout());
		list.add(BasicRoles.Mayor());
		list.add(BasicRoles.Sheriff());
		list.add(BasicRoles.Snitch());
		list.add(BasicRoles.Spy());
		list.add(BasicRoles.Veteran());
		list.add(BasicRoles.Vigilante());

		list.add(BasicRoles.Agent());
		list.add(BasicRoles.Assassin());
		list.add(BasicRoles.Blackmailer());
		list.add(BasicRoles.Coward());
		list.add(BasicRoles.Framer());
		list.add(BasicRoles.Godfather());
		list.add(BasicRoles.Janitor());
		list.add(BasicRoles.Mafioso());

		list.add(BasicRoles.Amnesiac());
		list.add(BasicRoles.Executioner());
		list.add(BasicRoles.Jester());
		list.add(BasicRoles.Survivor());

		list.add(BasicRoles.Cultist());
		list.add(BasicRoles.CultLeader());

		list.add(BasicRoles.Witch());

		list.add(BasicRoles.Arsonist());
		list.add(BasicRoles.MassMurderer());
		list.add(BasicRoles.Poisoner());
		list.add(BasicRoles.SerialKiller());

		return list;
	}


}
