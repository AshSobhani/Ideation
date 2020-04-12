package com.example.ideation.recycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProjectBoxAdapter extends FirestoreRecyclerAdapter<ProjectBox, ProjectBoxAdapter.ProjectBoxHolder> {
	private static final String TAG = "ProjectBoxAdapter";

	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private OnBoxClickListener listener;

	public ProjectBoxAdapter(@NonNull FirestoreRecyclerOptions<ProjectBox> options) {
		super(options);
	}

	@Override
	protected void onBindViewHolder(@NonNull ProjectBoxHolder holder, int position, @NonNull ProjectBox model) {
		//Set the Project Box fields respectively
		holder.textViewTitle.setText(model.getTitle());
		holder.textViewOwnerName.setText(model.getOwnerName());
		holder.textViewCategory.setText(model.getCategory());
		holder.textViewDateCreated.setText(model.getDateCreated());
	}

	@NonNull
	@Override
	public ProjectBoxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		//Find the view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_box,
				parent, false);

		//Get the firebase auth instance
		firebaseAuth = FirebaseAuth.getInstance();

		return new ProjectBoxHolder(v);
	}

	public void deleteProject(int position) {
		//Create a reference to the project and make it final so it can be accessed in inner class
		final DocumentReference projectRef = getSnapshots().getSnapshot(position).getReference();

		//Get all the project access requests
		projectRef.collection(IdeationContract.COLLECTION_ACCESS_REQUESTS).get()
				.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
					@Override
					public void onComplete(@NonNull Task<QuerySnapshot> task) {
						//If its successful get all of the documents
						if (task.isSuccessful()) {
							for (QueryDocumentSnapshot document : task.getResult()) {
								//Create a hash map to insert archived status into the request status fields
								Map<String, Object> statusData = new HashMap<>();
								statusData.put(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_PROJECT_ARCHIVED);

								//Set the data and ensure that the data merges
								projectRef.collection(IdeationContract.COLLECTION_ACCESS_REQUESTS).document(document.getId())
										.set(statusData, SetOptions.merge());
							}
							//Delete the document
							projectRef.delete();
						} else {
							Log.d(TAG, "Error getting documents: ", task.getException());
						}
					}
				});
	}

	public void revokeAccess(int position) {
		Log.d(TAG, "revokeAccess: User being removed from whitelist");
		
		//Get the userUID
		final String userUID = firebaseAuth.getUid();

		//Create a reference to the project and make it final so it can be accessed in inner class
		final DocumentReference projectRef = getSnapshots().getSnapshot(position).getReference();

		//Get all the project access requests
		projectRef.update(IdeationContract.PROJECT_WHITELIST, FieldValue.arrayRemove(userUID))
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: User has been removed from the project whitelist");
					}
				});

		//Change the request status to access revoked and make the request applicability true
		//From the current project
		projectRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
			@Override
			public void onSuccess(DocumentSnapshot documentSnapshot) {
				//Get the project title
				String projectTitle = documentSnapshot.getString(IdeationContract.PROJECT_TITLE);
				//String projectUID =

				//Get the most recent request by this user
				projectRef.collection(IdeationContract.COLLECTION_ACCESS_REQUESTS)
						.whereEqualTo(IdeationContract.PROJECT_REQUESTS_USERUID, userUID)
						.whereEqualTo(IdeationContract.PROJECT_REQUESTS_PROJECT, projectTitle)
						.whereEqualTo(IdeationContract.PROJECT_REQUESTS_APPLICABLE, IdeationContract.FALSE)
						.orderBy(IdeationContract.PROJECT_REQUESTS_DATETIME, Query.Direction.DESCENDING)
						.limit(1).get()
						.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
							@Override
							public void onComplete(@NonNull Task<QuerySnapshot> task) {
								String requestUID = task.getResult().getDocuments().get(0).getId();

								projectRef.collection(IdeationContract.COLLECTION_ACCESS_REQUESTS).document(requestUID)
										.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_ACCESS_REVOKED);
								projectRef.collection(IdeationContract.COLLECTION_ACCESS_REQUESTS).document(requestUID)
										.update(IdeationContract.PROJECT_REQUESTS_APPLICABLE, IdeationContract.TRUE);
							}
						});
			}
		});
	}

	class ProjectBoxHolder extends RecyclerView.ViewHolder {
		//Initialise variables
		TextView textViewTitle;
		TextView textViewOwnerName;
		TextView textViewCategory;
		TextView textViewDateCreated;

		public ProjectBoxHolder(@NonNull View boxView) {
			super(boxView);
			//Find views and assign to variables
			textViewTitle = boxView.findViewById(R.id.projectName);
			textViewOwnerName = boxView.findViewById(R.id.ownerName);
			textViewCategory = boxView.findViewById(R.id.projectCategory);
			textViewDateCreated = boxView.findViewById(R.id.creationDate);

			//Set an on click listener which gets the box position so we can use it later
			boxView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getAdapterPosition();
					if (position != RecyclerView.NO_POSITION && listener != null) {
						listener.onBoxClick(getSnapshots().getSnapshot(position), position);
					}
				}
			});
		}
	}

	public interface OnBoxClickListener {
		void onBoxClick(DocumentSnapshot documentSnapshot, int position);
	}

	public void setOnBoxClickListener(OnBoxClickListener listener) {
		this.listener = listener;
	}
}
