package com.example.ideation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
	private static final String TAG = "SignUpActivity";

	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private EditText emailField;
	private EditText passwordField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign the views to object
		emailField = findViewById(R.id.newEmailText);
		passwordField = findViewById(R.id.newPasswordText);
	}

	public void onCreateAccount(View v) {
		//Retrieve the strings
		String emailText = emailField.getText().toString();
		String passwordText = passwordField.getText().toString();

		//Make sure strings are not empty (causing an issue)
		if(emailText.equals("") || passwordText.equals("")) {
			emailText = "empty";
			passwordText = "empty";
		}

		//Create the account
		createAccount(emailText, passwordText);
	}

	public void createAccount(String email, String password) {
		Log.d(TAG, "createAccount: Creating account");

		firebaseAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign up success
							Log.d(TAG, "createUserWithEmail:success");

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
}
