package com.example.ideation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class NewProjectActivity extends AppCompatActivity {
	private static final String TAG = "NewProjectActivity";

	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";

	private EditText editTextTitle;
	private EditText editTextDescription;
	private TextView textViewData;

	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private DocumentReference noteRef = db.collection("Notepad").document("My First Note");


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_project);

		//Setting views to variables
		editTextTitle = findViewById(R.id.edit_text_title);
		editTextDescription = findViewById(R.id.edit_text_description);
		textViewData = findViewById(R.id.text_view_data);
	}

	public void onAddRecord(View v) {
		Log.d(TAG, "onAddRecord: Adding Record");
		String title = editTextTitle.getText().toString();
		String description = editTextDescription.getText().toString();

		Map<String, Object> note = new HashMap<>();
		note.put(KEY_TITLE, title);
		note.put(KEY_DESCRIPTION, description);

		noteRef.set(note)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Toast.makeText(NewProjectActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(NewProjectActivity.this, "Error!", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}

	public void onGetRecord(View v) {
		noteRef.get()
				.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
					@Override
					public void onSuccess(DocumentSnapshot documentSnapshot) {
						if (documentSnapshot.exists()) {
							String title = documentSnapshot.getString(KEY_TITLE);
							String description = documentSnapshot.getString(KEY_DESCRIPTION);

							//Map<String, Object> note = documentSnapshot.getData();

							textViewData.setText("Title: " + title + "\n" + "Description: " + description);
						} else {
							Toast.makeText(NewProjectActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(NewProjectActivity.this, "Error!", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}
}
