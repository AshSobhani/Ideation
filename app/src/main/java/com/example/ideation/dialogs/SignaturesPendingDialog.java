package com.example.ideation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.example.ideation.R;
import com.example.ideation.activities.SignatureActivity;
import com.example.ideation.database.IdeationContract;
import com.example.ideation.recycler.SignatureBox;
import com.example.ideation.recycler.SignatureBoxAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SignaturesPendingDialog extends AppCompatDialogFragment {
	private static final String TAG = "SignaturesPendingDialog";
	
	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private SignatureBoxAdapter adapter;
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
		View v = inflater.inflate(R.layout.layout_signatures_pending_dialog, null);

		// Get Firebase Auth instance and assign to variable
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign views to variables
		recyclerView = v.findViewById(R.id.requestsRecyclerView);

		//Set the builder view and customise
		builder.setView(v)
				.setTitle("Pending Signatures")
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
		String userUID = firebaseAuth.getUid();

		//Create the project collection query, and if needed add query filer below (priority, by date, etc..)
		Query query = db.collectionGroup(IdeationContract.COLLECTION_ACCESS_REQUESTS)
				.whereEqualTo(IdeationContract.PROJECT_REQUESTS_USERUID, userUID)
				.whereEqualTo(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_SIGNATURE_PENDING);

		//Query the database and build
		FirestoreRecyclerOptions<SignatureBox> options = new FirestoreRecyclerOptions.Builder<SignatureBox>()
				.setLifecycleOwner(this)
				.setQuery(query, SignatureBox.class)
				.build();

		//Assign project box adapter to variable
		adapter = new SignatureBoxAdapter(options);

		//Set adapter attributes and start
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
		recyclerView.setAdapter(adapter);

		//View Project - Detect when a project has been clicked and open that activity
		adapter.setOnBoxClickListener(new SignatureBoxAdapter.OnBoxClickListener() {
			@Override
			public void onBoxClick(DocumentSnapshot documentSnapshot, int position) {
				//Get project UID
				final String projectUID = documentSnapshot.getReference().getParent().getParent().getId();
				//Get request UID
				final String requestUID = documentSnapshot.getId();

				//Pass through the project and request UID
				toSignatureActivity(projectUID, requestUID);
			}
		});
	}

	private void toSignatureActivity(final String projectUID, final String requestUID) {
		//Put the projectUID into a new bundle
		Bundle bundle = new Bundle();
		bundle.putString("projectUID", projectUID);
		bundle.putString("requestUID", requestUID);

		//Create an intent and start view project activity whilst also sending the bundle
		Intent intent = new Intent(getContext(), SignatureActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
