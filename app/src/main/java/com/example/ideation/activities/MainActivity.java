package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.PersistableBundle;
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
	Fragment selectedFragment, discoveryFragment, projectsFragment, profileFragment;
	BottomNavigationView bottomNav = null;
	int fragmentFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: Main Activity Open");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Create fragments and assign to variables
		discoveryFragment = new DiscoveryFragment();
		profileFragment = new ProfileFragment();

		//If the project fragment exists then retrieve it otherwise create it
		if (savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "projectsFragment") != null) {
			//Restore the fragment's instance
			projectsFragment = getSupportFragmentManager().getFragment(savedInstanceState, "projectsFragment");
		} else {
			projectsFragment = new ProjectsFragment();
		}

		//Find my nav bar and assign to variable
		bottomNav = findViewById(R.id.bottom_navbar);
		bottomNav.setOnNavigationItemSelectedListener(navListener);

		//Set starting fragment to discovery
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, discoveryFragment).commit();
	}

	//Create a listener to detect changes in selected fragments
	private BottomNavigationView.OnNavigationItemSelectedListener navListener =
			new BottomNavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
					selectedFragment = null;

					//Set selected fragment accordingly by whats been selected
					//Set a flag to for return to fragment checker
					switch (menuItem.getItemId()) {
						case R.id.navDiscovery:
							fragmentFlag = 0;
							selectedFragment = discoveryFragment;
							break;

						case R.id.navMyProjects:
							fragmentFlag = 1;
							selectedFragment = projectsFragment;
							break;

						case R.id.navProfile:
							fragmentFlag = 2;
							selectedFragment = profileFragment;
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
		//Use a switch that returns the user to their selected fragment based on the set flag
		switch (fragmentFlag) {
			case 0:
				//Return to discovery fragment and check navigation icon
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, discoveryFragment).commit();
				bottomNav.getMenu().getItem(0).setChecked(true);
				break;
			case 1:
				//Return to projects fragment and check navigation icon
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, projectsFragment).commit();
				bottomNav.getMenu().getItem(1).setChecked(true);
				break;
			case 2:
				//Return to profile fragment and check navigation icon
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
				bottomNav.getMenu().getItem(2).setChecked(true);
				break;
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		Log.d(TAG, "onSaveInstanceState: Saving");
		//Save the fragment flag when state is being saved
		outState.putInt("fragmentFlag", fragmentFlag);

		//If we are currently on the projects fragment then save the its instance
		Fragment currentFragment = (Fragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if(currentFragment instanceof ProjectsFragment){
			getSupportFragmentManager().putFragment(outState, "projectsFragment", projectsFragment);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState: Restoring");
		super.onRestoreInstanceState(savedInstanceState);

		//Retrieve the fragment flag and return to the correct fragment
		fragmentFlag = savedInstanceState.getInt("fragmentFlag");

		returnToFragment();
	}
}

