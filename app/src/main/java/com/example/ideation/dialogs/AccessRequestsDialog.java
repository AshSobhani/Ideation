package com.example.ideation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.example.ideation.recycler.ProjectBoxAdapter;
import com.example.ideation.recycler.RequestBox;
import com.example.ideation.recycler.RequestBoxAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
	private boolean resultFlag;
	private TextView emptyViewField;

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
		emptyViewField = v.findViewById(R.id.emptyView);

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
		Query query = db.collectionGroup(IdeationContract.COLLECTION_ACCESS_REQUESTS)
				.whereEqualTo(IdeationContract.PROJECT_REQUESTS_OWNERUID, ownerUID)
				.whereEqualTo(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_ACCESS_REQUESTED);

		//Query the database and build
		FirestoreRecyclerOptions<RequestBox> options = new FirestoreRecyclerOptions.Builder<RequestBox>()
				.setLifecycleOwner(this)
				.setQuery(query, RequestBox.class)
				.build();

		//Assign project box adapter to variable
		adapter = new RequestBoxAdapter(options);

		//Handle the view (check if empty and use place holder)
		handleRecyclerView(adapter);

		//Set adapter attributes and start
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
		recyclerView.setAdapter(adapter);
	}

	private void handleRecyclerView (final RequestBoxAdapter adapter) {
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
