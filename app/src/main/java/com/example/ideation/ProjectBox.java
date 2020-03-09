package com.example.ideation;

public class ProjectBox {

	//Declare variables (must be caps for fire store query)
	private String Title, OwnerName, Category, DateCreated;

	public ProjectBox() {
		//empty constructor needed
	}

	public ProjectBox(String title, String ownerName, String category, String dateCreated) {
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
		return DateCreated;
	}
}
