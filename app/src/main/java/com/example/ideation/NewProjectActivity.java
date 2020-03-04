package com.example.ideation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
		if (!titleField.equals("") && !descriptionText.equals("") && !categoryText.equals("")){
			addProjectToCollection();
			finish();
		} else {
			Toast.makeText(NewProjectActivity.this, "Error: Empty Fields", Toast.LENGTH_SHORT).show();
		}

	}

	private void addProjectToCollection() {
		//Get the users unique ID
		String UID = firebaseAuth.getUid();

		//Get current date for date created text
		Date c = Calendar.getInstance().getTime();

		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String dateCreatedText = df.format(c);

		//Create a hash to store the data before inserting into firebase
		Map<String, Object> projectInfo = new HashMap<>();
		projectInfo.put(IdeationContract.PROJECT_OWNERUID, UID);
		projectInfo.put(IdeationContract.PROJECT_TITLE, titleText);
		projectInfo.put(IdeationContract.PROJECT_DESCRIPTION, descriptionText);
		projectInfo.put(IdeationContract.PROJECT_CATEGORY, categoryText);
		projectInfo.put(IdeationContract.PROJECT_DATE_CREATED, dateCreatedText);

		//Insert user into users collection
		db.collection(IdeationContract.PROJECTS_COLLECTION).document().set(projectInfo)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Toast.makeText(NewProjectActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(NewProjectActivity.this, "Error!", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}
}
