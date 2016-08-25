package android.setup;

import java.util.ArrayList;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

import android.NActivity;
import android.alerts.PlayerPopUp;
import android.alerts.TeamBuilder;
import android.alerts.TeamEditor;
import android.content.Context;
import android.os.Bundle;
import android.screens.ListingAdapter;
import android.text.Html;
import android.texting.StateObject;
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
import shared.logic.Narrator;
import voss.narrator.R;


public class ActivityCreateGame extends NActivity implements OnItemClickListener, OnClickListener, PlayerPopUp.AddPlayerListener {

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
		//cataLV.setSelection(0);
		if(manager != null)
			manager.resumeTexting();
	}
	
	public void onBackPressed() {
		if(server.IsLoggedIn())
			ns.refresh();
		finish();
	}

	public void onPause(){
		if(manager != null)
			manager.stopTexting();
		super.onPause();
	}
	

	public static final String ID_KEY = "game_id";
	private SetupManager manager;
	private Object managerLock = new Object();
	private void setup(Bundle b){
		SetFont(R.id.create_info_label, this, false);
		
		chatButton = (Button) findViewById(R.id.create_toChat);
		chatButton = (Button) findViewById(R.id.create_toChat);
		SetFont(R.id.create_chatButton, this, false);
		
		chatET = (EditText) findViewById(R.id.create_chatET);
		chatTV = (TextView) findViewById(R.id.create_chatTV);
		findViewById(R.id.roles_show_Players).setOnClickListener(this);
		
		findViewById(R.id.create_createTeamButton).setOnClickListener(this);
		SetFont(R.id.create_createTeamButton, this, false);
		
		findViewById(R.id.create_deleteTeamButton).setOnClickListener(this);
		SetFont(R.id.create_deleteTeamButton, this, false);
		
		findViewById(R.id.create_editAlliesButton).setOnClickListener(this);
		SetFont(R.id.create_editAlliesButton, this, false);
		
		findViewById(R.id.create_editMembersButton).setOnClickListener(this);
		SetFont(R.id.create_editMembersButton, this, false);
		
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
		synchronized (managerLock){
			if(manager == null){
				if(ns == null){
					connectNarrator(new NarratorConnectListener() {
						public void onConnect() {
							try {
								manager = new SetupManager(ActivityCreateGame.this, ns);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				} else
					try {
						manager = new SetupManager(this, ns);
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
			
		}
		/*if(manager == null){



			

			
		}*/
	}
	public void onConnect(SetupManager sm) throws JSONException{
		manager = sm;

		refreshAvailableFactions();
		setupRoleCatalogue();
		setupRoleList();

		rolesLeftTV = (TextView) findViewById(R.id.roles_hint_title);


		Button startGameButton = (Button) findViewById(R.id.roles_startGame);
		if(manager.isHost())
			startGameButton.setOnClickListener(this);
		else if (server.IsLoggedIn()) {
			startGameButton.setOnClickListener(this);
			startGameButton.setText("Exit");
		}else
			startGameButton.setVisibility(View.GONE);

		if(server.IsLoggedIn()) {
			chatButton.setOnClickListener(this);
			SetFont(R.id.create_toChat, this, false);
		}else
			chatButton.setVisibility(View.GONE);
		
		rolesLV.setOnItemClickListener(this);
		
		setHostCode();

		refreshDescription();

		findViewById(R.id.create_chatButton).setOnClickListener(this);
		updateChat();
	}

	private void changeFont(int id){
		SetFont(id, this, true);
	}
	public SetupManager getManager(){
		return manager;
	}


	public JSONObject activeFaction;
	public void refreshAvailableFactions() throws JSONException {
		JSONObject allFactions = manager.ns.getFactions();
		if(allFactions == null)//not initialized yet
			return;
		
		
		cataLV = (ListView) findViewById(R.id.roles_categories_LV);
	
		ArrayList<String> data = new ArrayList<>();
		ArrayList<String> cData = new ArrayList<>();
			
		JSONObject faction;
		JSONArray factionNames = allFactions.getJSONArray(StateObject.factionNames);
		String factionName;
		for(int i = 0; i < factionNames.length(); i++) {
			factionName = factionNames.getString(i);
			faction = allFactions.getJSONObject(factionName);
			data.add(faction.getString("name"));
			cData.add(faction.getString("color"));
		}

		ListingAdapter adapter = new ListingAdapter(data, this);
		adapter.setColors(cData);
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

		JSONArray roles = ns.getRoles();
		try{
			JSONObject r;
			for(int i = 0; i < roles.length(); i++){
				r = roles.getJSONObject(i);
				
			//for(RoleTemplate r : manager.ns.local.getAllRoles()){
				names.add(r.getString(StateObject.roleType));
				colors.add(r.getString(StateObject.color));
			}
		}catch(JSONException e){
			e.printStackTrace();
		}

		
		ListingAdapter adapter = new ListingAdapter(names, this);
		adapter.setLayoutID(R.layout.create_roles_right_item);
		adapter.setColors(colors);
		rolesListLV.setAdapter(adapter);

	}

    private void setHostCode(){
		if(server.IsLoggedIn()) {
			//rolesLeftTV.setText("Host Code: " + manager.ns.getIp().replace("", "*"));
		}else
			rolesLeftTV.setVisibility(View.GONE);
    }

    public void resetView(){
    	try{
    		JSONObject allFactions = manager.ns.getFactions();
    		if(activeFaction != null){
    			String factionName = activeFaction.getString("name");
    			JSONObject oldFaction = activeFaction;
    			if(allFactions.has(factionName)){
    				activeFaction = allFactions.getJSONObject(factionName);
    		
	    			if(oldFaction == activeRule)
	    				activeRule = activeFaction;
	    			else{
	    				activeRule = activeFaction.getJSONArray("members").getJSONObject(lastClicked);
	    			}
    			}else{
    				activeFaction = activeRule = null;
    			}
    		}
    		
    		refreshAvailableFactions();
    		refreshAvailableRolesList();
    		refreshDescription();

			if(tEditor != null)
				tEditor.refresh();
    		
    	}catch(JSONException e){
    		e.printStackTrace();
    	}
    }
    
    private int lastClicked;
	public void onItemClick(AdapterView<?> parent, View unused, int position, long id) {
		switch(parent.getId()){
		
		case R.id.roles_categories_LV:
			if(chatVisible())
				switchChat();
			try {
				JSONObject allFactions = manager.ns.getFactions(); 
				String factionName = allFactions.getJSONArray(StateObject.factionNames).getString(position);
				activeFaction = allFactions.getJSONObject(factionName);
				activeRule = activeFaction;
				
				refreshAvailableRolesList();

				refreshDescription();
			}catch(JSONException e){
				e.printStackTrace();
			}
			return;
			
		case R.id.roles_bottomLV:
			if(position != AbsListView.INVALID_POSITION){
				if(activeFaction == null)
					return;
				try{
					lastClicked = position;
					activeRule = activeFaction.getJSONArray("members").getJSONObject(position);
					if(manager.isHost()){
						ns.addRole(activeRule.getString("name"), activeRule.getString("color"));
					}
					manager.screenController.setRoleInfo(activeRule);
				}catch(JSONException ef){
					ef.printStackTrace();
				}
			}
			break;
			
		case R.id.roles_rolesList:
			if(!manager.isHost())
				return;
			position = rolesListLV.getCheckedItemPosition();
			if(position != AbsListView.INVALID_POSITION){
				TextView tv = (TextView) unused;

				String color = String.format("#%06X", (0xFFFFFF & tv.getCurrentTextColor()));
				manager.removeRole(tv.getText().toString(), color);
				activeRule = null;
				resetView();

				//roleDescription(role);
			}
			break;
		}
		
			
		
 	}
	public JSONObject activeRule = null;
	public void refreshDescription() throws JSONException{
		manager.screenController.setRoleInfo(activeRule);
	}


	public void setDescriptionText(String name, String description, String color){
		TextView tvName = (TextView) findViewById(R.id.create_info_label);
		tvName.setText(name);
		NActivity.setTextColor(tvName, color);
		tvName.setVisibility(View.VISIBLE);
		
		TextView tvDescrip = (TextView) findViewById(R.id.create_info_description);
		tvDescrip.setText(description);
		tvDescrip.setVisibility(View.VISIBLE);
	}
	
	

	public void refreshAvailableRolesList() throws JSONException{
		ArrayList<String> rolesList = new ArrayList<>(), colors = new ArrayList<>();
		JSONObject faction = activeFaction;
		if(faction == null){
			rolesLV.setVisibility(View.GONE);
			return;
		}else{
			rolesLV.setVisibility(View.VISIBLE);
		}
			
		JSONArray members = faction.getJSONArray("members");
		if(members.length() == 0){
			rolesLV.setVisibility(View.GONE);
			return;
		}

		
		JSONObject member;
		for(int i = 0; i < members.length(); i++){
			member = members.getJSONObject(i);
			rolesList.add(member.getString("name"));
			colors.add(member.getString("color"));
		}
		
		ListingAdapter ada = new ListingAdapter(rolesList, this);
		ada.setColors(colors);
		ada.setLayoutID(R.layout.create_roles_left_item);
		rolesLV.setAdapter(ada);


	}

	private EditText chatET;
	private TextView chatTV;
	private void sendMessage(){
		if(!server.IsLoggedIn())
			return;

		String message = chatET.getText().toString();
		if(chatET.length() == 0)
			return;

		chatET.setText("");
		manager.talk(message);
	}

	public void updateChat(){
		if(!server.IsLoggedIn())
			return;

		String events = ns.getChat();
		events = events.replace("\n", "<br>");
		chatTV.setText(Html.fromHtml(events));
		pushChatDown();
	}

	public boolean chatVisible(){
		if(activeFaction == null)
			return chatET.getVisibility() != View.GONE;
		return rolesLV.getVisibility() == View.GONE;
	}

	private void switchChat(){
		int mode1, mode2;
		if(chatVisible()){ //it's not going to be visible anymore
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

		if(!chatVisible() && activeFaction == null){
			rolesLV.setVisibility(View.GONE);
			findViewById(R.id.create_info_wrapper).setVisibility(View.GONE);
		}

	}

	public static final String EDIT_TEAM_PROMPT = "editTeam";
	public static final int REGULAR = 0;
	public static final int EDITING_ALLIES = 1;
	public static final int EDITING_ROLES = 2;
	public int mode = REGULAR;

	public TeamEditor tEditor;

	public void onClick(View v) {
		switch(v.getId()) {

			case R.id.create_toChat:
				switchChat();
				return;

			case R.id.create_chatButton:
				sendMessage();
				return;

			case R.id.create_createTeamButton:
				openCreateTeamDialog();

				return;
				
			case R.id.create_deleteTeamButton:
				try {
					String color = activeFaction.getString("color");
					ns.deleteTeam(color);
					refreshRolesList();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return;
				
			case R.id.create_editAlliesButton:
				mode = EDITING_ALLIES;
				tEditor = new TeamEditor();
				tEditor.show(getFragmentManager(), EDIT_TEAM_PROMPT);
				return;
				
			case R.id.create_editMembersButton:
				mode = EDITING_ROLES;
				tEditor = new TeamEditor();
				tEditor.show(getFragmentManager(), EDIT_TEAM_PROMPT);
				return;
			
            case R.id.roles_startGame:
				if (server.IsLoggedIn()){
					JSONObject jo = new JSONObject();
					if(ns.isHost()){
						ns.put(jo, StateObject.message, StateObject.startGame);
					}else{
						ns.put(jo, StateObject.message, StateObject.leaveGame);
					}
					ns.sendMessage(jo);
					
					if(!ns.isHost()){
						if(manager != null)
							manager.stopTexting();
						unbindNarrator();
						finish();
					}
				}else if (manager.isHost())
					manager.startGame(manager.getNarrator().getSeed());
				break;

            case R.id.roles_show_Players:
                showPlayerList();
				break;
        }
	}
	
	

	public void pushChatDown() {
		boolean hasFocus = chatET.hasFocus();
		final ScrollView chatLV = (ScrollView) findViewById(R.id.create_chatHolder);
		chatLV.post(new Runnable() {
			public void run() {
				chatLV.fullScroll(View.FOCUS_DOWN);
			}
		});
		if(hasFocus)
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

    public static final String PLAYER_POP_UP = "playerlist";
    public PlayerPopUp pPop = null;
	private void showPlayerList(){
		pPop = new PlayerPopUp();
		pPop.show(getFragmentManager(), PLAYER_POP_UP);
	}
	
	public void openCreateTeamDialog(){
		new TeamBuilder().show(getFragmentManager(), "newTeam");
	}

	public void onPopUpDismiss(){
		pPop = null;
	}

	public void onPopUpCreate(PlayerPopUp p){
		
	}

	public Narrator getNarrator(){
		if(manager == null)
			manager.toString();
		return manager.ns.local;
	}





	public void onDestroy(){
		if(ns!=null)
			ns.removeSetupManager();
		this.unbindNarrator();
		super.onDestroy();
	}

	

}

