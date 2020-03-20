package com.example.ideation.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.ideation.R;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ResetPasswordDialog extends AppCompatDialogFragment {
	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private EditText resetEmailField;

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		//Get an instance of the dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		//Get the layout inflater and assign to variable to then find our layout dialog
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.layout_reset_password_dialog, null);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign view to variable
		resetEmailField = v.findViewById(R.id.resetEmailText);

		//Set the builder view and customise
		builder.setView(v)
				.setTitle("Forgot your password?")
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Do something
					}
				})
				.setPositiveButton("Send", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Send password reset email to user
						firebaseAuth.sendPasswordResetEmail(resetEmailField.getText().toString());
					}
				});

		return builder.create();
	}
}
