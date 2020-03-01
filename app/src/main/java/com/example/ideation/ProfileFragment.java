package com.example.ideation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
	private static final String TAG = "ProfileFragment";
	private View v;

	//Make variables
	private TextView emailView;
	private Button logoutButton;


	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//Assign the correct view to the fragment
		v = inflater.inflate(R.layout.fragment_profile, container, false);

		//Assign instance and user to variables
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		//Assign views to variables
		emailView = v.findViewById(R.id.emailText);
		logoutButton = v.findViewById(R.id.logoutButton);

		//Set an on click listener for the button
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Sign the user out and return to login
				onSignOut();
			}
		});

		updateFields();

		return v;
	}

	private void updateFields() {
		//Set the text fields
		emailView.setText(firebaseUser.getEmail());
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
}
