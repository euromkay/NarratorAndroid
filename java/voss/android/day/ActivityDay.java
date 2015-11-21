package voss.android.day;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import voss.android.R;
import voss.android.alerts.ExitGameAlert;
import voss.android.alerts.ExitGameAlert.ExitGameListener;
import voss.android.day.PlayerDrawerAdapter.OnPlayerClickListener;
import voss.android.screens.ActivityHome;
import voss.android.screens.ListingAdapter;
import voss.android.screens.MembersAdapter;
import voss.android.screens.SimpleGestureFilter;
import voss.android.screens.SimpleGestureFilter.SimpleGestureListener;
import voss.android.texting.PhoneNumber;
import voss.android.texting.TextHandler;
import voss.logic.Narrator;
import voss.logic.Player;
import voss.logic.PlayerList;
import voss.logic.Team;
import voss.logic.Event;
import voss.logic.exceptions.PlayerTargetingException;
import voss.logic.support.Constants;
import voss.packaging.Board;
import voss.roles.Framer;


public class ActivityDay extends FragmentActivity 
implements 
	ExitGameListener, 
	OnClickListener, 
	OnInitListener, 
	OnItemClickListener, 
	OnItemSelectedListener, 
	OnPlayerClickListener, 
	DrawerListener, 
	SimpleGestureListener {

	public DayManager manager;
	protected TextHandler tHandler;

	private IntentFilter iF;
	private TextToSpeech speaker;
	protected ListView rolesLV, membersLV, actionLV, alliesLV;
	public TextView leftTV, rightTV, roleInfoTV, commandsTV, skipTV, chatTV, playerLabelTV;
	public Spinner framerSpinner;
	private ScrollView chatLV;
	public EditText chatET;
	public Button button, chatButton, messagesButton, actionButton, infoButton;

	private DrawerLayout dayWindow;
	public RecyclerView playerMenu;
	
	private SimpleGestureFilter detector;

	private static final boolean HTML = true;

	protected void onCreate(Bundle b){
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_day);


		setup(b);

		iF = new IntentFilter();
		iF.addAction("SMS_RECEIVED_ACTION");
	}
	
	protected void onResume(){
		super.onResume();
		setup(null);
		registerReceiver(intentReceiver, iF);
	}

	protected void onSaveInstanceState(Bundle b){
		super.onSaveInstanceState(b);
		if (b != null)
			b.putParcelable(Narrator.KEY, Board.GetParcel(manager.getNarrator()));
	}
	public void onBackPressed(){
		if (!manager.getNarrator().isInProgress()){
			stopTexting();
			finish();
		}
		if (drawerOut){
			closeDrawer();
		}else {
			onPlayerClick(null);
			onDrawerClosed(null);
			onClick(infoButton);
		}
	}
	
	
	private void setup(Bundle b){
		if (manager != null)
			return;

		Narrator n;
		boolean isHost;
		if(b == null) {
			n = Board.getNarrator(getIntent().getParcelableExtra(Narrator.KEY));
			isHost = getIntent().getBooleanExtra(ActivityHome.ISHOST, false);
		}else {
			n = Board.getNarrator(b.getParcelable(Narrator.KEY));
			isHost = b.getBoolean(ActivityHome.ISHOST, false);
		}
		playerMenu = (RecyclerView) findViewById(R.id.day_playerNavigationPane);


		dayWindow = (DrawerLayout) findViewById(R.id.day_main);
		dayWindow.setDrawerListener(this);
		dayWindow.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		chatLV         = (ScrollView) findViewById(R.id.day_chatHolder);
		chatET         = (EditText) findViewById(R.id.day_chatET);

		commandsTV     = (TextView) findViewById(R.id.day_commandsLabel);
		skipTV         = commandsTV;

		leftTV         = (TextView) findViewById(R.id.day_rolesList_label);
		rightTV        = (TextView) findViewById(R.id.day_membersLabel);
		roleInfoTV     = (TextView) findViewById(R.id.day_role_info);

		chatTV         = (TextView) findViewById(R.id.day_chatTV);
		playerLabelTV  = (TextView) findViewById(R.id.day_currentPlayerTV);

		membersLV      = (ListView) findViewById(R.id.day_membersLV);
		actionLV       = (ListView) findViewById(R.id.day_actionList);
		rolesLV        = (ListView) findViewById(R.id.day_rolesList);
		alliesLV       = rolesLV;
		button         = findButton(R.id.day_button);
		messagesButton = findButton(R.id.day_messagesButton);
		infoButton     = findButton(R.id.day_infoButton);
		actionButton   = findButton(R.id.day_actionButton);
		chatButton     = (Button) findViewById(R.id.day_chatButton);

		framerSpinner  = (Spinner) findViewById(R.id.day_frameSpinner);
		framerSpinner.setOnItemSelectedListener(this);


		detector = new SimpleGestureFilter(this, this);

		button.setOnClickListener(this);
		chatButton.setOnClickListener(this);

		addOnClickListener(R.id.day_actionButton);
		addOnClickListener(R.id.day_messagesButton);
		addOnClickListener(R.id.day_infoButton);
		addOnClickListener(R.id.day_playerDrawerButton);
		addOnClickListener(R.id.day_chatET);


		speaker = new TextToSpeech(this, this);
		speaker.setLanguage(Locale.UK);
		speaker.setSpeechRate(0.9f);

		manager = new DayManager(this, n);
		manager.startIpListener(isHost);

		manager.initiate();
		onClick(infoButton);

	}
	public Narrator getNarrator(){
		return manager.getNarrator();
	}
	private Button findButton(int id){
		return (Button) findViewById(id);
	}
	private void addOnClickListener(int id){
		findViewById(id).setOnClickListener(this);

	}
	protected void setupPlayerDrawer(PlayerList livePlayers){
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		playerMenu.setLayoutManager(layoutManager);
		playerMenu.setAdapter(new PlayerDrawerAdapter(livePlayers, this));
	}
	public ArrayList<String> frameOptions;
	public void setupFramerSpinner(){
		frameOptions = new ArrayList<>();
		ArrayList<Integer> teamColors = new ArrayList<>();

		for (Team t: manager.getNarrator().getAllTeams()){
			if(t.getAlignment() == Constants.A_SKIP)
				continue;
			teamColors.add(t.getAlignment());
			frameOptions.add(t.getName());
		}

		ListingAdapter adapter = new ListingAdapter(frameOptions, teamColors, this);
		adapter.setLayoutID(R.layout.day_player_player_dropdown_item);
		framerSpinner.setAdapter(adapter);
	}

	public void log(String i){
		Log.d("ActivityDay", i);
	}

	public int getSpinnerSelectedID(){
		String name = (String) framerSpinner.getSelectedItem();
		Team found = null;
		for (Team t: manager.getNarrator().getAllTeams()){
			if (t.getName().equals(name)) {
				found = t;
				break;
			}
		}

		if (found == null)
			throw new NullPointerException(name + " wasn't found");
		return found.getAlignment();
	}
	protected void setButtonText(String s){
		if(button == null || s == null)
			throw new NullPointerException("This is null??");
		button.setText(s);
	}
	protected String getSelectedAbility(){
		return commandsTV.getText().toString();
	}
	protected void setDayLabel(boolean day, int dayNumber){
		String s;
		if(day)
			s = "Day";
		else
			s = "Night";
		s += " " + dayNumber;
		((TextView) findViewById(R.id.day_title)).setText(s);
	}


	public void onInit(int status) {
		
	}
	protected void say(String s) {
		speaker.speak(s, TextToSpeech.QUEUE_ADD, null);
	}


	protected void setCommand(String command){
		commandsTV.setText(command);
	}

	protected void updateMembers() {
		rightTV.setText("Players of Society");
		rightTV.setTextColor(getResources().getColor(R.color.trimmings));
		membersLV.setAdapter(new MembersAdapter(manager.getNarrator().getAllPlayers(), this));
		hideView(roleInfoTV);
	}

	protected void uncheck(Player p){
		if (p != null)
			actionLV.setItemChecked(actionList.indexOf(p), false);

	}
	protected void check(Player p){
		if (p != null)
			actionLV.setItemChecked(actionList.indexOf(p), true);

	}
	protected void hideDayButton(){
		hideView(button);
	}

	
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		if (!manager.playerSelected() || (manager.getNarrator().isNight() && manager.getCurrentPlayer().endedNight())) {
			manager.updateActionPanel();
			return;
		}
		try {
			log(manager.getCurrentPlayer().getDescription() + " chose (" + commandsTV.getText().toString() + ") for " + actionList.get(position).getDescription());
		}catch (ArrayIndexOutOfBoundsException e){

			log("accessing out of bounds again");
		}

		manager.command(actionList.get(position));
			
	}
	
	public BroadcastReceiver intentReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			String message = intent.getExtras().getString("message");
			PhoneNumber number = new PhoneNumber(intent.getExtras().getString("number"));
			Player owner = manager.phoneBook.getByNumber(number);
			
			if(owner == null){
				Toast.makeText(ActivityDay.this, "received text message", Toast.LENGTH_LONG).show();
				return;
			}
			
			try{
				tHandler.text(owner, message);
			}catch(Exception|Error e){
				e.printStackTrace();
				if(owner != null)
					owner.sendMessage(e.getMessage());
			}
				
			
		}
	};
	
	
	
	
	


	
	public PlayerList actionList;
	protected void setActionList(PlayerList playerList, boolean day){
		actionList = playerList;
		List<String> targetables = new ArrayList<>();
		for(Player aP: playerList){
			if(day)
				targetables.add(manager.getNarrator().getVoteCountOf(aP) + " - " + aP.getName());
			else
				targetables.add(aP.getName());
		}

		
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.vote_item, targetables);
		adapter.setDropDownViewResource(R.layout.create_roles_right_item);
	
		actionLV.setAdapter(adapter);
		actionLV.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

		actionLV.setOnItemClickListener(this);
	}



	public View panel = null;
	public void onClick(View v){
		switch(v.getId()){
			case R.id.day_chatET:
				pushChatDown();
				break;

			case R.id.day_button:
				log("Big button clicked");
				try{
					manager.buttonClick();
				}catch(PlayerTargetingException e){
					e.printStackTrace();
					toast(e.getMessage());
				}
				break;

			case R.id.day_playerDrawerButton:
				log("Player drawer activated");
				dayWindow.openDrawer(playerMenu);
				break;

			case R.id.day_messagesButton:
				log("Message button clicked.");

				panel = v;
				hideActionPanel();
				hideInfoPanel();
				showMessagesPanel();
				break;

			case R.id.day_infoButton:
				log("Info button clicked.");

				panel = v;
				hideActionPanel();
				hideMessagePanel();
				showInfoPanel();
				break;

			case R.id.day_chatButton:
				log("Send Message button clicked.");
				sendMessage();
				break;

			case R.id.day_actionButton:
				log("Actions button clicked.");

				panel = v;
				hideMessagePanel();
				hideInfoPanel();
				showActionPanel();
		}
	}
	
	public void toast(String s){
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	public void sendMessage(){
		String message = chatET.getText().toString();
		if (message.length() == 0)
			return;

		if(manager.getCurrentPlayer() == null){
			if(message.startsWith(TextHandler.MODKILL + " ")){
				String name = message.substring(TextHandler.MODKILL.length() + 1);
				Player baddy = manager.phoneBook.getByName(name);
				if(baddy != null)
					baddy.modkill();
			}
				
		}else
			manager.talk(manager.getCurrentPlayer(), message);
		chatET.setText("");
	}

	public void updateChatPanel(){
		Player p = manager.getCurrentPlayer();
		String text;
		if (!manager.playerSelected())
			text = manager.getNarrator().getEvents(Event.PUBLIC, HTML);
		else
			text = p.getNarrator().getEvents(p.getID(), true);
		setChatPanelText(text);
		pushChatDown();
	}

	private void setChatPanelText(String text){
		chatTV.setText(Html.fromHtml(text.replace("\n", "<br>")));
	}

	public void hideMessagePanel(){
		chatET.setText("");
		hideView(chatLV);
		hideView(chatButton);
		hideView(chatET);
	}

	public void showInfoPanel(){
		showView(leftTV);
		showView(rolesLV);

		showView(rightTV);
		if (manager.playerSelected()){
			if (manager.getCurrentPlayer().hasDayAction() && manager.isDay())
				showView(button);
			else
				hideView(button);

			showView(roleInfoTV);
			hideView(membersLV);
		}else{
			showView(membersLV);
			hideView(roleInfoTV);
			hideView(button);
		}
	}

	public void onPlayerClick(Player p){
		String name;
		if (p == null || p.equals(p.getSkipper()))
			name = "Main Screen selected.";
		else
			name = p.getDescription();
		log(name + " taking the helm.");
		manager.setCurrentPlayer(p);
	}

	public void onExitAttempt(){
		DialogFragment newFragment = new ExitGameAlert();
		newFragment.show(getFragmentManager(), "missiles");
	}

	public void onExitGame(){
		stopTexting();
		speaker.shutdown();
		finish();
	}

	public int getMyColor(int id){
		return getResources().getColor(id);
	}

	public void setActionButton(){
		if(manager.isDay()){
			actionButton.setText("Voting");
		}else{
			actionButton.setText("Night Actions");
		}
	}

	public void hideActionPanel(){
		((RelativeLayout.LayoutParams)actionLV.getLayoutParams()).addRule(RelativeLayout.BELOW, commandsTV.getId());
		hideView(actionLV);
		hideView(commandsTV);
		hideView(button);
		hideView(framerSpinner);
	}

	public void showActionPanel() {
		showView(actionLV);
		showView(commandsTV);

		if (manager.playerSelected()){
			if (!manager.isDay()){
				showView(button);
				showFrameSpinner();
			}
		}else{
			hideView(button);
		}
	}

	public void showFrameSpinner(){
		if (!manager.playerSelected()) {
			hideView(framerSpinner);
			return;
		}
		if (manager.getCurrentPlayer().getRoleName().equals(Framer.ROLE_NAME) && isFrameActionSelected()) {
			showView(framerSpinner);
			((RelativeLayout.LayoutParams) actionLV.getLayoutParams()).addRule(RelativeLayout.BELOW, framerSpinner.getId());
		}else
			hideView(framerSpinner);
	}

	private boolean isFrameActionSelected(){
		String command = commandsTV.getText().toString();
		int abilityID = manager.getCurrentPlayer().parseAbility(command);
		int frameAbilityID = Framer.MAIN_ABILITY;
		return abilityID == frameAbilityID;
	}

	public void hideInfoPanel(){
		hideView(leftTV);
		hideView(rolesLV);

		hideView(rightTV);
		hideView(membersLV);
		hideView(roleInfoTV);
		hideView(button);
	}

	public void showMessagesPanel() {
		showView(chatLV);
		if(manager.playerSelected() || Narrator.DEBUG) {
			showView(chatET);
			showView(chatButton);
		}
		pushChatDown();
	}

	public void pushChatDown(){
		chatLV.post(new Runnable() {
			@Override
			public void run() {
				chatLV.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	public void setPlayerLabel(String name){
		playerLabelTV.setText(name);
	}

	protected void hideView(View v){
		v.setVisibility(View.GONE);
	}
	protected void hideView(int id){
		findViewById(id).setVisibility(View.GONE);
	}

	protected void showView(View v){
		v.setVisibility(View.VISIBLE);
	}

	protected void setListView(ListView v, ArrayList<String> texts, ArrayList<Integer> colors){
		v.setAdapter(new ListingAdapter(texts, colors, this));
	}

	public void endGame(Narrator n){

		hideActionPanel();
		hideInfoPanel();
		hideMessagePanel();
		hideView(playerLabelTV);
		hideView(R.id.day_playerDrawerButton);
		hideView(R.id.day_title);
		hideView(R.id.day_horizontalShimmy);
		hideView(messagesButton);
		hideView(actionButton);
		hideView(infoButton);
		hideView(commandsTV);

		showView(chatLV);
		setChatPanelText(manager.getNarrator().getEvents(Event.PRIVATE, HTML));

		((RelativeLayout.LayoutParams)chatLV.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);

		speaker.stop();
		speaker.shutdown();
		stopTexting();
		/*Intent i = new Intent(this, ActivityEndGame.class);
		i.putExtra(Narrator.KEY, Board.GetParcel(n));
		startActivity(i);
		finish();*/

	}
	private void stopTexting(){
		TextHandler.stopTexting(this, intentReceiver);
	}

	public void setRolesListHeader(){
		leftTV.setText("Roles");
		leftTV.setTextColor(getResources().getColor(R.color.trimmings));
	}
	public void setAlliesHeader(int color){
		leftTV.setText("Allies");
		leftTV.setTextColor(color);
	}

	public void updateRoleInfo(Player r, int color){
		rightTV.setText(r.getRoleName());
		rightTV.setTextColor(color);
		roleInfoTV.setText(r.getRoleInfo());
		roleInfoTV.setTextColor(color);
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		manager.command(manager.getCurrentPlayer().getTarget(Framer.MAIN_ABILITY));
	}

	public void onNothingSelected(AdapterView<?> arg0) {

	}

	public void setVotesToLynch(int votes){
		skipTV.setText("Number of votes to lynch - " + votes);
	}

	public void closeDrawer(){
		dayWindow.closeDrawer(playerMenu);
	}
	public void onDrawerClosed(View v){
		log("drawer closed");
		drawerOut = false;
		manager.updatePlayerControlPanel();
	}
	public void onDrawerOpened(View v){
		drawerOut = true;
	}
	public void onDrawerSlide(View v, float f){}
	public void onDrawerStateChanged(int i){}
	public void onDoubleTap() {
		if (Narrator.DEBUG && !drawerOut && !simulationRunning && manager.getNarrator().isInProgress()) {
			simulationRunning = true;
			manager.nextSimulation();
			simulationRunning = false;
		}
	}
	private boolean simulationRunning = false;
	private boolean drawerOut = false;
	public boolean drawerOut(){
		return drawerOut;
	}


	public boolean dispatchTouchEvent(@NonNull MotionEvent me){
		// Call onTouchEvent of SimpleGestureFilter class
		detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}
	public void onSwipe(int direction) {
		if(direction == SimpleGestureFilter.SWIPE_LEFT){
			log("swiped left");
		}else if (direction == SimpleGestureFilter.SWIPE_RIGHT){
			log("swiped right");
		}
		manager.setNextAbility(direction);
	}
}
