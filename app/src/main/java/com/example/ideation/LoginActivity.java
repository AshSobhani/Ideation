package com.example.ideation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";

	private FirebaseAuth mAuth;
	private ProgressBar loginProgressBar;
	private EditText emailField;
	private EditText passwordField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();

		//Assign the views to object
		loginProgressBar = findViewById(R.id.loginProgressBar);
		emailField = findViewById(R.id.emailText);
		passwordField = findViewById(R.id.passwordText);

		hideProgressBar();
	}

	private void signIn(String email, String password) {
		Log.d(TAG, "signIn: " + email);
//		if (!validateForm()) {
//			return;
//		}

		showProgressBar();

		// Sign in with email and password
		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success
							Log.d(TAG, "signInWithEmail:success");

							enterApp();

						} else {
							// Sign in failed
							Log.w(TAG, "signInWithEmail:failure", task.getException());
							Toast.makeText(LoginActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
						}

						//If Login fails then let the firebaseUser know
						if (!task.isSuccessful()) {
							//mStatusTextView.setText(R.string.auth_failed);
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

		//Sign in
		signIn(email, password);
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
		//// Check if firebaseUser is signed in (non-null) and update UI accordingly.
		//FirebaseUser currentUser = mAuth.getCurrentUser();
		//updateUI(currentUser);
	}

	@Override
	public void onStop() {
		super.onStop();
		hideProgressBar();
	}

}
