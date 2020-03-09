package com.example.ideation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";

	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private ProgressBar loginProgressBar;
	private EditText emailField, passwordField;
	private TextView loginFailedField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign the views to object
		loginProgressBar = findViewById(R.id.loginProgressBar);
		emailField = findViewById(R.id.emailText);
		passwordField = findViewById(R.id.newPasswordText);
		loginFailedField = findViewById(R.id.loginFailedText);

		hideProgressBar();
	}

	private void signIn(String email, String password) {
		Log.d(TAG, "signIn as: " + email);

		showProgressBar();

		// Sign in with email and password
		firebaseAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success
							Log.d(TAG, "signInWithEmail:success");

							enterApp();

						} else {
							// Sign in failed - prompt the user
							Log.w(TAG, "signInWithEmail:failure", task.getException());
							loginFailedField.setText("Incorrect username or password.");
						}

						hideProgressBar();
					}
				});
	}

	public void enterApp() {
		//Kill the login activity and go to the application
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		finish();
		startActivity(intent);
	}

	public void onSignIn(View v) {
		//Get the text
		String email = emailField.getText().toString();
		String password = passwordField.getText().toString();

		//Make sure strings are not empty (causing an issue)
		if(email.equals("") || password.equals("")) {
			email = "empty";
			password = "empty";
		}

		//Sign in
		signIn(email, password);
	}

	public void onSignUp(View v) {
		//Start the sign up activity
		Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
		startActivity(intent);
	}

	public void showProgressBar() {
		if (loginProgressBar != null) {
			loginProgressBar.setVisibility(View.VISIBLE);
		}
	}

	public void hideProgressBar() {
		if (loginProgressBar != null) {
			loginProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// Check if firebaseUser is signed in
		FirebaseUser currentUser = firebaseAuth.getCurrentUser();
		if (currentUser != null) {
			enterApp();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		hideProgressBar();
	}
}
