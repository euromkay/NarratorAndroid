package android.alerts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.CommunicatorPhone;
import android.NActivity;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.setup.ActivityCreateGame;
import android.setup.SetupListener;
import android.texting.StateObject;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.wifi.CommunicatorInternet;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorNull;
import shared.logic.support.RoleTemplate;
import voss.narrator.R;


public class PlayerPopUp extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SetupListener{


    public View mainView;
    public ListView lv;
    public JSONArray players;
    private boolean first = true;
    
    public static final String COMPUTER_COMMAND = "set computers";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mainView = inflater.inflate(R.layout.create_player_list, container);

        lv = (ListView) mainView.findViewById(R.id.listView1);

        ListAdapter adapter = getAdapter();
        lv.setAdapter(adapter);
        if (activity.server.IsLoggedIn()) {
            mainView.findViewById(R.id.addPlayerContent).setVisibility(View.GONE);
            mainView.findViewById(R.id.addPlayerConfirm).setVisibility(View.GONE);
        }else {
            lv.setOnItemClickListener(this);
            mainView.findViewById(R.id.addPlayerConfirm).setOnClickListener(this);
        }



        setTitle();

        EditText edit_txt = (EditText) mainView.findViewById(R.id.addPlayerContent);

        edit_txt.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(null);
                    return true;
                }
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onClick(null);
                    return true;
                }
                return false;
            }
        });

        return mainView;
    }

    public void setTitle(){
        getDialog().setTitle("Players in Game - " + mListener.getNarrator().getPlayerCount());
    }

    public void onItemClick(AdapterView<?> unusedA, View clickedItem, int position, long unusedL){
        
        if(activity.server.IsLoggedIn()){
        	return;
        } 
        Player clicked = null;
		try {
			clicked = activity.ns.local.getPlayerByName(players.getJSONObject(position).getString(StateObject.playerName));
		} catch (JSONException e) {
			e.printStackTrace();
		}

        if(clicked.getCommunicator().getClass() == CommunicatorInternet.class)
            return;
        if(clicked.getCommunicator().getClass() == CommunicatorNull.class){
            if(clicked.isComputer())
                activity.getManager().removePlayer(clicked.getName(), false);//gotta be a host delete
            else
                return;
        }
        if(clicked.isComputer()){
        	activity.getManager().removePlayer(clicked.getName(), false);//gotta be a host delete
        }else if(!activity.getManager().isHost()) {
        	activity.getManager().requestRemovePlayer(clicked.getName());
        }else {
            TextView tv = (TextView) clickedItem;
            tv.setTextColor(ActivityCreateGame.ParseColor(activity, R.color.redBlood));
            clicked.setComputer();
            clicked.setCommunicator(new CommunicatorPhone());
            if (first){
                activity.toast(clicked.getName() + " is now a computer. Click again to delete.");
                first = false;
            }
        }
    }

    private ListAdapter getAdapter(){
        return new ListAdapter(players, activity);
    }


    public void onClick(View v) {
    	if(activity.server.IsLoggedIn())
    		return;
        EditText et = (EditText) mainView.findViewById(R.id.addPlayerContent);
        String name = et.getText().toString();

        et.setText("");
        if (name.length() == 0)
            return;

        Narrator n = activity.ns.local;
        if(name.equals(COMPUTER_COMMAND)){
            for(Player p: n.getAllPlayers())
                p.setComputer();
            updatePlayerList();
            return;
        }


        if (checkName(name, et))
            return;

        if (mListener.getNarrator().getAllPlayers().hasName(name)){
            Toast.makeText(getActivity(), "Name taken", Toast.LENGTH_LONG).show();
            return;
        }


        activity.getManager().addPlayer(name, new CommunicatorPhone());

    }

    public void updatePlayerList(){
    	updatePlayerData();
        lv.setAdapter(getAdapter());
    }
    
    private void updatePlayerData(){
    	JSONObject playerObject = activity.ns.getPlayers(); 
        try {
			players = playerObject.getJSONArray("Lobby");
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    }

    public void pushPlayersDown(){
        lv.post(new Runnable() {
            public void run() {
                lv.setSelection(players.length()-1);
            }
        });
    }



    private boolean checkName(String name, EditText et){
        if (name.toLowerCase().equals("cancel") || name.toLowerCase().equals("skip")){
            Toast.makeText(getActivity(), "don't use this name", Toast.LENGTH_LONG).show();
            et.setText("");
            return true;
        }
        return false;
    }

    public interface AddPlayerListener {
        Narrator getNarrator();
        void onPopUpDismiss();
        void onPopUpCreate(PlayerPopUp p);
    }

    private AddPlayerListener mListener;

    private ActivityCreateGame activity;
    public void onAttach(Activity a){
        super.onAttach(a);
        this.activity = (ActivityCreateGame) a;
        activity.getManager().addListener(this);
        try {
            mListener = (AddPlayerListener) a;
            mListener.onPopUpCreate(this);
            updatePlayerData();
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " must implement AddPlayerListenerListener");
        }
    }

    public void onDismiss(final DialogInterface arg0) {
        mListener.onPopUpDismiss();
        activity.getManager().removeListener(this);
        super.onDismiss(arg0);
    }


    public void onRoleAdd(RoleTemplate l){}
    public void onRoleRemove(String s1, String s2){}

    public void onPlayerAdd(String name, Communicator c){
        updatePlayerList();
        pushPlayersDown();
        setTitle();
    }

    public void onPlayerRemove(String name){
        updatePlayerList();
        setTitle();
    }


    public class ListAdapter extends BaseAdapter{

        private JSONArray data;
        private NActivity c;
        private Typeface font;

        public ListAdapter(JSONArray data, NActivity c){
            this.data = data;
            this.c = c;
            font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
        }

        public int getCount() {
            return data.length();
        }

        public String getItem(int position) {
            try {
				return data.getString(position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView result;

            if (convertView == null) {
                result = (TextView) c.getLayoutInflater().inflate(R.layout.create_roles_right_item, parent, false);
            } else {
                result = (TextView) convertView;
            }
 
            try{
	            JSONObject player = data.getJSONObject(position);
	            String name = player.getString(StateObject.playerName);
	            int color;
	            if (player.getBoolean("isComputer")){
	                color = getColor(R.color.redBlood);
	            }else{
	                color = getColor(R.color.white);
	            }
	            result.setText(name);
	            //result.setTypeface(font);
	            result.setTextColor(color);
            }catch(JSONException e){
            	e.printStackTrace();
            }

            return result;


        }

        private int getColor(int id){
            return NActivity.ParseColor(c, id);
        }

		public int size(){
			return data.length();
		}
    }
}
