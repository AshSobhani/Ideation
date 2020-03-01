package com.example.ideation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
	private static final String TAG = "ProfileFragment";
	private View v;

	//Make objects
	private TextView emailView;

	private FirebaseUser firebaseUser;
	private FirebaseAuth firebaseAuth;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//Assign the correct view to the fragment
		v = inflater.inflate(R.layout.fragment_profile, container, false);


		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		//Assign view to object
		emailView = v.findViewById(R.id.emailText);

		emailView.setText(firebaseUser.getEmail());

		return v;
	}
}
