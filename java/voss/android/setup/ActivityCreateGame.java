package voss.android.setup;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import voss.android.R;
import voss.android.alerts.PlayerPopUp;
import voss.android.parse.Server;
import voss.android.screens.ActivityHome;
import voss.android.screens.ListingAdapter;
import voss.android.wifi.CommunicatorInternet;
import voss.packaging.Board;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.roles.Agent;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Blackmailer;
import voss.shared.roles.Bodyguard;
import voss.shared.roles.BusDriver;
import voss.shared.roles.Chauffeur;
import voss.shared.roles.Citizen;
import voss.shared.roles.Consort;
import voss.shared.roles.CultLeader;
import voss.shared.roles.Cultist;
import voss.shared.roles.Detective;
import voss.shared.roles.Doctor;
import voss.shared.roles.Escort;
import voss.shared.roles.Executioner;
import voss.shared.roles.Framer;
import voss.shared.roles.Godfather;
import voss.shared.roles.Janitor;
import voss.shared.roles.Jester;
import voss.shared.roles.Lookout;
import voss.shared.roles.Mafioso;
import voss.shared.roles.MassMurderer;
import voss.shared.roles.Mayor;
import voss.shared.roles.SerialKiller;
import voss.shared.roles.Sheriff;
import voss.shared.roles.Veteran;
import voss.shared.roles.Vigilante;
import voss.shared.roles.Witch;


//TODO have to remove spaces from names

public class ActivityCreateGame extends FragmentActivity implements OnItemClickListener, OnClickListener, PlayerPopUp.AddPlayerListener {

	public static final int TOWN = 0;
	public static final int MAFIA = 1;
	public static final int YAKUZA = 2;
	public static final int NEUTRAL = 3;
	public static final int RANDOM = 4;



    private static final int[] neutralPointerList = {R.color.greenYellow, R.color.greenYellow, R.color.outcasts, R.color.orange, R.color.serialKiller, R.color.massMurderer, R.color.benigns, R.color.benigns};
    private static final int[] randomsPointerList = {R.color.white, R.color.town, R.color.town, R.color.town, R.color.town, R.color.mafia, R.color.yakuza, R.color.neutral};

	public ListView cataLV, rolesLV, rolesListLV;
	public TextView playersInGameTV, rolesLeftTV;
	private Narrator narrator;


	protected void onCreate(Bundle b){
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_creategame);
		
