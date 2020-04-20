package com.example.ideation.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.ideation.dialogs.DeniedDialog;
import com.example.ideation.database.IdeationContract;
import com.example.ideation.recycler.ProjectBox;
import com.example.ideation.recycler.ProjectBoxAdapter;
import com.example.ideation.R;
import com.example.ideation.activities.ViewProjectActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DiscoveryFragment extends Fragment {
	private static final String TAG = "MyProjectsFragment";

	//Initialise Variables
	private View v;
	private boolean resultFlag;
	private TextView emptyViewField;
	private TextInputLayout searchTextFieldLayout;
	private RecyclerView recyclerView;
	private ProjectBoxAdapter adapter;
	private FirebaseAuth firebaseAuth;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
		emptyViewField = v.findViewById(R.id.emptyView);
		searchTextFieldLayout = v.findViewById(R.id.searchTextLayout);

		//Update recycler view with filter and hide keyboard
		searchTextFieldLayout.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				//Hide the keyboard
				InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}

				//Add filter to recycler view and hide recycler view
				recyclerView.setVisibility(View.INVISIBLE);
				populateRecyclerView(v.getText().toString().toLowerCase());
				return false;
			}
		});

		//Reset filter and clear text box
		searchTextFieldLayout.setEndIconOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Load projects with no filter
				populateRecyclerView("");

				//Clear the text box
				searchTextFieldLayout.getEditText().setText("");
			}
		});

		if(savedInstanceState != null) {
			//Restore current search and load projects with filter
			String currentSearch = savedInstanceState.getString("currentSearch");
			populateRecyclerView(currentSearch);
		} else {
			//Load projects with no filter
			populateRecyclerView("");
		}

		return v;
	}

	private void populateRecyclerView(String searchText) {
		//Create the project collection reference, and if needed add query filer below (priority, by date, etc..)
		Query query = db.collection(IdeationContract.COLLECTION_PROJECTS);

		//If the search is empty then order by date created
		if (!searchText.equals("")) {
			query = query.orderBy(IdeationContract.PROJECT_TITLE_SEARCH).startAt(searchText).endAt(searchText + "\uf8ff");
		} else {
			query = query.orderBy(IdeationContract.PROJECT_DATE_CREATED, Query.Direction.DESCENDING);
		}

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
		db.collection(IdeationContract.COLLECTION_PROJECTS).document(projectUID).get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Retrieve the ownerUID and whitelist from database
						String ownerUID = documentSnapshot.getString(IdeationContract.PROJECT_OWNERUID);
						List<String> whitelist = (List<String>) documentSnapshot.get(IdeationContract.PROJECT_WHITELIST);

						//If the user is in the whitelist or if its the owner let them in, otherwise all the to request access
						if (whitelist.contains(firebaseAuth.getUid()) || ownerUID.equals(firebaseAuth.getUid())) {
							//Put the projectUID into a new bundle
							Bundle bundle = new Bundle();
							bundle.putString("projectUID", projectUID);

							//Create an intent and start view project activity whilst also sending the bundle
							Intent intent = new Intent(getContext(), ViewProjectActivity.class);
							intent.putExtras(bundle);
							startActivity(intent);
						} else {
							Log.d(TAG, "Project access denied");

							//Open dialog notifying user they don't have access to the project
							openDeniedDialog(projectUID);
						}
					}
				});
	}

	private void openDeniedDialog(String projectUID) {
		//Put the projectUID into a new bundle
		Bundle bundle = new Bundle();
		bundle.putString("projectUID", projectUID);

		//Make an instance of our access dialog and pass through our bundle before showing
		DeniedDialog deniedDialog = new DeniedDialog();
		deniedDialog.setArguments(bundle);
		deniedDialog.show(getParentFragmentManager(), "Denied Dialog");
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
		}, 750);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		//Save current search
		outState.putString("currentSearch", searchTextFieldLayout.getEditText().getText().toString().toLowerCase());
		super.onSaveInstanceState(outState);
	}
}
