package voss.android.screens;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

import voss.android.ActivityTutorial;
import voss.android.CommunicatorPhone;
import voss.android.R;
import voss.android.alerts.IpPrompt;
import voss.android.alerts.IpPrompt.IpPromptListener;
import voss.android.alerts.NamePrompt;
import voss.android.alerts.NamePrompt.NamePromptListener;
import voss.android.alerts.PhoneBookPopUp;
import voss.android.alerts.PhoneBookPopUp.AddPhoneListener;
import voss.android.setup.ActivityCreateGame;
import voss.android.texting.CommunicatorText;
import voss.android.texting.PhoneNumber;
import voss.android.wifi.WifiHost;
import voss.logic.Narrator;
import voss.logic.Player;
import voss.logic.exceptions.UnsupportedMethodException;
import voss.packaging.Board;

public class ActivityHome extends Activity implements OnClickListener, IpPromptListener, NamePromptListener, AddPhoneListener {
	
	public void creating(Bundle b){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
		
		setText(R.id.home_join);
		setText(R.id.home_host);
		setText(R.id.home_login);
		setText(R.id.home_tutorial);
		setText(R.id.home_settings);

		if(b == null)
			n = Board.getNarrator(getIntent().getParcelableExtra(Narrator.KEY));
		else
			n = Board.getNarrator(b.getParcelable(Narrator.KEY));
		if(n == null)
			n = Narrator.Default();

		ParseUser p = ParseUser.getCurrentUser();
		if(p != null){
			findViewById(R.id.home_login).setVisibility(View.INVISIBLE);
		}else{
			
		}
		
	}

	public Narrator getNarrator(){
		throw new UnsupportedMethodException();
	}

	private Narrator n;
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		creating(b);
	}

	private void setText(int id){
		TextView text = (TextView) findViewById(id);
		Typeface font = Typeface.createFromAsset(getAssets(), "AbrilFatface-Regular.ttf");
		text.setTypeface(font);
		text.setOnClickListener(this);
	}

	


	public Context passContext(){
		return this;
	}

	private boolean isLoggedIn(){
		return false;
	}

	public static int buildNumber(){
		return Build.VERSION.SDK_INT;
	}

	public void onClick(View v) {
		switch(v.getId()){

			case R.id.home_host:
				if(isLoggedIn())
					startNewGame(null);
				else{
					if(buildNumber() < 16)
						toast("You will not be able to host games wirelessly");
					showNamePrompt("Host");
				}
				break;

			case R.id.home_join:
				if(isLoggedIn())
					startNewGame(null);
				else{
					showNamePrompt("Join");
				}
				break;

			//wins losses
			case R.id.home_tutorial:
				startTutorial();
				break;
			
		}
	}

	private void startTutorial(){
		Intent i = new Intent(this, ActivityTutorial.class);
		i.putExtra(Narrator.KEY, Board.GetParcel(n));
		startActivity(i);
		finish();
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

	private void showIpPrompt(){
		DialogFragment pList = new IpPrompt();
		pList.show(getFragmentManager(), "ipprompt");
	}

	public void onNamePromptConfirm(NamePrompt np, String name, boolean isHost){
		SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
		prefs.putString(HOST_NAME, name);
		n.removeAllPlayers();
		if(isHost)
			n.addPlayer(name).setCommunicator(new CommunicatorPhone());
		prefs.commit();
		np.dismiss();

		if( Build.VERSION.SDK_INT >= 18) {
			if (isHost) {
				PhoneBookPopUp pList = new PhoneBookPopUp();
				pList.setIsHost();
				pList.show(getFragmentManager(), "phoneBookPopup");
			} else {
				showIpPrompt();
			}
		}
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
		public void onPostExecute(IpPrompt ip){
			boolean b = ip == null;
			if (b){
				toast("wrong code, try again");
			}else{
				WifiHost.StartConnection(ActivityHome.this, ip.getIP(), retrName());
				ip.dismiss();
				startNewGame(ip.getIP());
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
				number = contacts.get(name);
				//ReceiverText.sendText(number, "You have been invited to the Narrator.  You are " + name + ". If that's not who you are, tell me your name.");
				CommunicatorText cp = new CommunicatorText(number);
				n.addPlayer(cp).setName(name);
			}
			popup.dismiss();
			startNewGame(null);
		}
	}




	

	public static final String HOST_NAME = "host_name";
	private String retrName(){
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		return prefs.getString(HOST_NAME, null);
	}

	private void toast(String s){
		Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
	}



	public static final String ISHOST = "ishost_activityhome";
	public static final String MYNAME = "myname_activityhoome";
	private void startNewGame(String ip){
		Intent i = new Intent(this, ActivityCreateGame.class);
		i.putExtra(ISHOST, ip == null);
		i.putExtra(MYNAME, retrName());
		n.removeAllRoles();
		i.putExtra(Narrator.KEY, Board.GetParcel(n));
		if(ip != null)
			i.putExtra(ActivityCreateGame.IP_KEY, ip);
		startActivity(i);
		finish();
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
}
