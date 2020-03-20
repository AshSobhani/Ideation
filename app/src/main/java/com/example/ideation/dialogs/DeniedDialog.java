package com.example.ideation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
	private String projectUID;
	private Button positiveButton;
	private AlertDialog deniedDialog;
	private EditText requestReason;
	private TextView requestFailedField;

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
		requestFailedField = v.findViewById(R.id.requestFailedText);

		//Retrieve project UID
		projectUID = getArguments().getString("projectUID");

		//Set the dialog settings
		builder.setView(v)
				.setTitle("Project Access Denied")
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Do something
					}
				})
				.setPositiveButton("Send Request", null);

		return builder.create();
	}

	private void sendAccessRequest(String projectUID) {
		//Get the current user and project UID
		String userUID = firebaseUser.getUid();
		final String projectUIDFinal = projectUID;

		//Access the users record to retrieve User UID and User Name
		db.collection(IdeationContract.COLLECTION_USERS).document(userUID).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Retrieve User UID and User Name and put into variables
						final String userUID = firebaseAuth.getUid();
						final String userName = documentSnapshot.getString(IdeationContract.USER_USERNAME);

						//Access the users record to retrieve User UID and User Name
						db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUIDFinal).get()
								.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
									@Override
									public void onSuccess(DocumentSnapshot documentSnapshot) {
										//Retrieve the project title and put into variable
										final String ownerUID = documentSnapshot.getString(IdeationContract.PROJECT_OWNERUID);
										final String projectTitle = documentSnapshot.getString(IdeationContract.PROJECT_TITLE);

										//Add a request document to project request
										db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUIDFinal).collection(IdeationContract.COLLECTION_PROJECT_REQUESTS)
												.whereEqualTo(IdeationContract.PROJECT_REQUESTS_USERUID, userUID)
												.whereEqualTo(IdeationContract.PROJECT_REQUESTS_PROJECT, projectTitle).get()
												.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
													@Override
													public void onComplete(@NonNull Task<QuerySnapshot> task) {
														if (task.isSuccessful()) {
															//Get the document
															QuerySnapshot queryResult = task.getResult();

															//If the document exists don't duplicate requests otherwise create the request
															if (!queryResult.isEmpty()) {
																Log.d(TAG, "Access already requested");

																//If text view is not null post error message
																if(!requestFailedField.equals(null)){
																	requestFailedField.setText("Access already requested for this project");
																}
																//Disable the request button
																positiveButton.setEnabled(false);

															} else {
																Log.d(TAG, "Request Successful");

																//Create data hash map holding user uid, request date, and set request status stage
																Map<String, Object> data = new HashMap<>();
																data.put(IdeationContract.PROJECT_REQUESTS_OWNERUID, ownerUID);
																data.put(IdeationContract.PROJECT_REQUESTS_USERUID, userUID);
																data.put(IdeationContract.PROJECT_REQUESTS_USERNAME, userName);
																data.put(IdeationContract.PROJECT_REQUESTS_PROJECT, projectTitle);
																data.put(IdeationContract.PROJECT_REQUESTS_DATETIME, new Timestamp(new Date()));
																data.put(IdeationContract.PROJECT_REQUESTS_REASON, requestReason.getText().toString());
																data.put(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_ACCESS_REQUESTED);

																//Add a request document to project request
																db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUIDFinal).collection(IdeationContract.COLLECTION_PROJECT_REQUESTS).document().set(data)
																		.addOnSuccessListener(new OnSuccessListener<Void>() {
																			@Override
																			public void onSuccess(Void aVoid) {
																				Log.d(TAG, "onSuccess: Request added");

																				//Once added dismiss the dialog
																				deniedDialog.dismiss();
																			}
																		});
															}
														}
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

	@Override
	public void onStart() {
		super.onStart();
		deniedDialog = (AlertDialog) getDialog();
		if (deniedDialog != null) {
			//Assign button to variable
			positiveButton = deniedDialog.getButton(Dialog.BUTTON_POSITIVE);

			//Set an on click listener
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Try to request access
					sendAccessRequest(projectUID);
				}
			});
		}
	}
}
