package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewProjectActivity extends AppCompatActivity {
	private static final String TAG = "NewProjectActivity";
	private int STORAGE_PERMISSION_CODE = 1;
	private static final int PICK_FILE_REQUEST = 1;

	//Initialise variables
	EditText titleField, descriptionField, categoryField;
	String titleText, descriptionText, categoryText;
	private TextView fileName;
	private ProgressBar progressBar;

	//Declare a URI for the PDF
	private Uri fileUri;

	//Initialise firebase auth then make a database and storage instance
	private FirebaseAuth firebaseAuth;
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseStorage storage = FirebaseStorage.getInstance();
	private StorageReference storageRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_project);

		//Get auth instance and storage reference
		firebaseAuth = FirebaseAuth.getInstance();
		storageRef = storage.getReference(IdeationContract.STORAGE_NDA_FORMS);

		//Assign views to variables
		titleField = findViewById(R.id.projectTitle);
		descriptionField = findViewById(R.id.projectDescription);
		categoryField = findViewById(R.id.projectCategory);
		fileName = findViewById(R.id.fileName);
		progressBar = findViewById(R.id.progress_bar);
	}

	public void onAddProject(View v) {
		//Retrieve the strings
		titleText = titleField.getText().toString();
		descriptionText = descriptionField.getText().toString();
		categoryText = categoryField.getText().toString();

		//Add project if fields are not empty and finish activity
		if (!titleField.equals("") && !descriptionText.equals("") && !categoryText.equals("")) {
			uploadFileAndAddProject();
			finish();
		} else {
			Toast.makeText(NewProjectActivity.this, "Error: Empty Fields", Toast.LENGTH_SHORT).show();
		}
	}

	public void uploadFileAndAddProject() {
		//If a file has been selected
		if (fileUri != null) {
			//Give the file its name and file extension
			final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
					+ "." + getFileExtension(fileUri));

			fileReference.putFile(fileUri)
					.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
						@Override
						public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
							Toast.makeText(NewProjectActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

							//Get the NDA form path and pass it to the add project function
							String NDAFormPath = taskSnapshot.getStorage().getPath();
							addProjectToCollection(NDAFormPath);
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(NewProjectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					})
					.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
						@Override
						public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
							//Show the upload progress on the progress bar
							double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
							progressBar.setProgress((int) progress);
						}
					});
		} else {
			addProjectToCollection(null);
			Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
		}
	}

	private void addProjectToCollection(final String NDAFormPath) {
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
						//Initiate the project white list
						projectInfo.put(IdeationContract.PROJECT_WHITELIST, Arrays.asList("Initiator"));

						//If an NDA form exists put the URL into the project
						if (NDAFormPath != null) {
							projectInfo.put(IdeationContract.PROJECT_NDA_PATH, NDAFormPath);
						}

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

	public void onChooseFile(View v) {
		//If the phone has given the application storage permission the continue in not request
		if (ContextCompat.checkSelfPermission(NewProjectActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(NewProjectActivity.this, "Permission Okay!",  Toast.LENGTH_SHORT).show();

			//Open file manager to select NDA
			openFileSelector();
		} else {
			//Prompt a request for access
			requestStoragePermission();
		}
	}

	private String getFileExtension(Uri uri) {
		//Get the file extension for so that firebase can identify it
		ContentResolver contentResolver = getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		return mime.getExtensionFromMimeType(contentResolver.getType(uri));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//If a file has been selected and its ok add it to the file uri variable
		if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK
				&& data != null && data.getData() != null) {
			fileUri = data.getData();

			Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);

			int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
			cursor.moveToFirst();
			fileName.setText(cursor.getString(nameIndex));
		}
	}

	private void requestStoragePermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.READ_EXTERNAL_STORAGE)) {

			//Create a dialog explaining why
			new AlertDialog.Builder(this)
					.setTitle("Permission Required")
					.setMessage("Storage permission is required to access your files.")
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(NewProjectActivity.this,
									new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		//Return whether or not the permission was granted and act accordingly
		if (requestCode == STORAGE_PERMISSION_CODE)  {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();

				//Open file manager to select NDA
				openFileSelector();
			} else {
				Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void openFileSelector() {
		//Create a new intent looking for PDF files and start it
		Intent intent = new Intent();
		intent.setType("application/pdf");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, PICK_FILE_REQUEST);
	}
}
