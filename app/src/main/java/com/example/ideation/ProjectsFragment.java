package com.example.ideation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.tabs.TabLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProjectsFragment extends Fragment {

	//Create variables
	private TabLayout tabLayout;
	private ViewPager viewPager;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: In My Projects Fragment");
		//Assign the correct view to the fragment
		View v = inflater.inflate(R.layout.fragment_projects, container, false);

		//Assign views to variables
		tabLayout = (TabLayout) v.findViewById(R.id.tabLayout);
		viewPager = (ViewPager) v.findViewById(R.id.viewPager);

		//Create and set the adapter to viewPager
		ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
		viewPager.setAdapter(adapter);
		//Assign the viewPager to the tab layout
		tabLayout.setupWithViewPager(viewPager);

		return v;
	}

}
