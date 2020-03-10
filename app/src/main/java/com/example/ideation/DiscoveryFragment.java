package com.example.ideation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
	private CollectionReference projectRef = db.collection(IdeationContract.COLLECTION_PROJECTS);

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
		//Query query = projectRef.orderBy("name").startAt(searchText).endAt(searchText + "\uf8ff");

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

		//View Project - Detect when a project has been clicked and open that activity
		adapter.setOnBoxClickListener(new ProjectBoxAdapter.OnBoxClickListener() {
			@Override
			public void onBoxClick(DocumentSnapshot documentSnapshot, int position) {
				//Get owner UID
				final String projectUID = documentSnapshot.getId();

				accessProject(projectUID);
			}
		});
	}

	private void accessProject(final String projectUID) {
		//Does the user already have access to the project
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).collection(IdeationContract.COLLECTION_PROJECT_WHITELIST).document(firebaseAuth.getUid()).get()
				.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
					@Override
					public void onComplete(@NonNull Task<DocumentSnapshot> task) {
						if (task.isSuccessful()) {
							//Check if the user exists in the project access collection before giving access
							DocumentSnapshot document = task.getResult();
							if (document.exists()) {
								//Put the projectUID into a new bundle
								Bundle bundle = new Bundle();
								bundle.putString("projectUID", projectUID);

								//Create an intent and start view project activity whilst also sending the bundle
								Intent intent = new Intent(getContext(), ViewProjectActivity.class);
								intent.putExtras(bundle);
								startActivity(intent);
							} else {
								Log.d(TAG, "Document does not exist!");
							}
						}
					}
				});

		//Is the user verified?
		//If an NDA form exists has the user signed it?

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
