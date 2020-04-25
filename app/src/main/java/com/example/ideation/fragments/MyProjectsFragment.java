package com.example.ideation.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ideation.dialogs.AccessRequestsDialog;
import com.example.ideation.database.IdeationContract;
import com.example.ideation.recycler.ProjectBox;
import com.example.ideation.recycler.ProjectBoxAdapter;
import com.example.ideation.R;
import com.example.ideation.activities.NewProjectActivity;
import com.example.ideation.activities.ViewProjectActivity;
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
	private TextView emptyViewField;
	private Button accessRequestsButton;
	private boolean resultFlag;
	private RecyclerView recyclerView;
	private ProjectBoxAdapter adapter;
	private FirebaseAuth firebaseAuth;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: In the my projects fragment");
		//Assign the correct view to the fragment
		View v = inflater.inflate(R.layout.fragment_my_projects, container, false);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Find the view and assign to member variable
		newProjectButton = v.findViewById(R.id.newProject);
		accessRequestsButton = v.findViewById(R.id.openAccessRequests);
		emptyViewField = v.findViewById(R.id.emptyView);
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
		String userUID = firebaseAuth.getUid();

		//Get collection reference and add query filer below (priority, by date, etc..)
		CollectionReference projectRef = db.collection(IdeationContract.COLLECTION_PROJECTS);
		Query query = projectRef.whereEqualTo(IdeationContract.PROJECT_OWNERUID, userUID).whereEqualTo(IdeationContract.PROJECT_ARCHIVED, IdeationContract.FALSE);

		//Query the database and build
		FirestoreRecyclerOptions<ProjectBox> options = new FirestoreRecyclerOptions.Builder<ProjectBox>()
				.setLifecycleOwner(this)
				.setQuery(query, ProjectBox.class)
				.build();

		//Assign project box adapter to variable
		adapter = new ProjectBoxAdapter(options);

		//Handle the view (check if empty and use place holder)
		handleRecyclerView(adapter);

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
				//Show validation dialog - Delete if confirmed and restore if not
				archiveIfValidated(viewHolder);
			}
		}).attachToRecyclerView(recyclerView);
	}

	private void archiveIfValidated(RecyclerView.ViewHolder viewHolder) {
		//Make the view holder final so it can be called in an inner class
		final RecyclerView.ViewHolder finalViewHolder = viewHolder;

		//Initialise the alert dialog builder
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

		//Set the builder view and customise
		alertDialogBuilder.setTitle("Are you sure?").setMessage("This project will be archived permanently and cannot be restored.")
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//Bring the holder back if the user changes their mind
						adapter.notifyItemChanged(finalViewHolder.getAdapterPosition());
					}
				})
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//If the deletion is confirmed the delete the project
						adapter.archiveProject(finalViewHolder.getAdapterPosition());
					}
				});

		// Create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// Show it
		alertDialog.show();
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

	private void handleRecyclerView (final ProjectBoxAdapter adapter) {
		//Set default flag to false
		resultFlag = false;

		//Check if there are any results if yes show them and hide the no results text
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			public void onItemRangeInserted(int positionStart, int itemCount) {
				int totalNumberOfItems = adapter.getItemCount();

				//If there are results update view and update flag
				if(totalNumberOfItems >= 1) {
					recyclerView.setVisibility(View.VISIBLE);
					emptyViewField.setVisibility(View.GONE);

					resultFlag = true;
				}
			}

			public void onItemRangeRemoved(int positionStart, int itemCount) {
				int totalNumberOfItems = adapter.getItemCount();

				//If there are results update view and update flag
				if (totalNumberOfItems >= 1) {
					recyclerView.setVisibility(View.VISIBLE);
					emptyViewField.setVisibility(View.GONE);

					resultFlag = true;
				} else {
					//Set the default view to no results
					recyclerView.setVisibility(View.GONE);
					emptyViewField.setVisibility(View.VISIBLE);
				}
			}
		});

		//Do a result check but wait for results to load and flag to change if flag is false
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				//If there are no results
				if(!resultFlag) {
					//Set the default view to no results
					recyclerView.setVisibility(View.GONE);
					emptyViewField.setVisibility(View.VISIBLE);
				}
			}
		}, 500);
	}
}
