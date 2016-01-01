package voss.android.setup;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import voss.android.R;
import voss.shared.logic.Rules;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;

public class SetupScreenController implements SetupListener, CompoundButton.OnCheckedChangeListener {

    private Toast toast;
    private ActivityCreateGame screen;

    private CheckBox[] cBox;
    private EditText[] eText;
    private TextView[] tView;
    private TextWatcher[] tWatcher;


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
                String t = s.toString();
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
                String t = s.toString();
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


    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        switch(activeRole){
            case DOCTOR:
                rules.doctorCanHealSelf = b;
                break;
            case BLOCKER:
                rules.blockersCanBeBlocked = b;
                break;
            case EXECUTIONER:
                rules.exeuctionerImmune = b;
                break;
            case WITCH:
                rules.witchLeavesFeedback = b;
                break;
            case SERIAL_KILLER:
                rules.serialKillerIsInvulnerable = b;
                break;
            case ARSONIST:
                rules.arsonInvlunerable = b;
                break;
            case MASS_MURDERER:
                rules.mmInvulnerable = b;
                break;
            case CULT:
                rules.cultKeepsRoles = b;
                break;
            case GODFATHER:
                rules.gfInvulnerable = b;
        }
    }

    private void secondBox(boolean b){
        switch(activeRole){
            case DOCTOR:
                rules.doctorKnowsIfTargetIsAttacked = b;
                break;
            case EXECUTIONER:
                rules.exeuctionerWinImmune = b;
                break;
            case ARSONIST:
                rules.arsonDayIgnite = b;
                break;
            case CULT:
                rules.cultLeaderCanOnlyRecruit = b;
                break;
            case GODFATHER:
                rules.gfUndetectable = b;
                break;
        }
    }

    private void thirdBox(boolean b){
        switch(activeRole){
            case CULT:
                rules.cultImplodesOnLeaderDeath = b;
                break;
        }
    }

    private void firstET(int i){
        switch(activeRole){
            case MAYOR:
                rules.mayorVoteCount = i;
                break;
            case VIGILANTE:
                rules.vigilanteShots = i;
                break;
            case VETERAN:
                rules.vetAlerts = i;
                break;
            case MASS_MURDERER:
                rules.mmSpreeDelay = i;
                break;
            case CULT:
                rules.cultPowerRoleCooldown = i;
                break;
        }
    }

    private void secondET(int i){
        switch(activeRole){
            case CULT:
                rules.cultConversionCooldown = i;
                break;
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    public void afterTextChanged(Editable s) {

        s.toString();

    }

    public int activeRole;
    public void setRoleInfo(int i, int color, Rules r){
        if( r!= null)
            rules = r;

        if(color != Constants.A_NORMAL)
            setColor(color);
        activeRole = i;
        switch(activeRole) {
            case DOCTOR:
                setDoctor();
                return;
            case VIGILANTE:
                setVigilante();
                return;
            case VETERAN:
                setVeteran();
                return;
            case MAYOR:
                setMayor();
                return;
            case BLOCKER:
                setBlocker();
                return;
            case EXECUTIONER:
                setExecutioner();
                return;
            case WITCH:
                setWitch();
                return;
            case SERIAL_KILLER:
                setSK();
                return;
            case ARSONIST:
                setArson();
                return;
            case MASS_MURDERER:
                setMM();
                return;
            case CULT:
                setCult();
                return;
            case GODFATHER:
                setGF();
                return;
            default:
                hideAll();
        }
    }
    private void setGF(){
        setBooleanTexts("Godfather is invulnerable", "Godfather is undetectable");
        setBoolean(rules.gfInvulnerable, rules.gfUndetectable);
        setTexts();
        setEdits();
    }

    private void setCult(){
        setBooleanTexts("Culted members keep original roles", "Only Cult Leader can recruit");
        setBoolean(rules.cultKeepsRoles, rules.cultLeaderCanOnlyRecruit);
        setTexts("Cooldown after converting non-citizen", "Cooldown after successful conversion");
        setEdits(rules.cultPowerRoleCooldown, rules.cultConversionCooldown);
    }

    private void setMM(){
        setBooleanTexts("Mass Murderer is invulnerable");
        setBoolean(rules.mmInvulnerable);
        setTexts("Mass Murderer spree cool down(when successful)");
        setEdits(rules.mmSpreeDelay);
    }

    private void setArson(){
        setBooleanTexts("Arsonist is invulnerable", "Arsonist has a day ignite");
        setBoolean(rules.arsonInvlunerable, rules.arsonDayIgnite);
        setTexts();
        setEdits();
    }

    private void setSK(){
        setBooleanTexts("Serial Killer is invulnerable");
        setBoolean(rules.serialKillerIsInvulnerable);
        setTexts();
        setEdits();
    }

    private void setWitch(){
        setBooleanTexts("Witch leaves feedbac");
        setBoolean(rules.witchLeavesFeedback);
        setTexts();
        setEdits();
    }

    private void setExecutioner(){
        setBooleanTexts("Executioner is immune", "Executioner is immune upon win");
        setBoolean(rules.exeuctionerImmune, rules.exeuctionerWinImmune);
        setTexts();
        setEdits();
    }

    private void setBlocker(){
        setBooleanTexts("Blockers can be blocked");
        setBoolean(rules.blockersCanBeBlocked);
        setTexts();
        setEdits();
    }

    private void setMayor(){
        setBooleanTexts();
        setTexts("Number of extra votes");
        setEdits(rules.mayorVoteCount);
    }

    private void setVeteran(){
        setBooleanTexts();
        setTexts("Number of Veteran alerts");
        setEdits(rules.vetAlerts);
    }

    private void setVigilante(){
        setBoolean();
        setTexts("Number of Vigilante shots");
        setEdits(rules.vigilanteShots);
    }

    private void setDoctor(){
        setBooleanTexts("Doctor can self heal", "Doctor knows if target was attacked");
        setBoolean(rules.doctorCanHealSelf, rules.doctorKnowsIfTargetIsAttacked);
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
