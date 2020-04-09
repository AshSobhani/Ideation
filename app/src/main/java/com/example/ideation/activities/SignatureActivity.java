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
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideation.R;
import com.example.ideation.database.IdeationContract;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class SignatureActivity extends AppCompatActivity {
	private static final String TAG = "SignatureActivity";

	//Make variables
	String projectUID, requestUID;
	private TextView titleField;
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

		//Retrieve project UID
		Bundle bundle = getIntent().getExtras();
		projectUID = bundle.getString("projectUID");
		requestUID = bundle.getString("requestUID");

		//Assign views to variables
		titleField = findViewById(R.id.projectTitleText);

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
						StorageReference NDAReference = storage.getReference().child(NDAPath);

						//Get the download Url
						NDAReference.getDownloadUrl()
								.addOnSuccessListener(new OnSuccessListener<Uri>() {
									@Override
									public void onSuccess(Uri uri) {
										//Create the file name and make a string url
										String fileName = projectTitle + "NDAForm";
										String url = uri.toString();

										//Create the request but don't download yet
										createRequest(fileName, ".pdf", DIRECTORY_DOWNLOADS, url);
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {

									}
								});
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

	public void createRequest(String fileName, String fileExtension, String saveDirectory, String url) {
		//Parse the url and create the download request
		Uri uri = Uri.parse(url);
		request = new DownloadManager.Request(uri);

		//Customise the download notification
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setDestinationInExternalPublicDir(saveDirectory, fileName + fileExtension);
	}

	public void onDownloadAndViewFile(View v) {
		//Initialise a download manager
		DownloadManager downloadManager = (DownloadManager) SignatureActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

		//Enqueue the download request and navigate user to downloads
		downloadManager.enqueue(request);
		startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
	}
}
