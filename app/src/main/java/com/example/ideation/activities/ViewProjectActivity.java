package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ViewProjectActivity extends AppCompatActivity {
	private static final String TAG = "ViewProjectActivity";
	private int STORAGE_PERMISSION_CODE = 1;

	//Make variables
	String projectUID;
	private TextView titleField, projectOwnerField;
	private EditText categoryField, descriptionField, problemSolutionField, revenueModelField, targetMarketField, currentProgressField;
	private FloatingActionButton editProjectButton, editDoneButton;
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
		projectOwnerField = findViewById(R.id.ownerAndDateText);
		categoryField = findViewById(R.id.categoryText);
		descriptionField = findViewById(R.id.descriptionText);
		problemSolutionField = findViewById(R.id.problemSolutionText);
		revenueModelField = findViewById(R.id.revenueModelText);
		targetMarketField = findViewById(R.id.targetMarketText);
		currentProgressField = findViewById(R.id.currentProgressText);
		editProjectButton = findViewById(R.id.editProjectButton);
		editDoneButton = findViewById(R.id.editDoneButton);
		downloadNDAButton = findViewById(R.id.downloadNDAButton);

		//Retrieve and assign user data to text fields
		retrieveProjectData();
	}

	public void onEditProject(View v) {
		Log.d(TAG, "onEditProject: Edit Mode");

		//Show save changes button and hide edit project button
		editProjectButton.setVisibility(View.INVISIBLE);
		editDoneButton.setVisibility(View.VISIBLE);
		//Make the text editable
		textEditMode(true);
	}

	public void onEditDone(View v) {
		Log.d(TAG, "onEditProject: Edit Mode");

		//Initialise the alert dialog builder
		android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);

		//Set the builder view and customise
		alertDialogBuilder.setTitle("Commit these changes?").setMessage("If you would like to discard changes select revert, alternately press back to continue editing.")
				.setNeutralButton("Back", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "onClick: Back to editing");
					}
				})
				.setNegativeButton("Revert", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Show save changes button and hide edit project button
						editProjectButton.setVisibility(View.VISIBLE);
						editDoneButton.setVisibility(View.INVISIBLE);
						//Make the text uneditable
						textEditMode(false);

						//Reload data fields
						retrieveProjectData();
					}
				})
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Show save changes button and hide edit project button
						editProjectButton.setVisibility(View.VISIBLE);
						editDoneButton.setVisibility(View.INVISIBLE);
						//Make the text uneditable
						textEditMode(false);

						//Create a hash to store the data before inserting into firebase
						Map<String, Object> editData = new HashMap<>();
						editData.put(IdeationContract.PROJECT_CATEGORY, categoryField.getText().toString());
						editData.put(IdeationContract.PROJECT_DESCRIPTION, descriptionField.getText().toString());
						editData.put(IdeationContract.PROJECT_PROBLEM_SOLUTION, problemSolutionField.getText().toString());
						editData.put(IdeationContract.PROJECT_REVENUE_MODEL, revenueModelField.getText().toString());
						editData.put(IdeationContract.PROJECT_TARGET_MARKET, targetMarketField.getText().toString());
						editData.put(IdeationContract.PROJECT_CURRENT_PROGRESS, currentProgressField.getText().toString());

						//Insert project into project collection
						db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).update(editData)
								.addOnSuccessListener(new OnSuccessListener<Void>() {
									@Override
									public void onSuccess(Void aVoid) {
										Log.d(TAG, "onSuccess: Project updated");
									}
								});
					}
				});

		// Create alert dialog and show it
		android.app.AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void textEditMode(Boolean editable) {
		//Make the text fields either editable or not editable
		categoryField.setEnabled(editable);
		categoryField.setFocusable(editable);
		categoryField.setClickable(editable);
		categoryField.setFocusableInTouchMode(editable);
		descriptionField.setEnabled(editable);
		descriptionField.setFocusable(editable);
		descriptionField.setClickable(editable);
		descriptionField.setFocusableInTouchMode(editable);
		problemSolutionField.setEnabled(editable);
		problemSolutionField.setFocusable(editable);
		problemSolutionField.setClickable(editable);
		problemSolutionField.setFocusableInTouchMode(editable);
		revenueModelField.setEnabled(editable);
		revenueModelField.setFocusable(editable);
		revenueModelField.setClickable(editable);
		revenueModelField.setFocusableInTouchMode(editable);
		targetMarketField.setEnabled(editable);
		targetMarketField.setFocusable(editable);
		targetMarketField.setClickable(editable);
		targetMarketField.setFocusableInTouchMode(editable);
		currentProgressField.setEnabled(editable);
		currentProgressField.setFocusable(editable);
		currentProgressField.setClickable(editable);
		currentProgressField.setFocusableInTouchMode(editable);
	}

	private void retrieveProjectData() {
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Get the owner ID to check if the viewer owns the project
						String ownerUID = documentSnapshot.getString(IdeationContract.PROJECT_OWNERUID);

						//If its the owner allow them to edit the project
						if (ownerUID.equals(firebaseUser.getUid())) {
							editProjectButton.setVisibility(View.VISIBLE);
						}

						//Get the project title and assign to final variable
						final String projectTitle = documentSnapshot.getString(IdeationContract.PROJECT_TITLE);

						//Get the date
						Timestamp date = documentSnapshot.getTimestamp(IdeationContract.PROJECT_DATE_CREATED);
						//Change date format to desired format
						SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
						String formattedDateCreated = df.format(date.toDate());
						//Create the owner and date field
						String ownerAndDate = documentSnapshot.getString(IdeationContract.PROJECT_OWNERNAME) + " | " + formattedDateCreated;

						//Set the text fields
						titleField.setText(projectTitle);
						projectOwnerField.setText(ownerAndDate);
						categoryField.setText(documentSnapshot.getString(IdeationContract.PROJECT_CATEGORY));
						descriptionField.setText(documentSnapshot.getString(IdeationContract.PROJECT_DESCRIPTION));
						problemSolutionField.setText(documentSnapshot.getString(IdeationContract.PROJECT_PROBLEM_SOLUTION));
						revenueModelField.setText(documentSnapshot.getString(IdeationContract.PROJECT_REVENUE_MODEL));
						targetMarketField.setText(documentSnapshot.getString(IdeationContract.PROJECT_TARGET_MARKET));
						currentProgressField.setText(documentSnapshot.getString(IdeationContract.PROJECT_CURRENT_PROGRESS));

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
		//If the phone has given the application storage permission the continue in not request
		if (ContextCompat.checkSelfPermission(ViewProjectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			//Download file and navigate user to downloads
			downloadAndNavigate();
		} else {
			//Prompt a request for access
			requestStoragePermission();
		}
	}

	private void requestStoragePermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

			//Create a dialog explaining why
			new AlertDialog.Builder(this)
					.setTitle("Permission Required")
					.setMessage("Storage permission is required to download to your files.")
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(ViewProjectActivity.this,
									new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.create().show();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		//Return whether or not the permission was granted and act accordingly
		if (requestCode == STORAGE_PERMISSION_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();

				//Open file manager to select NDA
				downloadAndNavigate();
			} else {
				Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void downloadAndNavigate() {
		//Initialise a download manager
		DownloadManager downloadManager = (DownloadManager) ViewProjectActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

		//Enqueue the download request and navigate user to downloads
		if (request != null) {
			downloadManager.enqueue(request);
			startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
		}
	}
}
