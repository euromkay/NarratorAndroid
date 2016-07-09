package android.setup;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.NActivity;

import shared.event.Message;
import voss.narrator.R;
import android.SuccessListener;
import android.alerts.PlayerPopUp;
import android.parse.Server;
import android.screens.ActivityHome;
import android.screens.ListingAdapter;
import shared.logic.Narrator;
import shared.logic.Team;
import shared.logic.support.Constants;
import shared.logic.support.RoleTemplate;
import shared.roles.Agent;
import shared.roles.Arsonist;
import shared.roles.Blackmailer;
import shared.roles.Bodyguard;
import shared.roles.Citizen;
import shared.roles.CultLeader;
import shared.roles.Cultist;
import shared.roles.Detective;
import shared.roles.Doctor;
import shared.roles.Executioner;
import shared.roles.Framer;
import shared.roles.Godfather;
import shared.roles.Janitor;
import shared.roles.Jester;
import shared.roles.Lookout;
import shared.roles.Mafioso;
import shared.roles.MassMurderer;
import shared.roles.Mayor;
import shared.roles.SerialKiller;
import shared.roles.Sheriff;
import shared.roles.Veteran;
import shared.roles.Vigilante;
import shared.roles.Witch;



public class ActivityCreateGame extends NActivity implements OnItemClickListener, OnClickListener, PlayerPopUp.AddPlayerListener {

	public static final int TOWN = 0;
	public static final int MAFIA = 1;
	public static final int YAKUZA = 2;
	public static final int NEUTRAL = 3;
	public static final int RANDOM = 4;



    private static final String[] neutralPointerList = {Constants.A_CULT,Constants.A_CULT, Constants.A_OUTCASTS, Constants.A_ARSONIST, Constants.A_SK, Constants.A_MM, Constants.A_BENIGN, Constants.A_BENIGN};
    private static final String[] randomsPointerList = {Constants.A_RANDOM, Constants.A_TOWN, Constants.A_TOWN, Constants.A_TOWN, Constants.A_TOWN, Constants.A_MAFIA, Constants.A_YAKUZA, Constants.A_NEUTRAL};

	public ListView cataLV, rolesLV, rolesListLV;
	public TextView playersInGameTV, rolesLeftTV;

	private Button chatButton;

	protected void onCreate(Bundle b){
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_creategame);
		
