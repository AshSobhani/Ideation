package com.example.ideation;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class ProjectBox {
	//Declare variables (must be caps for fire store query)
	private String Title, OwnerName, Category;
	private Timestamp DateCreated;

	public ProjectBox() {
		//empty constructor needed
	}

	public ProjectBox(String title, String ownerName, String category, Timestamp dateCreated) {
		this.Title = title;
		this.OwnerName = ownerName;
		this.Category = category;
		this.DateCreated = dateCreated;
	}

	public String getTitle() {
		return Title;
	}

	public String getOwnerName() {
		return OwnerName;
	}

	public String getCategory() {
		return Category;
	}

	public String getDateCreated() {
		//Change format
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		String formattedDateCreated = df.format(DateCreated.toDate());

		return formattedDateCreated;
	}
}
