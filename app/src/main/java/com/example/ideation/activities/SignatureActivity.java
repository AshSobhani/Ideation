package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideation.R;
import com.example.ideation.database.IdeationContract;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class SignatureActivity extends AppCompatActivity {
	private static final String TAG = "SignatureActivity";

	//Make variables
	String projectUID, requestUID;
	private TextView titleField, confirmPasswordFailedField;
	private EditText confirmPassword;
	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;
	private DownloadManager.Request request;

	//Get firestore and storage instance and store in variables, and then create storage reference
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseStorage storage = FirebaseStorage.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signature);

		//Assign instance and user to variables
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		//Retrieve project UID
		Bundle bundle = getIntent().getExtras();
		projectUID = bundle.getString("projectUID");
		requestUID = bundle.getString("requestUID");

		//Assign views to variables
		titleField = findViewById(R.id.projectTitleText);
		confirmPasswordFailedField = findViewById(R.id.confirmPasswordFailedText);
		confirmPassword = findViewById(R.id.confirmPasswordText);

		retrieveProjectData();
	}

	private void retrieveProjectData() {
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(final DocumentSnapshot documentSnapshot) {
						//Get the project title
						final String projectTitle = documentSnapshot.getString(IdeationContract.PROJECT_TITLE);

						//Set the text fields
						titleField.setText(projectTitle);

						//Get the NDA path and make a storage reference
						String NDAPath = documentSnapshot.getString(IdeationContract.PROJECT_NDA_PATH);

						//Create the download request
						createDownloadRequest(projectTitle, NDAPath);
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(SignatureActivity.this, "Error: Request Not Found", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}

	public void createDownloadRequest(final String projectTitle, String NDAPath) {
		//Make a storage reference
		StorageReference NDAReference = storage.getReference().child(NDAPath);

		//Get the download Url
		NDAReference.getDownloadUrl()
				.addOnSuccessListener(new OnSuccessListener<Uri>() {
					@Override
					public void onSuccess(Uri uri) {
						//Create the file name and make a string url
						String fileName = projectTitle + "NDAForm";

						//Create the download request
						request = new DownloadManager.Request(uri);

						//Customise the download notification
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
						request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, fileName + ".pdf");
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {

					}
				});
	}

	public void onDownloadAndViewFile(View v) {
		//Initialise a download manager
		DownloadManager downloadManager = (DownloadManager) SignatureActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

		//Enqueue the download request and navigate user to downloads
		downloadManager.enqueue(request);
		startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
	}

	public void onSignAndAccept(View v) {
		//Get the confirmed password attempt as a string
		String confirmPasswordText = confirmPassword.getText().toString();

		//Make sure strings are not empty (causing an issue)
		if(confirmPasswordText.equals("")) {
			confirmPasswordText = "empty";
		}

		//Minimise the keyboard on action done
		confirmPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

		//Create user credentials by providing the email but just asking to reconfirm password
		AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), confirmPasswordText);

		//Attempt to re-authenticate the users password for extra security
		firebaseUser.reauthenticate(credential)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Toast.makeText(SignatureActivity.this, "Success!", Toast.LENGTH_SHORT).show();

							//If the password was good then give the user access to the project
							addUserToProjectWhitelist();
							//Finish activity and return
							finish();
						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "re-authenticate:failure", task.getException());

							//Notify the user that the password is wrong by changing text field
							confirmPasswordFailedField.setText("Incorrect password.");
							Toast.makeText(SignatureActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	public void onDecline(View v) {
		//On request declined, set status to request revoked
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).collection(IdeationContract.COLLECTION_ACCESS_REQUESTS).document(requestUID)
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_REQUEST_REVOKED,
						IdeationContract.PROJECT_REQUESTS_APPLICABLE, IdeationContract.TRUE)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request accepted and status updated to request accepted");
					}
				});

		//Finish activity and return
		finish();
	}

	private void addUserToProjectWhitelist() {
		//Retrieve the user UID and put into a string
		String userUID = firebaseUser.getUid();

		//Put the requester user UID into the projects whitelist
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID)
				.update(IdeationContract.PROJECT_WHITELIST, FieldValue.arrayUnion(userUID))
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: User added to whitelist");
					}
				});

		//On request accepted, set status to accepted
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).collection(IdeationContract.COLLECTION_ACCESS_REQUESTS).document(requestUID)
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_REQUEST_ACCEPTED)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request accepted and status updated to request accepted");
					}
				});
	}
}
