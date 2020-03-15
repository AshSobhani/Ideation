package com.example.ideation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RequestBoxAdapter extends FirestoreRecyclerAdapter<RequestBox, RequestBoxAdapter.RequestBoxHolder> {
	private OnBoxClickListener listener;

	public RequestBoxAdapter(@NonNull FirestoreRecyclerOptions<RequestBox> options) {
		super(options);
	}

	@Override
	protected void onBindViewHolder(@NonNull RequestBoxHolder holder, int position, @NonNull RequestBox model) {
		//Set the Project Box fields respectively
		holder.textViewProject.setText(model.getProject());
		holder.textViewUserName.setText(model.getUserName());
		holder.textViewRequestDateTime.setText(model.getDateTime());

		if (!model.getReason().equals("")) {
			holder.textViewRequestReason.setText(model.getReason());
			holder.textViewRequestReason.setVisibility(View.VISIBLE);
		}
	}

	@NonNull
	@Override
	public RequestBoxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		//Find the view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_box,
				parent, false);

		return new RequestBoxHolder(v);
	}

	class RequestBoxHolder extends RecyclerView.ViewHolder {
		//Initialise variables
		TextView textViewProject;
		TextView textViewUserName;
		TextView textViewRequestReason;
		TextView textViewRequestDateTime;

		public RequestBoxHolder(@NonNull View boxView) {
			super(boxView);
			//Find views and assign to variables
			textViewProject = boxView.findViewById(R.id.projectName);
			textViewUserName = boxView.findViewById(R.id.requesterName);
			textViewRequestReason = boxView.findViewById(R.id.requestReason);
			textViewRequestDateTime = boxView.findViewById(R.id.requestDateTime);

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
