package com.example.ideation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MyProjectsFragment extends Fragment {
	private static final String TAG = "MyProjectsFragment";
	private View v;

	private FloatingActionButton newProjectButton;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//Assign the correct view to the fragment
		v = inflater.inflate(R.layout.fragment_myprojects, container, false);

		//Find the button and assign to member variable
		newProjectButton = v.findViewById(R.id.newProject);

		//Set an on click listener for the button
		newProjectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Start Tracking activity and service
				onNewProject();
			}
		});


		return v;
	}

	public void onNewProject() {
		//Make intent and start activity
		Intent intent = new Intent(getContext(), NewProjectActivity.class);
		startActivity(intent);
	}
}
