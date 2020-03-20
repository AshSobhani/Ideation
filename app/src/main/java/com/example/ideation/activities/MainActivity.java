package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.ideation.fragments.DiscoveryFragment;
import com.example.ideation.fragments.ProfileFragment;
import com.example.ideation.fragments.ProjectsFragment;
import com.example.ideation.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	//Create variables
	BottomNavigationView bottomNav = null;
	int fragmentFlag = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: Main Activity Open");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Find my nav bar and assign to variable
		bottomNav = findViewById(R.id.bottom_navbar);
		bottomNav.setOnNavigationItemSelectedListener(navListener);

		//Set starting fragment to sessions
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiscoveryFragment()).commit();
	}

	//Create a listener to detect changes in selected fragments
	private BottomNavigationView.OnNavigationItemSelectedListener navListener =
			new BottomNavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
					Fragment selectedFragment = null;

					//Set selected fragment accordingly by whats been selected
					//Set a flag to for return to fragment checker
					switch (menuItem.getItemId()) {
						case R.id.navDiscovery:
							fragmentFlag = 0;
							selectedFragment = new DiscoveryFragment();
							break;

						case R.id.navMyProjects:
							fragmentFlag = 1;
							selectedFragment = new ProjectsFragment();
							break;

						case R.id.navProfile:
							fragmentFlag = 2;
							selectedFragment = new ProfileFragment();
							break;
					}

					//Make sure a fragment has been selected
					if (selectedFragment != null) {
						//Execute set or switch respectfully to the selected fragment
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
					}
					return true;
				}
			};

	private void returnToFragment() {
		switch (fragmentFlag) {
			case 0:
				//Return to discovery fragment and check navigation icon
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiscoveryFragment()).commit();
				bottomNav.getMenu().getItem(0).setChecked(true);
				break;
			case 1:
				//Return to projects fragment and check navigation icon
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProjectsFragment()).commit();
				bottomNav.getMenu().getItem(1).setChecked(true);
				break;
			case 2:
				//Return to profile fragment and check navigation icon
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
				bottomNav.getMenu().getItem(2).setChecked(true);
				break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart: Starting activity");

		//Make sure we are on the right fragment
		returnToFragment();
	}
}

