package com.example.ideation.recycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RequestBoxAdapter extends FirestoreRecyclerAdapter<RequestBox, RequestBoxAdapter.RequestBoxHolder> {
	private static final String TAG = "RequestBoxAdapter";

	public RequestBoxAdapter(@NonNull FirestoreRecyclerOptions<RequestBox> options) {
		super(options);
	}

	@Override
	protected void onBindViewHolder(@NonNull final RequestBoxHolder holder, final int position, @NonNull RequestBox model) {
		//Set the Project Box fields respectively
		holder.textViewProject.setText(model.getProject());
		holder.textViewUserName.setText(model.getUserName());
		holder.textViewRequestDateTime.setText(model.getDateTime());

		//If the user gives a request reason then show it otherwise hide reason text field
		if (!model.getReason().equals("")) {
			holder.textViewRequestReason.setText(model.getReason());
			holder.textViewRequestReason.setVisibility(View.VISIBLE);
		}
	}

	@NonNull
	@Override
	public RequestBoxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		//Find the view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_box, parent, false);

		return new RequestBoxHolder(v);
	}

	class RequestBoxHolder extends RecyclerView.ViewHolder {
		//Initialise variables
		TextView textViewProject;
		TextView textViewUserName;
		TextView textViewRequestReason;
		TextView textViewRequestDateTime;
		ImageButton acceptRequestButton;
		ImageButton declineRequestButton;

		public RequestBoxHolder(@NonNull View boxView) {
			super(boxView);
			//Find views and assign to variables
			textViewProject = boxView.findViewById(R.id.projectName);
			textViewUserName = boxView.findViewById(R.id.requesterName);
			textViewRequestReason = boxView.findViewById(R.id.requestReason);
			textViewRequestDateTime = boxView.findViewById(R.id.requestDateTime);
			acceptRequestButton = boxView.findViewById(R.id.acceptRequestButton);
			declineRequestButton = boxView.findViewById(R.id.declineRequestButton);

			//If they click the accept request button do as follows
			acceptRequestButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "onClick: Request Accepted");

					//Get the adapter position
					int position = getAdapterPosition();
					if (position != RecyclerView.NO_POSITION) {
						//Accept the request by updating request status and adding them to whitelist and notify the holder
						handleRequest(getAdapterPosition());
					}
				}
			});

			//If they click the decline request button do as follows
			declineRequestButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "onClick: Request Declined");

					//Get the adapter position
					int position = getAdapterPosition();
					if (position != RecyclerView.NO_POSITION) {
						//Decline the request by updating request status and notify the holder
						declineRequest(position);
					}
				}
			});
		}
	}

	private void handleRequest(int position) {
		//Access the requests document to get the requester user UID
		getSnapshots().getSnapshot(position).getReference().get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						//Get the NDA flag to check if project needs a signature
						Boolean ndaFlag = documentSnapshot.getBoolean(IdeationContract.PROJECT_REQUESTS_NDA_FLAG);

						//If the project does not have an NDA form grant the user access otherwise request signature
						if (!ndaFlag) {
							//Project doesn't have an NDA so grant access
							acceptRequest(documentSnapshot);
						} else {
							//Project has an NDA so request a signature
							requestSignature(documentSnapshot);
						}
					}
				});
	}

	private void acceptRequest(DocumentSnapshot documentSnapshot) {
		//Retrieve the user UID and put into a string
		String userUID = documentSnapshot.getString(IdeationContract.PROJECT_REQUESTS_USERUID);

		//Put the requester user UID into the projects whitelist
		documentSnapshot.getReference().getParent().getParent()
				.update(IdeationContract.PROJECT_WHITELIST, FieldValue.arrayUnion(userUID))
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: User added to whitelist");
					}
				});

		//On request accepted, set status to accepted
		documentSnapshot.getReference()
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_REQUEST_ACCEPTED)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request accepted and status updated to request accepted");
						//notifyDataSetChanged();
					}
				});
	}

	private void requestSignature(DocumentSnapshot documentSnapshot) {
		//On request accepted, set status to accepted
		documentSnapshot.getReference()
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_SIGNATURE_PENDING)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request accepted and status updated to signature pending");
						//notifyDataSetChanged();
					}
				});
	}

	private void declineRequest(final int position) {
		//On request decline request, set status to declined
		getSnapshots().getSnapshot(position).getReference()
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_REQUEST_DECLINED)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request declined and status updated");
						//notifyDataSetChanged();
					}
				});
	}
}
