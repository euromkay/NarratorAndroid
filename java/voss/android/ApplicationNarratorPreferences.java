package voss.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

import voss.shared.logic.support.Constants;

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

		Parse.enableLocalDatastore(this);
		ParseCrashReporting.enable(this);
		Parse.initialize(this, "JrqL953o31jfhzzkW6gYQoHrQudrHBu9DLoxFbxZ", "rm5gHJVygUlvs2yJveV3Xh6ywXdvSiwiEpr7ssmH");
	}

	
}
