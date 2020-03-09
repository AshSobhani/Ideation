package com.example.ideation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
	private static final String TAG = "SignUpActivity";

	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private EditText firstNameField, lastNameField, userNameField, emailField, passwordField;
	private TextView signUpTextField;
	private String emailText, passwordText, firstNameText, lastNameText, userNameText;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign the views to object
		firstNameField = findViewById(R.id.firstNameText);
		lastNameField = findViewById(R.id.lastNameText);
		userNameField = findViewById(R.id.userNameText);
		emailField = findViewById(R.id.emailText);
		passwordField = findViewById(R.id.newPasswordText);
		signUpTextField = findViewById(R.id.signUpFailedText);
	}

	public void onCreateAccount(View v) {
		//Retrieve the strings
		emailText = emailField.getText().toString();
		passwordText = passwordField.getText().toString();
		firstNameText = firstNameField.getText().toString();
		lastNameText = lastNameField.getText().toString();
		userNameText = userNameField.getText().toString();

		//If fields are not empty, try to create the account
		if(!emailText.equals("") && !passwordText.equals("") && !firstNameText.equals("") && !lastNameText.equals("") && !userNameText.equals("")) {
			//Create the account
			createAccount(emailText, passwordText);
		}
		else {
			signUpTextField.setText("Please fill out all the empty fields.");
		}
	}

	public void createAccount(String email, String password) {
		Log.d(TAG, "createAccount: Creating account");

		//If email and password comply with expected format create the user
		firebaseAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign up success
							Log.d(TAG, "createUserWithEmail:success");

							//Add user to users collection
							addUserToCollection();

							//Email for verification
							sendEmailVerification();

							//Sign out of the account
							firebaseAuth.signOut();

							//Finish activity and return to login activity
							finish();

						} else {
							// Sign up failure
							Log.w(TAG, "createUserWithEmail:failure", task.getException());
							Toast.makeText(SignUpActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	private void sendEmailVerification() {
		//Get the current user
		final FirebaseUser user = firebaseAuth.getCurrentUser();

		//Send the mail verification
		user.sendEmailVerification()
				.addOnCompleteListener(this, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Toast.makeText(SignUpActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
						} else {
							Log.e(TAG, "sendEmailVerification", task.getException());
							Toast.makeText(SignUpActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	private void addUserToCollection() {
		//Get the users unique ID
		String UID = firebaseAuth.getUid();

		//Create a hash to store the data before inserting into firebase
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put(IdeationContract.USER_FIRSTNAME, firstNameText);
		userInfo.put(IdeationContract.USER_LASTNAME, lastNameText);
		userInfo.put(IdeationContract.USER_USERNAME, userNameText);

		//Insert user into users collection
		db.collection(IdeationContract.COLLECTION_USERS).document(UID).set(userInfo)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Toast.makeText(SignUpActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(SignUpActivity.this, "Error!", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}
}
