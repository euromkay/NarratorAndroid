package android.setup;

import android.NActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import shared.logic.support.rules.Rules;
import voss.narrator.R;
import shared.logic.support.Communicator;
import shared.logic.support.Constants;
import shared.logic.support.RoleTemplate;

public class SetupScreenController implements SetupListener, CompoundButton.OnCheckedChangeListener {

    private Toast toast;
    private ActivityCreateGame screen;

    public CheckBox[] cBox;
    private EditText[] eText;
    private TextView[] tView;
    private TextWatcher[] tWatcher;

    public static final int DAY = -1;
    public static final int NONE = 0;
    public static final int DOCTOR = 1;
    public static final int VIGILANTE = 2;
    public static final int VETERAN = 3;
    public static final int MAYOR = 4;
    public static final int BLOCKER = 5;
    public static final int EXECUTIONER = 6;
    public static final int WITCH = 7;
    public static final int SERIAL_KILLER = 8;
    public static final int ARSONIST = 9;
    public static final int MASS_MURDERER = 10;
    public static final int GODFATHER = 11;
    public static final int CULT = 12;



    private Rules rules;
    public SetupScreenController(ActivityCreateGame a, boolean isHost) {
        this.screen = a;
        rules = screen.ns.local.getRules();
        toast = Toast.makeText(screen, "", Toast.LENGTH_SHORT);

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
                //String t = s.toString();
                try{
                    firstET(Integer.parseInt(s.toString()));
                    screen.getManager().ruleChange();
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
                    secondET(Integer.parseInt(s.toString()));
                    screen.getManager().ruleChange();
                } catch (NumberFormatException | NullPointerException f){}
                }
            });
    }


    public void onRoleAdd(RoleTemplate listing){
        screen.refreshRolesList();
    }
    public void onRoleRemove(RoleTemplate listing){ screen.refreshRolesList();}

    public void onPlayerAdd(String name, Communicator c){
        if(toast != null)
            toast.cancel();
        toast = screen.toast(name + " has joined.");
    }
    public void onPlayerRemove(String s){
    	screen.toast(s + " has left the lobby.");

    }

    private void hideAll(){
        for(CheckBox cb: cBox)
            cb.setVisibility(View.GONE);

        for(TextView tv: tView)
            tv.setVisibility(View.GONE);

        for(EditText et: eText)
            et.setVisibility(View.GONE);
    }


    public void onCheckedChanged(CompoundButton buttonView, boolean unused) {
        CheckBox cb = (CheckBox) buttonView;
        switch(cb.getId()){
            case R.id.create_check1:
                firstBox(cb.isChecked());
                break;
            case R.id.create_check2:
                secondBox(cb.isChecked());
                break;
            case R.id.create_check3:
                thirdBox(cb.isChecked());
                break;
        }

        screen.getManager().ruleChange();
    }

    private void firstBox(boolean b){

    }

    private void secondBox(boolean b){

    }

    private void thirdBox(boolean b){

    }

    private void firstET(int i){

    }

    private void secondET(int i){

    }

    public int activeRole;
    public void setRoleInfo(int i, String color, Rules r){
        return;
    }
    private void setDay(){
        setBooleanTexts("Day Start");
        setTexts();
        setEdits();
    }



    private synchronized void setBoolean(boolean... bools){
        for(int i = 0; i < cBox.length; i++)
            cBox[i].setOnCheckedChangeListener(null);

        for(int i = 0 ; i < bools.length; i++){
            cBox[i].setChecked(bools[i]);
        }

        for(int i = 0; i < cBox.length; i++)
            cBox[i].setOnCheckedChangeListener(this);
    }

    private void setColor(int color){
        for(CheckBox cb: cBox)
            cb.setTextColor(color);

        for(TextView tv: tView)
            tv.setTextColor(color);
    }

    private void setBooleanTexts(String... texts){
        for(int i = 0 ; i < texts.length; i++){
            cBox[i].setVisibility(View.VISIBLE);
            cBox[i].setText(texts[i]);
        }

        for(int i = texts.length; i < cBox.length; i++){
            cBox[i].setVisibility(View.GONE);
        }
    }

    private void setTexts(String ... texts){
        for(int i = 0 ; i < texts.length; i++){
            tView[i].setVisibility(View.VISIBLE);
            tView[i].setText(texts[i]);
        }

        for(int i = texts.length; i < tView.length; i++){
            tView[i].setVisibility(View.GONE);
        }
    }

    private synchronized void setEdits(int ... nums){
        for(int i = 0; i < eText.length; i++)
            eText[i].removeTextChangedListener(tWatcher[i]);

        for(int i = 0 ; i < nums.length; i++){
            eText[i].setVisibility(View.VISIBLE);
            eText[i].setText(nums[i]+"");
        }

        for(int i = nums.length; i < tView.length; i++) {
            eText[i].setVisibility(View.GONE);
        }


        for(int i = 0; i < eText.length; i++)
            eText[i].addTextChangedListener(tWatcher[i]);

    }
}
