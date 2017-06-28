package com.blacksparrowgames.simplenote.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.blacksparrowgames.simplenote.R;

public class EditNameDialog extends DialogFragment {

    EditNameDialogListener listener;

    public EditNameDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_add_note, (ViewGroup) getActivity().findViewById(R.id.root));
        final EditText mEditText = (EditText) layout.findViewById(R.id.txt_your_name);
        mEditText.setText(listener.getNameToEdit());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        // Now configure the AlertDialog
        builder.setTitle("New note...");
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onFinishEditDialog(false, null);
                EditNameDialog.this.getDialog().cancel();
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String input = mEditText.getText().toString().trim();
                if(!input.equals(""))
                    listener.onFinishEditDialog(true, input);
                else
                    Toast.makeText(getActivity(), "Empty title", Toast.LENGTH_SHORT).show();
                EditNameDialog.this.getDialog().cancel();
            }
        });
        // Create the AlertDialog and return it
        AlertDialog passwordDialog = builder.create();
        return passwordDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        try {
            listener = (EditNameDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EditNameDialogListener in Activity");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditNameDialogListener) {
            listener = (EditNameDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EditNameDialogListener in Activity");
        }
    }

    public interface EditNameDialogListener {
        public void onFinishEditDialog(boolean result, String editedName);
        public String getNameToEdit();
    }
}