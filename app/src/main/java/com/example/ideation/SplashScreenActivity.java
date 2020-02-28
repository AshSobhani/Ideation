package com.example.ideation;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {
	private static final String TAG = "SplashScreenActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: Splash Screen Booting...");

		//Configure how the splash screen will look and what activity it will go to
		EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
				.withFullScreen()
				.withTargetActivity(MainActivity.class)
				.withSplashTimeOut(2500)
				.withBackgroundColor(Color.parseColor("#333333"))
				.withFooterText("IDEATION LTD. ©")
				.withLogo(R.drawable.ideation_logo);

		//Set the text colour and size
		config.getFooterTextView().setTextColor(Color.parseColor("#00CCFF"));
		config.getFooterTextView().setPadding(0, 0, 0, 40);
		config.getFooterTextView().setTextSize(20);

		//Create and view the splash screen
		View easySplashScreen = config.create();
		setContentView(easySplashScreen);
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart: START");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume: RESUME");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause: PAUSE");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy: DESTROY");
		super.onDestroy();
	}
}