		setup(b);
	}
	
	protected void onSaveInstanceState(Bundle b){
		super.onSaveInstanceState(b);
		b.putParcelable(Narrator.KEY, Board.GetParcel(narrator));
		manager.stopTexting();
	}
	
	protected void onResume(){
		super.onResume();
		setup(null);
		cataLV.setSelection(0);
		manager.resumeTexting();
	}
	
	public void onBackPressed(){
		Intent i = new Intent(this, ActivityHome.class);
		i.putExtra(Narrator.KEY, Board.GetParcel(narrator));
		manager.shutdown();
		startActivity(i);
		finish();
	}

	public void onPause(){
		manager.stopTexting();
		super.onPause();
	}


	public static int parseColor(Context context, int id) {
		final int version = Build.VERSION.SDK_INT;
		if (version >= 23) {
			return ContextCompat.getColor(context, id);
		} else {
			return context.getResources().getColor(id);
		}
	}
	
	private int[] getColorArray(int ... values){
		int[] array = new int[values.length];

		for(int i = 0; i < values.length; i++){
			array[i] = parseColor(this, values[i]);
		}
			
		
		return array;
		
	}
	public static final String IP_KEY = "@3ip_key";
	private SetupManager manager;
	private void setup(Bundle b){
		if(manager == null){
			setupCategories();
			
			if(b == null)
				narrator = Board.getNarrator(getIntent().getParcelableExtra(Narrator.KEY));
			else
				narrator = Board.getNarrator(b.getParcelable(Narrator.KEY));

			//narrator = Narrator.Default();

			Intent tent = getIntent();
			boolean isHost = tent.getBooleanExtra(ActivityHome.ISHOST, false);
			manager = new SetupManager(isHost, this, narrator);

			manager.setName(tent.getStringExtra(ActivityHome.MYNAME));


			if(ActivityHome.buildNumber() >= 16)
				manager.setupConnection(tent.getStringExtra(IP_KEY));


			rolesLeftTV = (TextView) findViewById(R.id.roles_hint_title);

			setupRoleCatalogue();
			setupRoleList();

			findViewById(R.id.roles_show_Players).setOnClickListener(this);
			Button startGameButton = (Button) findViewById(R.id.roles_startGame);
			if(manager.isHost())
				startGameButton.setOnClickListener(this);
			else if (Server.IsLoggedIn()) {
				startGameButton.setOnClickListener(this);
				startGameButton.setText("Exit");
			}else
				findViewById(R.id.roles_startGame).setVisibility(View.GONE);
			
			changeRoleType(TOWN);
			setHostCode();
		}
	}

	private void changeFont(int id){
		TextView tv = (TextView) findViewById(id);
		Typeface font = Typeface.createFromAsset(getAssets(), "Trocchi-Regular.ttf");
		tv.setTypeface(font);
	}
	public SetupManager getManager(){
		return manager;
	}

	private void setupCategories(){
		cataLV = (ListView) findViewById(R.id.roles_categories_LV);
	
		final String[] CATEGORYTYPES = {"Town", "Mafia", "Yakuza", "Neutral", "Randoms"};
		final int[] categoryColors = getColorArray(R.color.town, R.color.mafia, R.color.yakuza, R.color.neutral, R.color.white);
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
		if(manager.isHost())
			rolesLV.setOnItemClickListener(this);
		rolesLV.setItemsCanFocus(false);
		changeFont(R.id.roles_bottomLV_title);
	}

	private void setupRoleList(){
		rolesListLV = (ListView) findViewById(R.id.roles_rolesList);
		rolesListLV.setChoiceMode(android.widget.AbsListView.CHOICE_MODE_SINGLE);
		if(manager.isHost())
			rolesListLV.setOnItemClickListener(this);

		refreshRolesList();
		changeFont(R.id.roles_rightLV_title);
	}

	
	public void refreshRolesList(){
		ArrayList<String> names = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();

		for(RoleTemplate r : narrator.getAllRoles()){
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
        	rolesLeftTV.setText(manager.wifi.getIp().replace(".", "*"));
		else
			rolesLeftTV.setVisibility(View.GONE);
    }
	

	public static int colorToTeam(String role, boolean yakuza){
		int town = Constants.A_TOWN;
        int mafia;

        if (yakuza)
            mafia = Constants.A_YAKUZA;
        else
            mafia = Constants.A_MAFIA;
		
		if(role.equals(Citizen.ROLE_NAME))
			return town;
		else if(role.equals(Sheriff.ROLE_NAME))
			return town;
		else if(role.equals(Doctor.ROLE_NAME))
			return town;
		else if(role.equals(Lookout.ROLE_NAME))
			return town;
		else if(role.equals(Detective.ROLE_NAME))
			return town;
		else if(role.equals(BusDriver.ROLE_NAME))
			return town;
		else if(role.equals(Escort.ROLE_NAME))
			return town;
		else if(role.equals(Vigilante.ROLE_NAME))
			return town;
		else if(role.equals(Mayor.ROLE_NAME))
			return town;
		else if(role.equals(Bodyguard.ROLE_NAME))
			return town;
		else if(role.equals(Veteran.ROLE_NAME))
			return town;	
		else if(role.equals(Constants.TOWN_INVESTIGATIVE_ROLE_NAME))
			return town;
		else if(role.equals(Constants.TOWN_PROTECTIVE_ROLE_NAME))
			return town;
		else if(role.equals(Constants.TOWN_KILLING_ROLE_NAME))
			return town;
		else if(role.equals(Constants.TOWN_GOVERNMENT_ROLE_NAME))
			return town;
		else if(role.equals(Constants.TOWN_RANDOM_ROLE_NAME))
			return town;
			
		else if(role.equals(Godfather.ROLE_NAME))
			return mafia;
		else if(role.equals(Mafioso.ROLE_NAME))
			return mafia;
		else if(role.equals(Janitor.ROLE_NAME))
				return mafia;
		else if(role.equals(Blackmailer.ROLE_NAME))
			return mafia;
		else if(role.equals(Agent.ROLE_NAME))
			return mafia;
		else if(role.equals(Chauffeur.ROLE_NAME))
			return mafia;
		else if(role.equals(Consort.ROLE_NAME))
			return mafia;
		else if(role.equals(Framer.ROLE_NAME))
			return mafia;
		else if(role.equals(Constants.MAFIA_RANDOM_ROLE_NAME))
			return mafia;
		else if(role.equals(Constants.YAKUZA_RANDOM_ROLE_NAME))
			return Constants.A_YAKUZA;
		
		else if(role.equals(CultLeader.ROLE_NAME))
			return Constants.A_CULT;
		else if(role.equals(Cultist.ROLE_NAME))
			return Constants.A_CULT;
		
		else if(role.equals(SerialKiller.ROLE_NAME))
			return Constants.A_SK;
		else if(role.equals(MassMurderer.ROLE_NAME))
			return Constants.A_MM;
		else if(role.equals(Arsonist.ROLE_NAME))
			return Constants.A_ARSONIST;
		
		else if(role.equals(Constants.NEUTRAL_RANDOM_ROLE_NAME))
			return Constants.A_NEUTRAL;
		else if(role.equals(Jester.ROLE_NAME))
			return Constants.A_BENIGN;
		else if(role.equals(Executioner.ROLE_NAME))
			return Constants.A_BENIGN;
		
		else if(role.equals(Witch.ROLE_NAME))
			return Constants.A_OUTCASTS;
		
		else if(role.equals(Constants.ANY_RANDOM_ROLE_NAME)){
			return Constants.A_RANDOM;
		}
		
		else throw new IllegalArgumentException(role);
	}


	private int currentCatalogue;
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		RoleTemplate role;
		switch(parent.getId()){
		
		case R.id.roles_categories_LV:
			currentCatalogue = position;
			changeRoleType(position);
			return;
			
		case R.id.roles_bottomLV:
			//position = rolesLV.getCheckedItemPosition();
			if(position != AbsListView.INVALID_POSITION){
				role = getSelectedRole(position);
				manager.addRole(role);
			}
			break;
			
		case R.id.roles_rolesList:
			position = rolesListLV.getCheckedItemPosition();
			if(position != AbsListView.INVALID_POSITION){
				role = narrator.getAllRoles().get(position);
				manager.removeRole(role);
			}
			break;
		}
		
			
		
 	}
	
	
	private void changeRoleType(int position){
		String[] rolesList;
		int[] colors;
		
		switch(position){
		case TOWN:
			rolesList = getResources().getStringArray(R.array.roles_townRoles);
			colors = fillIntArray(R.color.town, rolesList.length);
			break;
			
		case MAFIA:
			rolesList = getResources().getStringArray(R.array.roles_mafiaRoles);
			colors = fillIntArray(R.color.mafia, rolesList.length);
			break;

        case YAKUZA:
            rolesList = getResources().getStringArray(R.array.roles_mafiaRoles);
            colors = fillIntArray(R.color.yakuza, rolesList.length);
            break;
			
		case NEUTRAL:
			rolesList = getResources().getStringArray(R.array.roles_neutralRoles);
			colors = fillIntArray(neutralPointerList);
			break;
			
		case RANDOM:
			rolesList = getResources().getStringArray(R.array.roles_randomRoles);
			colors = fillIntArray(randomsPointerList);
			break;
			
		default:
			throw new IndexOutOfBoundsException();
		}

		ListingAdapter ada = new ListingAdapter(rolesList, this);
		ada.setColors(colors);
		ada.setLayoutID(R.layout.create_roles_left_item);
		rolesLV.setAdapter(ada);
		
	}
	private int[] fillIntArray(int colorPointer, int length){
		int[] array = new int[length];
		
		int color = readColorPointer(colorPointer);
		for(int j = 0; j < length; j++)
			array[j] = color;
		
		return array;
	}

    private int readColorPointer(int colorPointer){
        return parseColor(this, colorPointer);
    }

    private int[] fillIntArray(int[] colorPointers){
        int[] colors = new int[colorPointers.length];
        for (int i = 0; i < colorPointers.length; i++)
            colors[i] = readColorPointer(colorPointers[i]);
        return colors;
    }

	
	
	public void onClick(View v) {
		switch(v.getId()) {

            case R.id.roles_startGame:
				if (manager.isHost())
					manager.startGame(manager.getNarrator().getSeed());
				else
					manager.exitGame();
				break;

            case R.id.roles_show_Players:
                showPlayerList();
				break;
        }

	}

    public void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

	private void showPlayerList(){
		new PlayerPopUp().show(getFragmentManager(), "playerlist");
	}

	private PlayerPopUp popup;
	public void onPopUpDismiss(){
		popup = null;
	}

	public void onPopUpCreate(PlayerPopUp p){
		popup = p;
	}

	public Narrator getNarrator(){
		return narrator;
	}

	public void onPlayerRemove(Player selected){
		narrator.removePlayer(selected);
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
		
		return RoleTemplate.Creator(s, colorToTeam(s, currentCatalogue == YAKUZA)) ;
	}







}

