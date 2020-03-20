package com.example.ideation.fragments;

import com.example.ideation.fragments.MyProjectsFragment;
import com.example.ideation.fragments.SharedProjectsFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		//Create a fragment variable and set to null
		Fragment fragment = null;

		//Open fragment based on the tab that is selected
		switch (position) {
			case 0:
				fragment = new MyProjectsFragment();
				break;
			case 1:
				fragment = new SharedProjectsFragment();
				break;
		}
		return fragment;
	}

	@Override
	public int getCount() {
		// Show 2 total pages.
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		//Give the tabs titles
		switch (position) {
			case 0:
				return "My Projects";
			case 1:
				return "Shared Projects";
		}
		return null;
	}
}
