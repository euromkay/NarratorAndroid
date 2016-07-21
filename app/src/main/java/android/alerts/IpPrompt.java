package android.alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import voss.narrator.R;


public class IpPrompt extends DialogFragment{


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

        EditText et = (EditText) mainView.findViewById(R.id.home_nameET);
        et.setInputType(InputType.TYPE_CLASS_PHONE);

        builder.setView(mainView);

        builder.setMessage("What is the host's code?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText et = (EditText) mainView.findViewById(R.id.home_nameET);
                        String name = et.getText().toString();
                        if (name.length() == 0)
                            return;

                        mListener.onIpPromptConfirm(IpPrompt.this);
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IpPrompt.this.dismiss();
                    }
                });
        return builder.create();
    }


    public String getIP(){
        EditText et = (EditText) mainView.findViewById(R.id.home_nameET);
        return et.getText().toString().replace("*", "");
    }

    public void setTitle(){
        getDialog().setTitle("What is your name?");
    }

    //private ArrayAdapter<String> getAdapter(String[] players){
    //    return new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, players);
    //}



    public static final String GO_BUTTON = "NAMEPROMPTGOBUTTON";

    public interface IpPromptListener {
        void onIpPromptConfirm(IpPrompt t);
    }

    private IpPromptListener mListener;

    public void onAttach(Activity a){
        super.onAttach(a);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (IpPromptListener) a;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(a.toString()
                    + " must implement AddPlayerListenerListener");
        }
    }

}
