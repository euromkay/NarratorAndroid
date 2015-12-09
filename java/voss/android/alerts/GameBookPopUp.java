package voss.android.alerts;


import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import voss.android.ActivitySettings;
import voss.android.R;
import voss.android.parse.GameListing;
import voss.android.parse.Server;
import voss.android.screens.ActivityHome;
import voss.android.screens.ListingAdapter;
import voss.android.setup.ActivityCreateGame;
import voss.shared.logic.Member;
import voss.shared.logic.Narrator;
import voss.shared.logic.support.CommandHandler;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.roles.RandomRole;
import voss.shared.roles.Role;

public class GameBookPopUp extends DialogFragment implements Server.GameFoundListener, AdapterView.OnItemClickListener, View.OnClickListener{

    public static final int RESUME = 0;
    public static final int JOIN = 1;

    private View mainView;
    private ListView gameLV;
    private EditText searchBar;
    private Button goButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mainView = inflater.inflate(R.layout.create_player_list, container);


        searchBar = (EditText) mainView.findViewById(R.id.addPlayerContent);
        goButton = (Button) mainView.findViewById(R.id.addPlayerConfirm);
        gameLV = (ListView) mainView.findViewById(R.id.listView1);

        if (mode == JOIN) {
            Server.GetAllGames(15, this);
            goButton.setText("Search");
            setTitle("Open Games:");
        }else{ //resume
            Server.GetMyGames(this);
            goButton.setText("Continue");
            setTitle("Current Games");
        }
        goButton.setOnClickListener(this);
        return mainView;
    }

    private int mode;
    public void setMode(int mode){
        this.mode = mode;
    }

    private ArrayList<GameListing> games;
    private HashMap<String, GameListing> hostToGame;

    public void onGamesFound(ArrayList<GameListing> games){
        this.games = games;
        hostToGame = new HashMap<>();
        ArrayList<String> toAdd = new ArrayList<>();

        ArrayList<Integer> colors = new ArrayList<>();
        final int started = ActivityCreateGame.parseColor(a,R.color.green);
        final int waiting = ActivityCreateGame.parseColor(a, R.color.yellow);

        for(GameListing gl : games) {
            hostToGame.put(gl.getHostName(), gl);
            toAdd.add(gl.getHeader());
            if(gl.inProgress())
                colors.add(started);
            else
                colors.add(waiting);
        }
        ListingAdapter adapter = new ListingAdapter(toAdd, a);
        adapter.setTextSize(15);
        adapter.setColors(colors);
        gameLV.setAdapter(adapter);
    }

    public void noGamesFound(){
        a.toast("No games found");
    }

    public void onError(String error){
        a.toast(error);
    }

    public void onClick(View v){
        String potentialHostName = getSearchContents();
        if (potentialHostName.length() == 0)
            return;

        if (!hostToGame.containsKey(potentialHostName)){
            a.toast("No games are being run by that host");
        }else
            joinGame(hostToGame.get(potentialHostName));
    }

    public void onItemClick(AdapterView<?> av, View v, int i, long l){
        String prevText = getSearchContents();

        GameListing gameSelected = games.get(i);
        String hostName = gameSelected.getHostName();

        if(hostName.equals(prevText)) {
            joinGame(gameSelected);
        }
        setSearchBar(hostName);
    }

    private String getSearchContents(){
        return searchBar.getText().toString();
    }

    private void setSearchBar(String s){
        searchBar.setText(s);
    }

    private void joinGame(GameListing gl){
        Narrator n = a.getNarrator();
        n.removeAllPlayers();
        n.removeAllRoles();
        for (String name: gl.getPlayerNames()){
            n.addPlayer(name);
        }
        for (String roleCompact: gl.getRoleNames()){
            RoleTemplate rt = RoleTemplate.FromIp(roleCompact);
            if (Role.isRole(rt.getName()))
                n.addRole((Member) rt);
            else
                n.addRole((RandomRole) rt);
        }
        if (gl.inProgress()) {
            n.setSeed(gl.getSeed());
            n.setRules(ActivitySettings.getRules(a));
            n.startGame();

            CommandHandler ch = new CommandHandler(n);
            for (String s : gl.getCommands()) {
                ch.parseCommand(s);
            }
        }
        if (mode == JOIN){
            Server.AddPlayer(gl);
        }

        a.start();//NewGame(gl.getID(), Server.GetCurrentUserName().equals(gl.getHostName()));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gameLV.setOnItemClickListener(this);
    }

    public void setTitle(String s){
        getDialog().setTitle(s);
    }

    private ActivityHome a;
    public void onAttach(Activity a){
        super.onAttach(a);
        this.a = (ActivityHome) a;
    }
}
