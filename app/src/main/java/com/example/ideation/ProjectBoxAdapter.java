package com.example.ideation;

import android.icu.text.Transliterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProjectBoxAdapter extends FirestoreRecyclerAdapter<ProjectBox, ProjectBoxAdapter.ProjectBoxHolder> {
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

		return new ProjectBoxHolder(v);
	}

	public void deleteProject(int position) {
		//Delete the project that has been selected
		getSnapshots().getSnapshot(position).getReference().delete();
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
			textViewDateCreated = boxView.findViewById(R.id.projectDateCreated);

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
