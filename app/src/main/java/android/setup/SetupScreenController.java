package android.setup;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.parse.Server;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import shared.logic.support.Communicator;
import shared.logic.support.Constants;
import shared.logic.support.RoleTemplate;
import shared.logic.support.rules.Rules;
import voss.narrator.R;

public class SetupScreenController implements SetupListener, CompoundButton.OnCheckedChangeListener {

    private Toast toast;
    private ActivityCreateGame screen;

    public CheckBox[] cBox;
    public EditText[] eText;
    public TextView[] tView;
    private TextWatcher[] tWatcher;
    private HashMap<Integer, String> ruleMap;

    public SetupScreenController(ActivityCreateGame a, boolean isHost) {
        this.screen = a;
        toast = Toast.makeText(screen, "", Toast.LENGTH_SHORT);

        ruleMap = new HashMap<>();
        
        cBox = new CheckBox[3];
        cBox[0] = (CheckBox) screen.findViewById(R.id.create_check1);
        cBox[1] = (CheckBox) screen.findViewById(R.id.create_check2);
        cBox[2] = (CheckBox) screen.findViewById(R.id.create_check3);

        for(CheckBox cb: cBox){
            cb.setOnCheckedChangeListener(this);
            cb.setEnabled(isHost);
        }

        tWatcher = new TextWatcher[2];

        tView = new TextView[2];
        tView[0] = (TextView) screen.findViewById(R.id.create_rulesTV1);
        tView[1] = (TextView) screen.findViewById(R.id.create_rulesTV2);

        eText = new EditText[2];
        eText[0] = (EditText) screen.findViewById(R.id.create_rulesET1);
        eText[0].setEnabled(isHost);
        eText[0].addTextChangedListener(tWatcher[0] = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {
                try{
                    int val = Integer.parseInt(s.toString());
                    String rule = ruleMap.get(eText[0].getId());
                    screen.getManager().ruleChange(rule, val);
                }catch(NumberFormatException | NullPointerException f){}
            }
        });

