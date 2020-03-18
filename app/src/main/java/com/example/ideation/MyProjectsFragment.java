package com.example.ideation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
	private FloatingActionButton newProjectButton;
	private Button accessRequestsButton;
	private RecyclerView recyclerView;
	private ProjectBoxAdapter adapter;
	private FirebaseAuth firebaseAuth;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: In My Projects Fragment");
		//Assign the correct view to the fragment
		View v = inflater.inflate(R.layout.fragment_my_projects, container, false);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Find the view and assign to member variable
		newProjectButton = v.findViewById(R.id.newProject);
		accessRequestsButton = v.findViewById(R.id.openAccessRequests);
		recyclerView = v.findViewById(R.id.projectsRecyclerView);

		//Set an on click listener for the button
		newProjectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Start Tracking activity and service
				onNewProject();
			}
		});

		//Set an on click listener for the button
		accessRequestsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Start Tracking activity and service
				openAccessRequestsDialog();
			}
		});

		//Populate the recycler view
		populateRecyclerView();

		return v;
	}

	private void populateRecyclerView() {
		//Get the users unique ID
		String UID = firebaseAuth.getUid();

		//Get collection reference and add query filer below (priority, by date, etc..)
		CollectionReference projectRef = db.collection(IdeationContract.COLLECTION_PROJECTS);
		Query query = projectRef.whereArrayContains(IdeationContract.PROJECT_WHITELIST, UID);

		//Query the database and build
		FirestoreRecyclerOptions<ProjectBox> options = new FirestoreRecyclerOptions.Builder<ProjectBox>()
				.setLifecycleOwner(this)
				.setQuery(query, ProjectBox.class)
				.build();

		//Assign project box adapter to variable
		adapter = new ProjectBoxAdapter(options);

		//Set adapter attributes and start
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter);

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
	}

	private void openAccessRequestsDialog() {
		//Make an instance of our access dialog and show it
		AccessRequestsDialog accessRequestsDialog = new AccessRequestsDialog();
		accessRequestsDialog.show(getParentFragmentManager(), "Access Request Dialog");
	}

	private void onNewProject() {
		//Make intent and start activity
		Intent intent = new Intent(getContext(), NewProjectActivity.class);
		startActivity(intent);
	}
}
