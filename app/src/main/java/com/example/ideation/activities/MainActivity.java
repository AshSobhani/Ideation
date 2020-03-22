package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
	Fragment selectedFragment, discoveryFragment, projectsFragment, profileFragment;
	BottomNavigationView bottomNav = null;
	FragmentManager fragmentManager;
	int fragmentFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: Main Activity Open");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Get the fragment manager and assign to variable
		fragmentManager = getSupportFragmentManager();

		//If the project fragment exists then retrieve it otherwise create it
		if (savedInstanceState != null) {
			//Restore the fragment states instead of creating new ones
			discoveryFragment = fragmentManager.getFragment(savedInstanceState, "discoveryFragment");
			projectsFragment = fragmentManager.getFragment(savedInstanceState, "projectsFragment");
			profileFragment = fragmentManager.getFragment(savedInstanceState, "profileFragment");
		} else {
			//Create fragments and assign to variables
			discoveryFragment = new DiscoveryFragment();
			projectsFragment = new ProjectsFragment();
			profileFragment = new ProfileFragment();

			//Create a initial fragment transaction
			FragmentTransaction initialiseFragmentTransaction = fragmentManager.beginTransaction();
			//Add the fragments to the fragment manager and only display the discovery fragment
			initialiseFragmentTransaction.add(R.id.fragment_container, discoveryFragment).show(discoveryFragment);
			initialiseFragmentTransaction.add(R.id.fragment_container, projectsFragment).hide(projectsFragment);
			initialiseFragmentTransaction.add(R.id.fragment_container, profileFragment).hide(profileFragment).commit();
		}

		//Find my nav bar and assign to variable
		bottomNav = findViewById(R.id.bottom_navbar);
		bottomNav.setOnNavigationItemSelectedListener(navListener);
	}

	//Create a listener to detect changes in selected fragments
	private BottomNavigationView.OnNavigationItemSelectedListener navListener =
			new BottomNavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
					selectedFragment = null;

					//Create a select fragment transaction
					FragmentTransaction selectFragmentTransaction = fragmentManager.beginTransaction();
					//Hide all the fragments
					selectFragmentTransaction.hide(discoveryFragment);
					selectFragmentTransaction.hide(projectsFragment);
					selectFragmentTransaction.hide(profileFragment);

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
						selectFragmentTransaction.show(selectedFragment).commit();
					}
					return true;
				}
			};

	private void returnToFragment() {
		//Create a select fragment transaction
		FragmentTransaction returnToFragmentTransaction = fragmentManager.beginTransaction();
		//Hide all the fragments
		returnToFragmentTransaction.hide(discoveryFragment);
		returnToFragmentTransaction.hide(projectsFragment);
		returnToFragmentTransaction.hide(profileFragment);

		//Use a switch that returns the user to their selected fragment based on the set flag
		switch (fragmentFlag) {
			case 0:
				//Return to discovery fragment and check navigation icon
				returnToFragmentTransaction.show(discoveryFragment);
				bottomNav.getMenu().getItem(0).setChecked(true);
				break;
			case 1:
				//Return to projects fragment and check navigation icon
				returnToFragmentTransaction.show(projectsFragment);
				bottomNav.getMenu().getItem(1).setChecked(true);
				break;
			case 2:
				//Return to profile fragment and check navigation icon
				returnToFragmentTransaction.show(profileFragment);
				bottomNav.getMenu().getItem(2).setChecked(true);
				break;
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		Log.d(TAG, "onSaveInstanceState: Saving");

		//Save the fragment flag when state is being saved
		outState.putInt("fragmentFlag", fragmentFlag);

		//Save all the fragment states
		fragmentManager.putFragment(outState, "discoveryFragment", discoveryFragment);
		fragmentManager.putFragment(outState, "projectsFragment", projectsFragment);
		fragmentManager.putFragment(outState, "profileFragment", profileFragment);

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