        eText[1] = (EditText) screen.findViewById(R.id.create_rulesET2);
        eText[1].setEnabled(isHost);
        eText[1].addTextChangedListener(tWatcher[1] = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {
                //String t = s.toString();
                try {
                	int val = Integer.parseInt(s.toString());
                    String rule = ruleMap.get(eText[0].getId());
                    screen.getManager().ruleChange(rule, val);
                } catch (NumberFormatException | NullPointerException f){}
                }
            });
        
        TextView newTeamButton = (TextView) screen.findViewById(R.id.create_createTeamButton);
        if(!isHost){
        	newTeamButton.setVisibility(View.GONE);
        }
    }
    
    


    public void onRoleAdd(RoleTemplate listing){
        screen.refreshRolesList();
    }
    public void onRoleRemove(String name, String color){ screen.refreshRolesList();}

    public void onPlayerAdd(String name, Communicator c){
        if(toast != null)
            toast.cancel();
        toast = screen.toast(name + " has joined.");
    }
    public void onPlayerRemove(String s){
        if(toast != null)
            toast.cancel();
        toast = screen.toast(s + " has left the lobby.");
    }

    private void hideAll(){
        for(CheckBox cb: cBox)
            cb.setVisibility(View.GONE);

        for(TextView tv: tView)
            tv.setVisibility(View.GONE);

        for(EditText et: eText)
            et.setVisibility(View.GONE);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean newValue) {
        CheckBox cb = (CheckBox) buttonView;
        String id = ruleMap.get(cb.getId());

        screen.getManager().ruleChange(id, newValue);
    }

    public void setRoleInfo(JSONObject activeRule) throws JSONException{
        if(activeRule == null){
            hideAll();
            screen.findViewById(R.id.create_info_wrapper).setVisibility(View.GONE);
    		screen.setDescriptionText("", "", Constants.A_RANDOM);
            return;
        }
        screen.findViewById(R.id.create_info_wrapper).setVisibility(View.VISIBLE);
        
		String color = activeRule.getString("color");
		String name = activeRule.getString("name");
		String description = activeRule.getString("description");
		
		screen.setDescriptionText(name, description, color);
        
        JSONArray rules = null;
        if(name.equals("Randoms")){
        	rules = new JSONArray();
        	rules.put(Rules.DAY_START[0]);
        	if(Server.IsLoggedIn()){
        		rules.put(Rules.DAY_LENGTH[0]);
        		rules.put(Rules.NIGHT_LENGTH[0]);
        	}
        	screen.findViewById(R.id.create_info_label).setVisibility(View.GONE);
        	screen.findViewById(R.id.create_info_description).setVisibility(View.GONE);
        }else if(activeRule.has("isEditable") && activeRule.getBoolean("isEditable")){
        	rules = new JSONArray();
        	rules.put(color + "kill");
        	rules.put(color + "identity");
        	rules.put(color + "liveToWin");
        	rules.put(color + "priority");
        	
        }else{
        	rules = activeRule.getJSONArray("rules");
        }
        String ruleName, ruleText;
        JSONObject rule;
        ArrayList<String> booleanTexts = new ArrayList<>(), numTexts = new ArrayList<>();
        ArrayList<Boolean> bools = new ArrayList<>();
        ArrayList<Integer> ints = new ArrayList<>();
        for(int i = 0; i < rules.length(); i++){
        	ruleName = rules.getString(i);
        	rule = screen.ns.getRuleById(ruleName);
        	ruleText = rule.getString("name");
        	if(rule.getBoolean("isNum")){
        		ruleMap.put(eText[numTexts.size()].getId(), ruleName);
        		ints.add(rule.getInt("val"));
        		numTexts.add(ruleText);
        	}else{
        		ruleMap.put(cBox[numTexts.size()].getId(), ruleName);
        		booleanTexts.add(ruleText);
        		bools.add(rule.getBoolean("val"));
        	}
        }
        setBoolean(bools);
        setBooleanTexts(booleanTexts);
        setEdits(ints);
        setETexts(numTexts);
        activeRule.toString();
    }
    



    private synchronized void setBoolean(ArrayList<Boolean> bools){
        for(int i = 0; i < cBox.length; i++)
            cBox[i].setOnCheckedChangeListener(null);

        for(int i = 0 ; i < bools.size(); i++){
            cBox[i].setChecked(bools.get(i));
        }

        for(int i = 0; i < cBox.length; i++)
            cBox[i].setOnCheckedChangeListener(this);
    }

    
    /*private void setColor(int color){
        for(CheckBox cb: cBox)
            cb.setTextColor(color);

        for(TextView tv: tView)
            tv.setTextColor(color);
    }*/

    private void setBooleanTexts(ArrayList<String> texts){
        for(int i = 0 ; i < texts.size(); i++){
            cBox[i].setVisibility(View.VISIBLE);
            cBox[i].setText(texts.get(i));
        }

        for(int i = texts.size(); i < cBox.length; i++){
            cBox[i].setVisibility(View.GONE);
        }
    }

    private void setETexts(ArrayList<String> texts){
        for(int i = 0 ; i < texts.size(); i++){
            tView[i].setVisibility(View.VISIBLE);
            tView[i].setText(texts.get(i));
        }

        for(int i = texts.size(); i < tView.length; i++){
            tView[i].setVisibility(View.GONE);
        }
    }

    private synchronized void setEdits(ArrayList<Integer> nums){
        for(int i = 0; i < eText.length; i++)
            eText[i].removeTextChangedListener(tWatcher[i]);

        for(int i = 0 ; i < nums.size(); i++){
            eText[i].setVisibility(View.VISIBLE);
            eText[i].setText(nums.get(i)+"");
        }

        for(int i = nums.size(); i < tView.length; i++) {
            eText[i].setVisibility(View.GONE);
        }


        for(int i = 0; i < eText.length; i++)
            eText[i].addTextChangedListener(tWatcher[i]);

    }
}
