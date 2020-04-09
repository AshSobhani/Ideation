package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class SignatureActivity extends AppCompatActivity {
	private static final String TAG = "SignatureActivity";

	//Make variables
	String projectUID, requestUID;
	private TextView titleField;
	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;

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
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Set the text fields
						titleField.setText(documentSnapshot.getString(IdeationContract.PROJECT_TITLE));

						//Get the NDA path
						String NDAPath = documentSnapshot.getString(IdeationContract.PROJECT_NDA_PATH);

						StorageReference NDAReference = storage.getReference().child(NDAPath);

						File localFile = null;
						try {
							localFile = File.createTempFile("Downloads", "pdf");
						} catch (IOException e) {
							e.printStackTrace();
						}

						NDAReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
								Log.d(TAG, "onSuccess: file downloaded");
							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception exception) {
								// Handle any errors
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
}
