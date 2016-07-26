package android.alerts;


import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.NarratorService;
import android.SuccessListener;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.screens.ListingAdapter;
import android.setup.ActivityCreateGame;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import voss.narrator.R;

public class TeamEditor extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SuccessListener {

    public static final String EDITING_ALLIES_TITLE = "Edit Allies and Enemies";
    public static final String EDITING_ROLES_TITLE  = "Edit Available Roles";
    public static final String AVAILABLE_ROLES_TITLE = "Available Roles";
    public static final String BLACKLISTED_ROLES_TITLE = "Blacklisted Roles";
    public static final String ALLIES_TITLE = "Allies";
    public static final String ENEMIES_TITLE = "Enemies";

    public View mainView;
    public ListView l1, l2;
    private LayoutInflater inflater;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        String title, header1, header2;

        if(editingAllies){
            title = EDITING_ALLIES_TITLE;
            header1 = ALLIES_TITLE;
            header2 = ENEMIES_TITLE;
        }else{
            title = EDITING_ROLES_TITLE;
            header1 = AVAILABLE_ROLES_TITLE;
            header2 = BLACKLISTED_ROLES_TITLE;
        }

        getDialog().setTitle(title);
        this.inflater = inflater;
        mainView = inflater.inflate(R.layout.create_team_editor, container);

        l1 = (ListView) mainView.findViewById(R.id.editTeamLV1);
        l2 = (ListView) mainView.findViewById(R.id.editTeamLV2);

        mainView.findViewById(R.id.editTeamConfirm).setOnClickListener(this);
        l1.setOnItemClickListener(this);
        l2.setOnItemClickListener(this);

        ((TextView) mainView.findViewById(R.id.editTeamTV1)).setText(header1);
        ((TextView) mainView.findViewById(R.id.editTeamTV2)).setText(header2);

        setListViews();

        return mainView;
    }

    public void onSuccess(){
        setListViews();
    }

    public void onFailure(String message){
        ac.toast(message);
    }

    private HashMap<String, String> translator;
    private void setListViews(){
        ac.resetView();
    	translator = new HashMap<String, String>();
        try {
            JSONObject obj;
            ArrayList<String> l1Data = new ArrayList<>(), l2Data = new ArrayList<>(), c1Data = new ArrayList<>(), c2Data = new ArrayList<>();
            if (editingAllies) {

                JSONArray allies = ac.activeFaction.getJSONArray("allies");
                JSONArray enemies = ac.activeFaction.getJSONArray("enemies");

                for (int i = 0; i < allies.length(); i++) {
                    obj = allies.getJSONObject(i);
                    l1Data.add(obj.getString("name"));
                    c1Data.add(obj.getString("color"));
                }
                for (int i = 0; i < enemies.length(); i++) {
                    obj = enemies.getJSONObject(i);
                    l2Data.add(obj.getString("name"));
                    c2Data.add(obj.getString("color"));
                }

            } else {
                String color = ac.activeFaction.getString("color");
                JSONArray members = ac.activeFaction.getJSONArray("members");
                JSONArray blackListed = ac.activeFaction.getJSONArray("blacklisted");

                for (int i = 0; i < members.length(); i++) {
                    obj = members.getJSONObject(i);
                    l1Data.add(obj.getString("name"));
                    translator.put(obj.getString("name"), obj.getString("simpleName"));
                    c1Data.add(color);
                }
                for (int i = 0; i < blackListed.length(); i++) {
                    obj = blackListed.getJSONObject(i);
                    l2Data.add(obj.getString("name"));
                    translator.put(obj.getString("name"), obj.getString("simpleName"));
                    c2Data.add(color);
                }

            }

            ListingAdapter adapter1 = new ListingAdapter(l1Data, ac);
            adapter1.layoutInflater = inflater;
            adapter1.setColors(c1Data);

            ListingAdapter adapter2 = new ListingAdapter(l2Data, ac);
            adapter2.layoutInflater = inflater;
            adapter2.setColors(c2Data);

            l1.setAdapter(adapter1);
            l2.setAdapter(adapter2);
        }catch(JSONException e){
            e.printStackTrace();
        }//this is redundant the first time it's called.
    }

    public void onClick(View v){
        getDialog().cancel();
    }

    public void onItemClick(AdapterView<?> av, View v, int position, long l){
    	TextView tv = (TextView) v;
        String name = tv.getText().toString();
        String color = String.format("#%06X", (0xFFFFFF & tv.getCurrentTextColor()));
        try {
            String teamColor = ac.activeFaction.getString("color");

            boolean leftToRight = av == l1;

            if (editingAllies && leftToRight) {
                ns.setEnemies(color, teamColor, this);
            } else if (editingAllies) { // and rightToLeft
                ns.setAllies(color, teamColor, this);
            } else if (leftToRight) { // editingRoles
                ns.removeTeamRole(name, teamColor, this);
            } else {
                ns.addTeamRole(translator.get(name), teamColor, this);
            }
        }catch(JSONException e){e.printStackTrace();}

    }

    public void onDismiss(DialogInterface dialogInterface){
        super.onDismiss(dialogInterface);
        ac.mode = ActivityCreateGame.REGULAR;
    }

    private NarratorService ns;
    private ActivityCreateGame ac;
    private boolean editingAllies;
    public void onAttach(Activity a){
        super.onAttach(a);
        ac = (ActivityCreateGame) a;
        editingAllies = ((ActivityCreateGame) a).mode == ActivityCreateGame.EDITING_ALLIES;
        this.ns = ((ActivityCreateGame) a).ns;
    }
}
