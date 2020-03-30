package com.example.ideation.recycler;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class SignatureBox {
	//Declare variables (must be caps for fire store query)
	private String Project, OwnerName;
	private Timestamp DateTime;

	public SignatureBox() {
		//empty constructor needed
	}

	public SignatureBox(String title, String userName, String requestReason, Timestamp requestDateTime) {
		//Assign values to variables
		this.Project = title;
		this.OwnerName = userName;
		this.DateTime = requestDateTime;
	}

	public String getProject() {
		return Project;
	}

	public String getOwnerName() {
		return OwnerName;
	}

	public String getDateTime() {
		//Change format
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		String formattedDateTime = df.format(DateTime.toDate());

		return formattedDateTime;
	}
}
