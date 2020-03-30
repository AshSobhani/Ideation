package com.example.ideation.recycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ideation.R;
import com.example.ideation.database.IdeationContract;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SignatureBoxAdapter extends FirestoreRecyclerAdapter<SignatureBox, SignatureBoxAdapter.SignatureBoxHolder> {
	private static final String TAG = "RequestBoxAdapter";

	//Create variables
	private OnBoxClickListener listener;

	public SignatureBoxAdapter(@NonNull FirestoreRecyclerOptions<SignatureBox> options) {
		super(options);
	}

	@Override
	protected void onBindViewHolder(@NonNull SignatureBoxHolder holder, final int position, @NonNull SignatureBox model) {
		//Set the Project Box fields respectively
		holder.textViewProject.setText(model.getProject());
		holder.textViewOwnerName.setText(model.getOwnerName());
		holder.textViewRequestDateTime.setText(model.getDateTime());
	}

	@NonNull
	@Override
	public SignatureBoxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		//Find the view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.signature_box, parent, false);

		return new SignatureBoxHolder(v);
	}

	class SignatureBoxHolder extends RecyclerView.ViewHolder {
		//Initialise variables
		TextView textViewProject;
		TextView textViewOwnerName;
		TextView textViewRequestDateTime;

		public SignatureBoxHolder(@NonNull View boxView) {
			super(boxView);
			//Find views and assign to variables
			textViewProject = boxView.findViewById(R.id.projectName);
			textViewOwnerName = boxView.findViewById(R.id.ownerName);
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

	public void setOnBoxClickListener(SignatureBoxAdapter.OnBoxClickListener listener) {
		this.listener = listener;
	}
}
