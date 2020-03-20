package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewProjectActivity extends AppCompatActivity {
	private static final String TAG = "NewProjectActivity";

	//Initialise variables
	EditText titleField, descriptionField, categoryField;
	String titleText, descriptionText, categoryText;
	private FirebaseAuth firebaseAuth;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_project);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign views to variables
		titleField = findViewById(R.id.projectTitle);
		descriptionField = findViewById(R.id.projectDescription);
		categoryField = findViewById(R.id.projectCategory);
	}

	public void onAddProject(View v) {
		//Retrieve the strings
		titleText = titleField.getText().toString();
		descriptionText = descriptionField.getText().toString();
		categoryText = categoryField.getText().toString();

		//Add project if fields are not empty and finish activity
		if (!titleField.equals("") && !descriptionText.equals("") && !categoryText.equals("")) {
			addProjectToCollection();
			finish();
		} else {
			Toast.makeText(NewProjectActivity.this, "Error: Empty Fields", Toast.LENGTH_SHORT).show();
		}

	}

	private void addProjectToCollection() {
		//Access the users record to retrieve User UID and User Name
		db.collection(IdeationContract.COLLECTION_USERS).document(firebaseAuth.getUid()).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Retrieve User UID and User Name and put into variables
						String ownerUID = firebaseAuth.getUid();
						String ownerName = documentSnapshot.getString(IdeationContract.USER_USERNAME);

						//Create a hash to store the data before inserting into firebase
						Map<String, Object> projectInfo = new HashMap<>();
						projectInfo.put(IdeationContract.PROJECT_OWNERUID, ownerUID);
						projectInfo.put(IdeationContract.PROJECT_OWNERNAME, ownerName);
						projectInfo.put(IdeationContract.PROJECT_TITLE, titleText);
						projectInfo.put(IdeationContract.PROJECT_DESCRIPTION, descriptionText);
						projectInfo.put(IdeationContract.PROJECT_CATEGORY, categoryText);
						projectInfo.put(IdeationContract.PROJECT_DATE_CREATED, new Timestamp(new Date()));
						//Add the owner to the project white list
						projectInfo.put(IdeationContract.PROJECT_WHITELIST, Arrays.asList("Initiator"));

						//Insert project into project collection
						db.collection(IdeationContract.COLLECTION_PROJECTS).add(projectInfo)
								.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
									@Override
									public void onSuccess(DocumentReference documentReference) {
										Toast.makeText(NewProjectActivity.this, "Project Created", Toast.LENGTH_SHORT).show();
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Toast.makeText(NewProjectActivity.this, "Failed to add project", Toast.LENGTH_SHORT).show();
										Log.d(TAG, e.toString());
									}
								});
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(NewProjectActivity.this, "Failed to access user", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}
}
