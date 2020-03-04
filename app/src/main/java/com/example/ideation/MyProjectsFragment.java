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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyProjectsFragment extends Fragment {
	private static final String TAG = "MyProjectsFragment";

	//Initialise Variables
	private View v;
	private FloatingActionButton newProjectButton;
	private RecyclerView recyclerView;
	private ProjectBoxAdapter adapter;
	private FirebaseAuth firebaseAuth;

	//Make an database instance and get collection reference
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private CollectionReference projectRef = db.collection(IdeationContract.COLLECTION_PROJECTS);

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: In My Projects Fragment");
		//Assign the correct view to the fragment
		v = inflater.inflate(R.layout.fragment_my_projects, container, false);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Find the view and assign to member variable
		newProjectButton = v.findViewById(R.id.newProject);
		recyclerView = v.findViewById(R.id.projectsRecyclerView);

		//Set an on click listener for the button
		newProjectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Start Tracking activity and service
				onNewProject();
			}
		});

		//Populate the recycler view
		populateRecyclerView();

		return v;
	}

	private void populateRecyclerView() {
		//Get the users unique ID
		String UID = firebaseAuth.getUid();

		//Add query filer below (priority, by date, etc..)
		Query query = projectRef.whereEqualTo(IdeationContract.PROJECT_OWNERUID, UID);

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

		//Delete Project - If a project box is swiped left delete the project
		new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
				adapter.deleteProject(viewHolder.getAdapterPosition());
			}
		}).attachToRecyclerView(recyclerView);

		//View Project - Detect when a project has been clicked and open that activity
		adapter.setOnBoxClickListener(new ProjectBoxAdapter.OnBoxClickListener() {
			@Override
			public void onBoxClick(DocumentSnapshot documentSnapshot, int position) {
				//Get owner UID and put it into a new bundle
				String projectUID = documentSnapshot.getId();
				Bundle bundle = new Bundle();
				bundle.putString("projectUID", projectUID);

				//Create an intent and start view project activity whilst also sending the bundle
				Intent intent = new Intent(getContext(), ViewProjectActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void onNewProject() {
		//Make intent and start activity
		Intent intent = new Intent(getContext(), NewProjectActivity.class);
		startActivity(intent);
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
