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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ViewProjectActivity extends AppCompatActivity {
	private static final String TAG = "ViewProjectActivity";

	//Make variables
	String projectUID;
	private TextView titleField, projectOwnerField, categoryField, descriptionField;
	private Button downloadNDAButton;
	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;
	private DownloadManager.Request request;

	//Get Fire Store instance and store in db variable
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseStorage storage = FirebaseStorage.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_project);

		//Assign instance and user to variables
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		//Retrieve project UID
		Bundle bundle = getIntent().getExtras();
		projectUID = bundle.getString("projectUID");

		//Assign views to variables
		titleField = findViewById(R.id.projectTitleText);
		projectOwnerField = findViewById(R.id.projectOwnerText);
		categoryField = findViewById(R.id.categoryText);
		descriptionField = findViewById(R.id.descriptionText);
		downloadNDAButton = findViewById(R.id.downloadNDAButton);

		//Retrieve and assign user data to text fields
		retrieveProjectData();
	}

	private void retrieveProjectData() {
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Get the project title and assign to final variable
						final String projectTitle = documentSnapshot.getString(IdeationContract.PROJECT_TITLE);

						//Set the text fields
						titleField.setText(projectTitle);
						projectOwnerField.setText(documentSnapshot.getString(IdeationContract.PROJECT_OWNERNAME));
						categoryField.setText(documentSnapshot.getString(IdeationContract.PROJECT_CATEGORY));
						descriptionField.setText(documentSnapshot.getString(IdeationContract.PROJECT_DESCRIPTION));

						//Get the NDA path
						String NDAPath = documentSnapshot.getString(IdeationContract.PROJECT_NDA_PATH);

						//If the project has an NDA
						if (NDAPath != null) {
							//Show the NDA download button
							downloadNDAButton.setVisibility(View.VISIBLE);
							//Create the download request
							createDownloadRequest(projectTitle, NDAPath);
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(ViewProjectActivity.this, "Error: Project Not Found", Toast.LENGTH_SHORT).show();
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
		DownloadManager downloadManager = (DownloadManager) ViewProjectActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

		//Enqueue the download request and navigate user to downloads
		downloadManager.enqueue(request);
		startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
	}
}
