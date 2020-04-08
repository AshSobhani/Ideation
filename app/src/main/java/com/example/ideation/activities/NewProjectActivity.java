package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewProjectActivity extends AppCompatActivity {
	private static final String TAG = "NewProjectActivity";
	private static final int PICK_PDF_REQUEST = 1;

	//Initialise variables
	EditText titleField, descriptionField, categoryField;
	String titleText, descriptionText, categoryText;
	private Button buttonChoosePdf;
	private Button buttonUpload;
	private TextView textViewShowUpload;
	private EditText pdfFileName;
	private ProgressBar progressBar;

	//Declare a URI for the PDF
	private Uri pdfUri;

	//Make an database instance
	private FirebaseAuth firebaseAuth;
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseStorage storage = FirebaseStorage.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_project);

		//Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign views to variables
		titleField = findViewById(R.id.projectTitle);
		descriptionField = findViewById(R.id.projectDescription);
		categoryField = findViewById(R.id.projectCategory);
		//Upload Views
		buttonChoosePdf = findViewById(R.id.button_choose_image);
		buttonUpload = findViewById(R.id.button_upload);
		textViewShowUpload = findViewById(R.id.text_view_show_upload);
		pdfFileName = findViewById(R.id.edit_text_file_name);
		progressBar = findViewById(R.id.progress_bar);
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

	public void onChoosePdf (View v) {
		Intent intent = new Intent();
		intent.setType("application/pdf");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, PICK_PDF_REQUEST);
	}

	private void openFileChooser() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK
				&& data != null && data.getData() != null) {
			pdfUri = data.getData();

			//Create a cursor to retrieve the name and get the name index
			Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null);
			int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
			cursor.moveToFirst();

			//Set the upload text to the PDF file name and close the cursor
			textViewShowUpload.setText(cursor.getString(nameIndex));
			cursor.close();
		}
	}
}
