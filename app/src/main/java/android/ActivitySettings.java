package android;

import voss.narrator.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import shared.logic.Narrator;
import shared.logic.Rules;
import shared.logic.exceptions.UnsupportedMethodException;
import shared.logic.support.Constants;

public class ActivitySettings extends Activity implements OnClickListener, OnCheckedChangeListener, OnItemSelectedListener{

	public static Rules getRules(){
		Rules rules = new Rules();

		rules.DAY_START = true;

		rules.doctorKnowsIfTargetIsAttacked = (true);

		rules.serialKillerIsInvulnerable = (true);



		return rules;
	}
	
	private SharedPreferences prefs;
	
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_settings);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Log.d("Settings", ""+prefs.getBoolean(Constants.RULES_DAYSTART, true));
		
		
		changeValues();
		
		addClickListeners();
	}
	
	private void addClickListeners(){
		final int[] keyArray= {
			R.id.rules_doctorTV,
			R.id.rules_serialKillerTV,
		};
		
		for(int key: keyArray){
			findViewById(key).setOnClickListener(this);
		}
	}
	private void changeValues(){
		setSwitch(R.id.rules_day_switch, Constants.RULES_DAYSTART);
		
			
		setSwitch(R.id.rules_self_heal_switch, Constants.RULES_DOCTOR_HEAL_SELF);
		setSwitch(R.id.rules_success_notif_switch, Constants.RULES_DOCTOR_SUCCESS_NOTIFICATION);
			
			
		setSwitch(R.id.rules_sk_invuln_switch, Constants.RULES_SK_INVULN);
			
		
		//dropDownMenu();
	}
	private void setSwitch(int buttonId, String stringId){
		Switch swich = (Switch) findViewById(buttonId);
		
		Log.d("Settings", stringId);
		Log.d("Settings", ""+ prefs.getBoolean(stringId, true));
		
		swich.setChecked(prefs.getBoolean(stringId, true));
		swich.setOnCheckedChangeListener(this);
	}
	/*private void dropDownMenu(){
		Spinner spinner = (Spinner) findViewById(R.id.rules_sk_alignment_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.rules_skValues, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner
		
		spinner.setAdapter(adapter);
		
		
		spinner.setOnItemSelectedListener(this);
	}*/

	public void onClick(View arg0) {
		int id = arg0.getId();
		switch(id){
		
		case R.id.rules_doctorTV:
			setVisible(R.id.rules_doc1);
			setVisible(R.id.rules_doc2);
			break;
			
		case R.id.rules_serialKillerTV:
			setVisible(R.id.rules_sk1);
			break;
		
		}
		
	}
	private void setVisible(int id){
		View v = findViewById(id);
		
		//visible = 0
		if(v.getVisibility() > View.VISIBLE)
			//is currently invisible
			v.setVisibility(View.VISIBLE);
		else
			//is currently invisible
			v.setVisibility(View.GONE);
	}

	
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch(arg0.getId()){
		case R.id.rules_day_switch:
			changeRule(Constants.RULES_DAYSTART, arg1);
			break;
		
			
		case R.id.rules_self_heal_switch:
			changeRule(Constants.RULES_DOCTOR_HEAL_SELF, arg1);
			break;
		case R.id.rules_success_notif_switch:
			changeRule(Constants.RULES_DOCTOR_SUCCESS_NOTIFICATION, arg1);
			break;
			
		
			
		case R.id.rules_sk_invuln_switch:
			changeRule(Constants.RULES_SK_INVULN, arg1);
			break;
			
		}
		
	}
	private void changeRule(String id, boolean b){
		Editor editor = prefs.edit();
		editor.putBoolean(id, b).commit();
	}
	
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch(arg0.getId()){
		/*
		case(R.id.rules_sk_alignment_spinner):
			prefs.edit().remove(Constants.RULES_SK_ALIGNMENT);
			prefs.edit().putInt(Constants.RULES_SK_ALIGNMENT, arg0.getSelectedItemPosition()).commit();
		
		    toast(Integer.toString(prefs.getInt(Constants.RULES_SK_ALIGNMENT, -1)).toString());
			break;
		*/
		}
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	
	
	public void onBackPressed() {
		finish();
		
	}

	public Narrator getNarrator() {
		throw new UnsupportedMethodException();
	}
	
	
}
