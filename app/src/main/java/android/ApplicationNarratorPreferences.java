package android;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import shared.logic.support.Constants;

public class ApplicationNarratorPreferences extends Application {
	public void onCreate(){
		super.onCreate();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor edit = prefs.edit();
		
		edit.putBoolean(Constants.RULES_DAYSTART, true);
		
		
		edit.putBoolean(Constants.RULES_DOCTOR_HEAL_SELF, false);
		edit.putBoolean(Constants.RULES_DOCTOR_SUCCESS_NOTIFICATION, true);
		

		edit.putBoolean(Constants.RULES_SK_INVULN, true);
		
		
		edit.commit();
	}

	
}
