package com.example.ideation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DeniedDialog extends AppCompatDialogFragment {
	private static final String TAG = "DeniedDialog";

	//Initialise variables
	private EditText requestReason;

	//Make an database, user, and auth instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		//Get an instance of the dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		//Get the layout inflater and assign to variable to then find our layout dialog
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.layout_denied_dialog, null);

		//Assign instance and user to variables
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		//Assign view to variable
		requestReason = v.findViewById(R.id.requestReasonText);

		//Retrieve project UID
		final String projectUID = getArguments().getString("projectUID");

		builder.setView(v)
				.setTitle("Project Access Denied")
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Do something
					}
				})
				.setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						sendAccessRequest(projectUID);
					}
				});

		return builder.create();
	}

	private void sendAccessRequest(final String projectUID) {
		//Get the current user and project UID
		String userUID = firebaseUser.getUid();
		final String projectUIDFinal = projectUID;

		Log.d(TAG, "sendAccessRequest: " + firebaseAuth.getUid() + " | " + userUID);

		//Access the users record to retrieve User UID and User Name
		db.collection(IdeationContract.COLLECTION_USERS).document(userUID).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Retrieve User UID and User Name and put into variables
						final String userUID = firebaseAuth.getUid();
						final String userName = documentSnapshot.getString(IdeationContract.USER_USERNAME);

						//Access the users record to retrieve User UID and User Name
						db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).get()
								.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
									@Override
									public void onSuccess(DocumentSnapshot documentSnapshot) {
										//Retrieve the project title and put into variable
										final String projectTitle = documentSnapshot.getString(IdeationContract.PROJECT_TITLE);

										//Create data hash map holding user uid, request date, and set request status stage
										Map<String, Object> data = new HashMap<>();
										data.put(IdeationContract.PROJECT_REQUESTS_USERUID, userUID);
										data.put(IdeationContract.PROJECT_REQUESTS_USERNAME, userName);
										data.put(IdeationContract.PROJECT_REQUESTS_PROJECT, projectTitle);
										data.put(IdeationContract.PROJECT_REQUESTS_DATETIME, new Timestamp(new Date()));
										data.put(IdeationContract.PROJECT_REQUESTS_REASON, requestReason.getText().toString());
										data.put(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_ACCESS_REQUESTED);

										//Add a request document to project request
										db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUIDFinal).collection(IdeationContract.COLLECTION_PROJECT_REQUESTS).document(userUID).set(data)
												.addOnSuccessListener(new OnSuccessListener<Void>() {
													@Override
													public void onSuccess(Void aVoid) {
														Log.d(TAG, "onSuccess: Request added");
													}
												});
									}
								});
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(TAG, "Failed to send a request" + e.toString());
					}
				});
	}
}
