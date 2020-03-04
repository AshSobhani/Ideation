package com.example.ideation;

import android.icu.text.Transliterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ProjectBoxAdapter extends FirestoreRecyclerAdapter<ProjectBox, ProjectBoxAdapter.ProjectBoxHolder> {

	public ProjectBoxAdapter(@NonNull FirestoreRecyclerOptions<ProjectBox> options) {
		super(options);
	}

	@Override
	protected void onBindViewHolder(@NonNull ProjectBoxHolder holder, int position, @NonNull ProjectBox model) {
		//Set the Project Box fields respectively
		holder.textViewTitle.setText(model.getTitle());
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
		getSnapshots().getSnapshot(position).getReference().delete();
	}

	class ProjectBoxHolder extends RecyclerView.ViewHolder {
		//Initialise variables
		TextView textViewTitle;
		TextView textViewCategory;
		TextView textViewDateCreated;


		public ProjectBoxHolder(@NonNull View itemView) {
			super(itemView);
			//Find views and assign to variables
			textViewTitle = itemView.findViewById(R.id.projectName);
			textViewCategory = itemView.findViewById(R.id.projectCategory);
			textViewDateCreated = itemView.findViewById(R.id.projectDateCreated);

			
		}
	}
}
