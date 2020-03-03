package com.example.ideation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
	private Button logoutButton;
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
		logoutButton = v.findViewById(R.id.logoutButton);

		//Set an on click listener for the button
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Sign the user out and return to login
				onSignOut();
			}
		});

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
		db.collection(IdeationContract.USERS_COLLECTION).document(firebaseUser.getUid()).get()
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
							if (firebaseUser.isEmailVerified()){
								verificationField.setText("Verified");
							} else {
								verificationField.setText("Not Verified");
							}

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
}
