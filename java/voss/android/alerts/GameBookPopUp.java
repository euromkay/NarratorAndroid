package voss.android.alerts;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;

import java.util.ArrayList;

import voss.android.R;
import voss.android.parse.GameListing;
import voss.android.parse.Server;

public class GameBookPopUp extends DialogFragment implements AdapterView.OnItemClickListener, Server.GameFoundListener {


    private View mainView;
    private ListView gameLV;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mainView = inflater.inflate(R.layout.create_player_list, container);
        ((Button) mainView.findViewById(R.id.addPlayerConfirm)).setText("Search");

        setTitle("Current Games:");


        gameLV = (ListView) mainView.findViewById(R.id.listView1);

        Server.GetAllGames(15, this);

        return mainView;
    }

    public void onGamesFound(ArrayList<GameListing> games){

    }

    public void noGamesFound(){
        Toast.makeText(a, "No games found", Toast.LENGTH_SHORT).show();
    }

    public void onItemClick(AdapterView<?> av, View v, int i, long l){

    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gameLV.setOnItemClickListener(this);
    }

    public void setTitle(String s){
        getDialog().setTitle(s);
    }

    private Activity a;
    public void onAttach(Activity a){
        super.onAttach(a);
        this.a = a;
    }
}
