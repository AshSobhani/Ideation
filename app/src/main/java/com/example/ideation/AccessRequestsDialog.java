package com.example.ideation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AccessRequestsDialog extends AppCompatDialogFragment {
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
		View v = inflater.inflate(R.layout.layout_access_requests_dialog, null);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign view to variable
		resetEmailField = v.findViewById(R.id.resetEmailText);

		//Set the builder view and customise
		builder.setView(v)
				.setTitle("Access Requests")
//				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialogInterface, int i) {
//						//Do something
//					}
//				})
				.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});

		return builder.create();
	}
}
