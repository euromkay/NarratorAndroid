package voss.android.alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class ExitGameAlert extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you really want to exit the game?")
                .setPositiveButton("Exit Game", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onExitGame();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ExitGameAlert.this.dismiss();
                    }
                });
        return builder.create();
    }

    public interface ExitGameListener {
        void onExitGame();
    }

    ExitGameListener mListener;


    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ExitGameListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }
}
