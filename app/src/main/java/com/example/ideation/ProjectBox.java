package com.example.ideation;

public class ProjectBox {

	//Declare variables (must be caps for fire store query)
	private String Title, Category, DateCreated;

	public ProjectBox() {
		//empty constructor needed
	}

	public ProjectBox(String name, String category, String date) {
		this.Title = name;
		this.Category = category;
		this.DateCreated = date;
	}

	public String getTitle() {
		return Title;
	}

	public String getCategory() {
		return Category;
	}

	public String getDateCreated() {
		return DateCreated;
	}
}
