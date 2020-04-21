package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class SignatureActivity extends AppCompatActivity {
	private static final String TAG = "SignatureActivity";
	private int STORAGE_PERMISSION_CODE = 1;

	//Make variables
	String projectUID, requestUID, fileName;
	private TextView titleField, confirmPasswordFailedField;
	private EditText confirmPassword;
	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;
	private DownloadManager.Request request;
	private BroadcastReceiver onDownloadComplete = null;
	private Button acceptButton;
	private byte[] pdfInBytes = null;

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
		acceptButton = findViewById(R.id.acceptButton);
		confirmPasswordFailedField = findViewById(R.id.confirmPasswordFailedText);
		confirmPassword = findViewById(R.id.confirmPasswordText);

		//Get the project information
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
						fileName = projectTitle + "NDAForm";

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

	public void checkPermissionsBeforeDownload(View v) {
		//If the phone has given the application storage permission the continue in not request
		if (ContextCompat.checkSelfPermission(SignatureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			//Download file and navigate user to downloads
			downloadAndNavigate();
		} else {
			//Prompt a request for access
			requestStoragePermission();
		}
	}

	public void onSignAndAccept(View v) {
		//Get the confirmed password attempt as a string
		String confirmPasswordText = confirmPassword.getText().toString();

		//Make sure strings are not empty (causing an issue)
		if (confirmPasswordText.equals("")) {
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

							//Get the user UID
							final String userUID = firebaseUser.getUid();

							//Access user records
							db.collection(IdeationContract.COLLECTION_USERS).document(userUID)
									.get()
									.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
										@Override
										public void onSuccess(DocumentSnapshot documentSnapshot) {
											//Get the user public key encoded string
											String encodedPublicKeyString = documentSnapshot.getString(IdeationContract.USER_PUBLIC_KEY);
											//Decode string back into bytes
											byte[] encodedPublicKey = Base64.decode(encodedPublicKeyString, Base64.NO_WRAP);

											//Hash the NDA file, then sign it, and then verify it...
											byte[] hashedNDAFile = hashNDAFile();
											byte[] signedNDAFile = signNDA(userUID, hashedNDAFile);
											String encodedSignature = verifySignedNDAFile(encodedPublicKey, hashedNDAFile, signedNDAFile);

											//If it fails then let the user know and don't process request
											if (encodedSignature.equals("Verification Failed")) {
												confirmPasswordFailedField.setText("Digital signature failed");
											} else {
												//If the password was good then give the user access to the project
												addUserToProjectWhitelist(encodedSignature);

												//Finish activity and return
												finish();
											}
										}
									});
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
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_SIGNATURE_DENIED,
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

	private void addUserToProjectWhitelist(String encodedSignature) {
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
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_REQUEST_ACCEPTED, IdeationContract.PROJECT_REQUESTS_SIGNATURE, encodedSignature)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request accepted and status updated to request accepted and signature added");
					}
				});
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
							ActivityCompat.requestPermissions(SignatureActivity.this,
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

	private void downloadAndNavigate() {
		//Initialise a download manager
		final DownloadManager downloadManager = (DownloadManager) SignatureActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

		//Enqueue the download request and navigate user to downloads
		if (request != null) {
			final long downloadID = downloadManager.enqueue(request);

			//Create a broadcast receiver to receive when downland is done
			BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
				@RequiresApi(api = Build.VERSION_CODES.O)

				//Do the following when the download is done
				public void onReceive(Context context, Intent intent) {
					//Take the user to the downloads page
					startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));

					//Create the file directory
					String fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName + ".pdf";

					//Parse the directory and create a path
					Uri uri = Uri.parse(fileDirectory);
					Path filePath = Paths.get(uri.getPath());

					try {
						//Convert the file into bytes and store in variable
						pdfInBytes = Files.readAllBytes(filePath);

						//Enable the button and allow them to agree to the NDA
						acceptButton.setEnabled(true);
						acceptButton.setTextColor(Color.parseColor("#00ccff"));

						//Check if the bytes give back the PDF
						//checkBytesToPDF(pdfInBytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			//Register to receiver and wait until the download is complete
			registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		}
	}

	public byte[] hashNDAFile() {
		//Initialise digest
		MessageDigest digest = null;

		//Get the SHA512 message digest instance
		try {
			digest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		//Make sure there are bytes to hash
		if (pdfInBytes != null) {
			//Apply the digest to the data to hash the data
			digest.update(pdfInBytes);
			byte[] hashedNDAFile = digest.digest();

			Toast.makeText(SignatureActivity.this, "Good bytes", Toast.LENGTH_SHORT).show();

			//Return hashed NDA file
			return hashedNDAFile;
		} else {
			//If there are no bytes then return null
			Toast.makeText(SignatureActivity.this, "No bytes", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	public byte[] signNDA(String signerUID, byte[] hashedNDAFile) {
		//Initialise signature
		byte[] signedNDAFile = null;

		try {
			//Get the keystore instance
			KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
			ks.load(null);

			//SIGN
			//Get the private key from Key Store and store in variable
			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(signerUID, null);  //null if you don't have key locked up with password
			PrivateKey privateKey = privateKeyEntry.getPrivateKey();

			//Initialise a signature and get SHA512 with RSA instance
			Signature signature = Signature.getInstance("SHA512withRSA");
			//Add the private key to the signature
			signature.initSign(privateKey);
			//Add the hashed data to the signature
			signature.update(hashedNDAFile);
			//Execute the signature
			signedNDAFile = signature.sign();

		} catch (SignatureException | UnrecoverableEntryException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | KeyStoreException | IOException e) {
			e.printStackTrace();
		}

		//Return the signature
		return signedNDAFile;
	}

	public String verifySignedNDAFile(byte[] encodedPublicKey, byte[] hashedNDAFile, byte[] signedNDA) {
		//Initialise variables
		boolean verified = false;
		String encodedSignature = null;

		try {
			//GET THE USERS PUBLIC KEY
			//Takes your byte array of the key as constructor parameter
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			//Takes algorithm used (RSA) to generate keys
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			//Creates a new PublicKey object
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			//VERIFY THE SIGNATURE
			//Initialise a signature and get SHA512 with RSA instance
			Signature verificationSignature = Signature.getInstance("SHA512withRSA");
			//Add the public key
			verificationSignature.initVerify(publicKey);
			//Add the hashed data to the signature
			verificationSignature.update(hashedNDAFile);
			//Check if its verified
			verified = verificationSignature.verify(signedNDA);

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		//If it verifies return the encoded signature to store in request document else return fail
		if (verified) {
			Log.d(TAG, "verifiedDeviceSignature: Verified");
			Toast.makeText(SignatureActivity.this, "VERIFIED", Toast.LENGTH_SHORT).show();

			//Encode the signature so it can be stored and return
			encodedSignature = new String(Base64.encode(signedNDA, 2));
			return encodedSignature;
		} else {
			Log.d(TAG, "verifiedDeviceSignature: Not Verified");
			Toast.makeText(SignatureActivity.this, "FAILED", Toast.LENGTH_SHORT).show();
			return "Verification Failed";
		}
	}

	private void checkBytesToPDF(byte[] pdfInBytes) throws IOException {
		//Chose file name and location
		File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + "BytesToNDA" + ".pdf");

		//Use the bytes and to populate the file
		OutputStream out = new FileOutputStream(outFile);
		out.write(pdfInBytes);
		out.close();
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (onDownloadComplete != null) {
			unregisterReceiver(onDownloadComplete);
		}
	}
}
