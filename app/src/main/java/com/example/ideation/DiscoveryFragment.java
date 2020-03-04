package com.example.ideation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DiscoveryFragment extends Fragment {
	private static final String TAG = "MyProjectsFragment";

	//Initialise Variables
	private View v;
	private RecyclerView recyclerView;
	private ProjectBoxAdapter adapter;
	private FirebaseAuth firebaseAuth;

	//Make an database instance and get collection reference
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private CollectionReference projectRef = db.collection(IdeationContract.PROJECTS_COLLECTION);

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: In Discovery Fragment");
		//Assign the correct view to the fragment
		v = inflater.inflate(R.layout.fragment_discovery, container, false);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Find the view and assign to member variable
		recyclerView = v.findViewById(R.id.projectsRecyclerView);

		//Populate the recycler view
		populateRecyclerView();

		return v;
	}

	private void populateRecyclerView() {
		//Add query filer below (priority, by date, etc..)
		Query query = projectRef;

		//Query the database and build
		FirestoreRecyclerOptions<ProjectBox> options = new FirestoreRecyclerOptions.Builder<ProjectBox>()
				.setQuery(query, ProjectBox.class)
				.build();

		//Assign project box adapter to variable
		adapter = new ProjectBoxAdapter(options);

		//Set adapter attributes and start
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//Start listening for database updates
		adapter.startListening();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//Stop listening for database updates
		adapter.stopListening();
	}
}
