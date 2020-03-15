package com.example.ideation;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class RequestBox {
	private static final String TAG = "RequestBox";
	//Declare variables (must be caps for fire store query)
	private String Project, UserName, Reason;
	private Timestamp DateTime;

	public RequestBox() {
		//empty constructor needed
	}

	public RequestBox(String title, String userName, String requestReason, Timestamp requestDateTime) {
		this.Project = title;
		this.UserName = userName;
		this.Reason = requestReason;
		this.DateTime = requestDateTime;
	}

	public String getProject() {
		Log.d(TAG, "getProject: " + Project);
		return Project;
	}

	public String getUserName() {
		return UserName;
	}

	public String getReason() {
		return Reason;
	}

	public String getDateTime() {
		//Change format
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		String formattedDateTime = df.format(DateTime.toDate());

		return formattedDateTime;
	}
}
