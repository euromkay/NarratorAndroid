package android.alerts;

import android.NarratorService;
import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.setup.ActivityCreateGame;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import shared.logic.exceptions.IllegalGameSettingsException;
import voss.narrator.R;

public class TeamBuilder extends DialogFragment implements OnClickListener, TextWatcher{

	private NarratorService ns;
	
	
	public EditText colorInput, nameInput;
	public TextView preview;
	public View mainView;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		mainView = inflater.inflate(R.layout.create_team_builder_layout, container);
		
		colorInput = (EditText) mainView.findViewById(R.id.newTeam_colorET);
		nameInput = (EditText) mainView.findViewById(R.id.newTeam_nameET);
		preview = (TextView) mainView.findViewById(R.id.newTeam_previewText);
		
		mainView.findViewById(R.id.newTeam_cancel).setOnClickListener(this);
		mainView.findViewById(R.id.newTeam_submit).setOnClickListener(this);
		nameInput.addTextChangedListener(this);
		colorInput.addTextChangedListener(this);
		return null;
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.newTeam_cancel:
			getDialog().cancel();
			break;
		
		case R.id.newTeam_submit:
			String name = nameInput.getText().toString();
			String color = colorInput.getText().toString().toUpperCase();
			if(name.length() == 0 || color.length() == 0)
				return;
			
			if(!color.startsWith("#"))
				color = "#" + color;
			
			if(color.length() != 4 && color.length() != 7){
				setErrorText(RGB_ERROR_CODE);
				return;
			}
			if(!isHex(color)){
				setErrorText("This isn't in RGB format");
				return;
			}
			
			try{
				ns.newTeam(name, color);
			}catch(IllegalGameSettingsException e){
				setErrorText(e.getMessage());
			}
			break;
		}
		
	}
	
	public static final String RGB_ERROR_CODE = "RGB codes are typically 6 characters long";
	
	private boolean isHex(String color){
		try{
			Color.parseColor(color);
			return true;
		}catch(IllegalArgumentException e){
			return false;
		}	
	}
	
	private void setErrorText(String message){
		preview.setText(message);
		preview.setTextColor(Color.parseColor("#F00"));
	}
	
    public void onAttach(Activity a){
        super.onAttach(a);
        this.ns = ((ActivityCreateGame) a).ns;
    }

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		//unused
	}

	public void afterTextChanged(Editable s) {
		//unused
		String color = colorInput.getText().toString();
		String name = nameInput.getText().toString();
		name = name.replace(" ", "");
		preview.setText(name);
		if(!color.startsWith("#"))
			color = "#" + color;
		if(!isHex(color))
			return;
		preview.setTextColor(Color.parseColor(color));
	}
}
