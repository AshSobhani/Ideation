package com.example.ideation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RequestBoxAdapter extends FirestoreRecyclerAdapter<RequestBox, RequestBoxAdapter.RequestBoxHolder> {
	private static final String TAG = "RequestBoxAdapter";
	private OnBoxClickListener listener;

	public RequestBoxAdapter(@NonNull FirestoreRecyclerOptions<RequestBox> options) {
		super(options);
	}

	@Override
	protected void onBindViewHolder(@NonNull RequestBoxHolder holder, final int position, @NonNull RequestBox model) {
		//Set the Project Box fields respectively
		holder.textViewProject.setText(model.getProject());
		holder.textViewUserName.setText(model.getUserName());
		holder.textViewRequestDateTime.setText(model.getDateTime());

		//If the user gives a request reason then show it otherwise hide reason text field
		if (!model.getReason().equals("")) {
			holder.textViewRequestReason.setText(model.getReason());
			holder.textViewRequestReason.setVisibility(View.VISIBLE);
		}

		//If they click the accept request button do as follows
		holder.acceptRequestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: Request Accepted");

				//Accept the request by updating request status and adding them to whitelist
				acceptRequest(position);
			}
		});

		//If they click the decline request button do as follows
		holder.declineRequestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: Request Declined");

				//Decline the request by updating request status
				declineRequest(position);
			}
		});
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

	private void acceptRequest(int position) {
		//Make position final so it can be used in the inner class
		final int finalPosition = position;

		//Access the requests document to get the requester user UID
		getSnapshots().getSnapshot(position).getReference().get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
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
										Log.d(TAG, "onComplete: Request accepted and status updated");
									}
								});
					}
				});
	}

	private void declineRequest(int position) {
		//On request decline request, set status to declined
		getSnapshots().getSnapshot(position).getReference()
				.update(IdeationContract.PROJECT_REQUESTS_STATUS, IdeationContract.REQUESTS_STATUS_REQUEST_DECLINED)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Log.d(TAG, "onComplete: Request declined and status updated");
					}
				});
	}

	public interface OnBoxClickListener {
		void onBoxClick(DocumentSnapshot documentSnapshot, int position);
	}

	public void setOnBoxClickListener(OnBoxClickListener listener) {
		this.listener = listener;
	}
}
