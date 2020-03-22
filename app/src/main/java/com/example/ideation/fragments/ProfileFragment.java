package com.example.ideation.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideation.activities.SignUpActivity;
import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.example.ideation.activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
	private static final String TAG = "ProfileFragment";
	private View v;

	//Make variables
	private TextView userNameField, firstNameField, lastNameField, emailField, verificationField;
	private Button logoutButton, resendVerificationButton;
	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;

	//Get Fire Store instance and store in db variable
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: In Profile Fragment");
		//Assign the correct view to the fragment
		v = inflater.inflate(R.layout.fragment_profile, container, false);

		//Assign instance and user to variables
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		//Assign views to variables
		userNameField = v.findViewById(R.id.userNameText);
		firstNameField = v.findViewById(R.id.firstNameText);
		lastNameField = v.findViewById(R.id.lastNameText);
		emailField = v.findViewById(R.id.emailText);
		verificationField = v.findViewById(R.id.verificationText);

		initialiseButtons();

		//Retrieve and assign user data to text fields
		retrieveUserData();

		return v;
	}

	private void onSignOut() {
		Log.d(TAG, "signOut: User Signed Out");
		//Sign out of the account
		firebaseAuth.signOut();
		
		//Kill the login activity and go to the application
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		getActivity().finish();
		startActivity(intent);
	}

	public void retrieveUserData() {
		//Retrieve information about the user from the database
		db.collection(IdeationContract.COLLECTION_USERS).document(firebaseUser.getUid()).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						if (documentSnapshot.exists()) {
							//Reload information about the user
							firebaseUser.reload();

							//Set the text fields
							userNameField.setText(documentSnapshot.getString(IdeationContract.USER_USERNAME));
							firstNameField.setText(documentSnapshot.getString(IdeationContract.USER_FIRSTNAME));
							lastNameField.setText(documentSnapshot.getString(IdeationContract.USER_LASTNAME));
							emailField.setText(firebaseUser.getEmail());

							//Check if the user is verified
							checkVerification();

						} else {
							Toast.makeText(getActivity(), "Document does not exist", Toast.LENGTH_SHORT).show();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}

	private void initialiseButtons() {
		//Assign view buttons to variables
		logoutButton = v.findViewById(R.id.logoutButton);
		resendVerificationButton = v.findViewById(R.id.resendVerificationButton);

		//Set an on click listener for the button
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Sign the user out and return to login
				onSignOut();
			}
		});

		//Set an on click listener for the button
		resendVerificationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Resend email verification
				resendEmailVerification();
			}
		});
	}

	private void resendEmailVerification() {
		//Send the mail verification
		firebaseUser.sendEmailVerification()
				.addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Toast.makeText(getContext(), "Verification email resent to " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
						} else {
							Log.e(TAG, "sendEmailVerification", task.getException());
							Toast.makeText(getContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	public void checkVerification() {
		Log.d(TAG, "checkVerification: Checking");
		//Check if the user is verified
		if (firebaseUser.isEmailVerified()){
			verificationField.setText("Verified");
		} else {
			verificationField.setText("Not Verified");
		}
	}
}
