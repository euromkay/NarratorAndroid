package voss.android.alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import voss.android.R;
import voss.android.screens.ActivityHome;


public class NamePrompt extends DialogFragment{

    public View mainView;
    String players;
    ListView lv;
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        mainView = inflater.inflate(R.layout.alert_name_prompt, null);
        String name = getArguments().getString(ActivityHome.HOST_NAME);
        final String buttonName = getArguments().getString(GO_BUTTON);
        if(name != null) {
            EditText et = (EditText) mainView.findViewById(R.id.home_nameET);
            et.setText(name);
        }
        builder.setView(mainView);

        builder.setMessage("What is your name?")
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText et = (EditText) mainView.findViewById(R.id.home_nameET);
                        String name = et.getText().toString();
                        if (name.length() == 0)
                            return;

                        mListener.onNamePromptConfirm(NamePrompt.this, name, buttonName.equals("Host"));
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NamePrompt.this.dismiss();
                    }
                });
        return builder.create();
    }


	public void setTitle(){
        getDialog().setTitle("What is your name?");
    }


	private ArrayAdapter<String> getAdapter(String[] players){
        return new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, players);
    }

    public static final String GO_BUTTON = "NAMEPROMPTGOBUTTON";

    public interface NamePromptListener {
        void onNamePromptConfirm(NamePrompt t, String s, boolean b);
    }

    private NamePromptListener mListener;

    public void onAttach(Activity a){
        super.onAttach(a);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NamePromptListener) a;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(a.toString()
                    + " must implement AddPlayerListenerListener");
        }
    }



	public void dismiss() {
		// TODO Auto-generated method stub
		
	}

}