		setup(b);
	}
	
	protected void onSaveInstanceState(Bundle b){
		super.onSaveInstanceState(b);
		if(manager != null)
			manager.stopTexting();
	}
	
	protected void onResume(){
		super.onResume();
		setup(null);
		cataLV.setSelection(0);
		if(manager != null)
			manager.resumeTexting();
	}
	
	public void onBackPressed() {
		if(Server.IsLoggedIn())
			ns.refresh();
		finish();
	}

	public void onPause(){
		if(manager != null)
			manager.stopTexting();
		super.onPause();
	}
	
	private int[] getColorArray(int ... values){
		int[] array = new int[values.length];

		for(int i = 0; i < values.length; i++){
			array[i] = ParseColor(this, values[i]);
		}
			
		
		return array;
		
	}
	public static final String ID_KEY = "game_id";
	private SetupManager manager;
	private void setup(Bundle b){
		if(manager == null){
			setupRoleCatalogue();
			connectNarrator(new NarratorConnectListener() {
				public void onConnect() {
					setupManager();
				}
			});
			setupCategories();
			findViewById(R.id.roles_show_Players).setOnClickListener(this);
			changeRoleType(TOWN);

			SetFont(R.id.create_info_label, this, false);

			chatET = (EditText) findViewById(R.id.create_chatET);
			chatTV = (TextView) findViewById(R.id.create_chatTV);

			chatET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						sendMessage();
						return true;
					}
					else if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
						sendMessage();
						return true;
					}
					return false;
				}
			});

			chatButton = (Button) findViewById(R.id.create_toChat);
			if(Server.IsLoggedIn()) {
				chatButton.setOnClickListener(this);
				SetFont(R.id.create_toChat, this, false);
			}else
				chatButton.setVisibility(View.GONE);
			SetFont(R.id.create_chatButton, this, false);
		}
	}
	private void setupManager(){
		if(manager != null)
			return;
		manager = new SetupManager(this, ns);


		if(networkCapable())
			manager.setupConnection();

		setupRoleList();

		rolesLeftTV = (TextView) findViewById(R.id.roles_hint_title);


		Button startGameButton = (Button) findViewById(R.id.roles_startGame);
		if(manager.isHost())
			startGameButton.setOnClickListener(this);
		else if (Server.IsLoggedIn()) {
			startGameButton.setOnClickListener(this);
			startGameButton.setText("Exit");
		}else
			startGameButton.setVisibility(View.GONE);

		rolesLV.setOnItemClickListener(this);
		
		setHostCode();

		teamDescription(TOWN);

		findViewById(R.id.create_chatButton).setOnClickListener(this);
		updateChat();
	}

	private void changeFont(int id){
		SetFont(id, this, true);
	}
	public SetupManager getManager(){
		return manager;
	}


	private void setupCategories(){
		cataLV = (ListView) findViewById(R.id.roles_categories_LV);
	
		final String[] CATEGORYTYPES = {"Town", "Mafia", "Yakuza", "Neutral", "Randoms"};
		final String[] categoryColors = new String[]{Constants.A_TOWN, Constants.A_MAFIA, Constants.A_YAKUZA, Constants.A_NEUTRAL, Constants.A_RANDOM};//getColorArray(R.color.town, R.color.mafia, R.color.yakuza, R.color.neutral, R.color.white);
		ListingAdapter adapter = new ListingAdapter(CATEGORYTYPES, this);
		adapter.setColors(categoryColors);
		adapter.setLayoutID(R.layout.create_roles_left_item);

		cataLV.setAdapter(adapter);
		cataLV.setOnItemClickListener(this);

		changeFont(R.id.roles_categories_title);
	}
	private void setupRoleCatalogue(){
		rolesLV = (ListView) findViewById(R.id.roles_bottomLV);
		rolesLV.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		rolesLV.setItemsCanFocus(false);
		changeFont(R.id.roles_bottomLV_title);
	}

	private void setupRoleList(){
		rolesListLV = (ListView) findViewById(R.id.roles_rolesList);
		rolesListLV.setChoiceMode(android.widget.AbsListView.CHOICE_MODE_SINGLE);

		rolesListLV.setOnItemClickListener(this);

		refreshRolesList();
		changeFont(R.id.roles_rightLV_title);
	}

	
	public void refreshRolesList(){
		ArrayList<String> names = new ArrayList<>();
		ArrayList<String> colors = new ArrayList<>();

		for(RoleTemplate r : manager.ns.local.getAllRoles()){
			names.add(r.getName());
			colors.add(r.getColor());
		}

		
		ListingAdapter adapter = new ListingAdapter(names, this);
		adapter.setLayoutID(R.layout.create_roles_right_item);
		adapter.setColors(colors);
		rolesListLV.setAdapter(adapter);

	}

    private void setHostCode(){
		if(manager.isHost() && !Server.IsLoggedIn())
        	rolesLeftTV.setText("Host Code: " + manager.ns.getIp().replace("", "*"));
		else
			rolesLeftTV.setVisibility(View.GONE);
    }



	private int currentCatalogue;
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		RoleTemplate role;
		switch(parent.getId()){
		
		case R.id.roles_categories_LV:
			if(chatVisible())
				switchChat();
			currentCatalogue = position;
			changeRoleType(position);

			teamDescription(currentCatalogue);

			return;
			
		case R.id.roles_bottomLV:
			//position = rolesLV.getCheckedItemPosition();
			if(position != AbsListView.INVALID_POSITION){
				role = getSelectedRole(position);

				if(manager.isHost())
					manager.addRole(role);

				roleDescription(role);
			}
			break;
			
		case R.id.roles_rolesList:
			position = rolesListLV.getCheckedItemPosition();
			if(position != AbsListView.INVALID_POSITION){
				role = manager.ns.local.getAllRoles().get(position);

				if(manager.isHost())
					manager.removeRole(role);

				roleDescription(role);
			}
			break;
		}
		
			
		
 	}
	private void teamDescription(int i){
		Team t = null;
		String color = "#FFFFFF";ParseColor(this, R.color.white);
		String text = "";
		switch (i) {
			case TOWN:
				t = manager.getNarrator().getTeam(Constants.A_TOWN);
				break;
			case MAFIA:
				t = manager.getNarrator().getTeam(Constants.A_MAFIA);
				break;
			case YAKUZA:
				t = manager.getNarrator().getTeam(Constants.A_YAKUZA);
				break;
			case NEUTRAL:
				text = "These roles are the miscellaneous roles without teams.";
				color = Constants.A_NEUTRAL;//ParseColor(this, R.color.neutral);
				break;
			case RANDOM:
				text = "These are random types that will spawn unknown roles.";
				color = Constants.A_RANDOM;//ParseColor(this, R.color.white);
		}

		if (t != null) {
			text = t.getDescription();
			color = t.getColor();
		}

		setDescriptionText(text, convertTeamColor(color));

		if(i == RANDOM)
			manager.screenController.setRoleInfo(SetupScreenController.DAY, color, null);
		else
			manager.screenController.setRoleInfo(SetupScreenController.NONE, Constants.A_NORMAL, null);
	}

	private void roleDescription(RoleTemplate rt){
		setDescriptionText(rt.getName() + ":\n\n" + rt.getDescription(), super.convertTeamColor(rt.getColor()));


		//manager.screenController.setRoleInfo(role, rt.getColor(), null);
	}


	private void setDescriptionText(String text, int color){
		TextView tv = (TextView) findViewById(R.id.create_info_label);
		tv.setText(text);
		tv.setTextColor(color);

	}
	
	private void changeRoleType(int position){
		String[] rolesList;
		String[] colors;
		
		switch(position){
		case TOWN:
			rolesList = getResources().getStringArray(R.array.roles_townRoles);
			colors = fillArray(Constants.A_TOWN, rolesList.length);
			break;
			
		case MAFIA:
			rolesList = getResources().getStringArray(R.array.roles_mafiaRoles);
			colors = fillArray(Constants.A_MAFIA, rolesList.length);
			break;

        case YAKUZA:
            rolesList = getResources().getStringArray(R.array.roles_mafiaRoles);
            colors = fillArray(Constants.A_YAKUZA, rolesList.length);
            break;
			
		case NEUTRAL:
			rolesList = getResources().getStringArray(R.array.roles_neutralRoles);
			colors = neutralPointerList;
			break;
			
		case RANDOM:
			rolesList = getResources().getStringArray(R.array.roles_randomRoles);
			colors = randomsPointerList;
			break;
			
		default:
			throw new IndexOutOfBoundsException();
		}

		ListingAdapter ada = new ListingAdapter(rolesList, this);
		ada.setColors(colors);
		ada.setLayoutID(R.layout.create_roles_left_item);
		rolesLV.setAdapter(ada);
		
	}
	private String[] fillArray(String color, int length){
		String[] array = new String[length];
		
		//int color = readColorPointer(colorPointer);
		for(int j = 0; j < length; j++)
			array[j] = color;
		
		return array;
	}

    private int readColorPointer(int colorPointer){
        return ParseColor(this, colorPointer);
    }

    private int[] fillIntArray(int[] colorPointers){
        int[] colors = new int[colorPointers.length];
        for (int i = 0; i < colorPointers.length; i++)
            colors[i] = readColorPointer(colorPointers[i]);
        return colors;
    }

	private EditText chatET;
	private TextView chatTV;
	private void sendMessage(){
		if(!Server.IsLoggedIn())
			return;

		String message = chatET.getText().toString();
		if(chatET.length() == 0)
			return;

		chatET.setText("");
		manager.talk(message);
	}

	protected void updateChat(){
		if(!Server.IsLoggedIn())
			return;


		String events = ns.local.getEventManager().getEvents(Message.PUBLIC).access(Message.PUBLIC, true);
		events = events.replace("\n", "<br>");
		chatTV.setText(Html.fromHtml(events));
		pushChatDown();
	}

	private boolean chatVisible(){
		return rolesLV.getVisibility() == View.GONE;
	}

	private void switchChat(){
		int mode1, mode2;
		if(chatVisible()){
			mode1 = View.VISIBLE;
			mode2 = View.GONE;
			chatButton.setText("View Chat");
		}else{
			mode1 = View.GONE;
			mode2 = View.VISIBLE;
			chatButton.setText("View Roles");
		}
		rolesLV.setVisibility(mode1);
		findViewById(R.id.roles_bottomLV_title).setVisibility(mode1);
		findViewById(R.id.create_info_wrapper).setVisibility(mode1);

		chatET.setVisibility(mode2);
		findViewById(R.id.create_chatHolder).setVisibility(mode2);
		findViewById(R.id.create_chatButton).setVisibility(mode2);



	}
	
	public void onClick(View v) {
		switch(v.getId()) {

			case R.id.create_toChat:
				switchChat();
				return;

			case R.id.create_chatButton:
				sendMessage();
				return;

            case R.id.roles_startGame:
				if (Server.IsLoggedIn()){
					if(manager.isHost()){
						if(!manager.checkNarrator())
							return;
						Server.StartGame(ns.local, ns.getGameListing(), new SuccessListener() {
							public void onSuccess() {
								toast("Starting game");
							}

							public void onFailure(String message) {
								toast("Game start failed.");
								Log.e("CreateGame st fail", message);
							}
						});
					} else
						manager.exitGame();
				}else if (manager.isHost())
					manager.startGame(manager.getNarrator().getSeed());
				break;

            case R.id.roles_show_Players:
                showPlayerList();
				break;
        }

	}

	public void pushChatDown() {
		final ScrollView chatLV = (ScrollView) findViewById(R.id.create_chatHolder);
		chatLV.post(new Runnable() {
			public void run() {
				chatLV.fullScroll(View.FOCUS_DOWN);
			}
		});
		chatET.post(new Runnable() {
			public void run() {
				chatET.requestFocusFromTouch();
				InputMethodManager lManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				lManager.showSoftInput(chatET, 0);
			}
		});
	}

    public Toast toast(String message){
        Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
		t.show();
		return t;
    }

	private void showPlayerList(){
		new PlayerPopUp().show(getFragmentManager(), "playerlist");
	}

	public void onPopUpDismiss(){
		
	}

	public void onPopUpCreate(PlayerPopUp p){
		
	}

	public Narrator getNarrator(){
		if(manager == null)
			manager.toString();
		return manager.ns.local;
	}

	private RoleTemplate getSelectedRole(int position){
		String[] rolesList = null;
		switch (currentCatalogue){
			case TOWN:
				rolesList = getResources().getStringArray(R.array.roles_townRoles);
				break;
			case MAFIA:
				rolesList = getResources().getStringArray(R.array.roles_mafiaRoles);
				break;
			case YAKUZA:
				rolesList = getResources().getStringArray(R.array.roles_mafiaRoles);
				break;
			case NEUTRAL:
				rolesList = getResources().getStringArray(R.array.roles_neutralRoles);
				break;
			case RANDOM:
				rolesList = getResources().getStringArray(R.array.roles_randomRoles);
				break;
		}
		
		String s = rolesList[position];

		return null;
	}





	public void onDestroy(){
		if(ns!=null)
			ns.removeSetupManager();
		this.unbindNarrator();
		super.onDestroy();
	}

}

