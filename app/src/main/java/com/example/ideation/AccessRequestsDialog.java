package com.example.ideation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AccessRequestsDialog extends AppCompatDialogFragment {
	private static final String TAG = "AccessRequestsDialog";
	
	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private RequestBoxAdapter adapter;
	private RecyclerView recyclerView;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateDialog: Creating request access dialog");
		//Get an instance of the dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		//Get the layout inflater and assign to variable to then find our layout dialog
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.layout_access_requests_dialog, null);

		// Get Firebase Auth instance and assign to variable
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign views to variables
		recyclerView = v.findViewById(R.id.requestsRecyclerView);

		//Set the builder view and customise
		builder.setView(v)
				.setTitle("Access Requests")
				.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Log.d(TAG, "onClick: Recycler stop listening");
						
					}
				});

		//Populate the recycler view
		populateRecyclerView();

		return builder.create();
	}

	private void populateRecyclerView() {
		//Get current ownerUID
		String ownerUID = firebaseAuth.getUid();

		//Create the project collection query, and if needed add query filer below (priority, by date, etc..)
		Query query = db.collectionGroup(IdeationContract.COLLECTION_PROJECT_REQUESTS)
				.whereEqualTo(IdeationContract.PROJECT_REQUESTS_OWNERUID, ownerUID)
				.whereEqualTo(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_ACCESS_REQUESTED);

		//Query the database and build
		FirestoreRecyclerOptions<RequestBox> options = new FirestoreRecyclerOptions.Builder<RequestBox>()
				.setLifecycleOwner(this)
				.setQuery(query, RequestBox.class)
				.build();

		//Assign project box adapter to variable
		adapter = new RequestBoxAdapter(options);

		//Set adapter attributes and start
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
		recyclerView.setAdapter(adapter);

		//View Project - Detect when a project has been clicked and open that activity
		adapter.setOnBoxClickListener(new RequestBoxAdapter.OnBoxClickListener() {
			@Override
			public void onBoxClick(DocumentSnapshot documentSnapshot, int position) {
				//Get owner UID
				final String projectUID = documentSnapshot.getId();

				//accessProject(projectUID);
			}
		});
	}
}
