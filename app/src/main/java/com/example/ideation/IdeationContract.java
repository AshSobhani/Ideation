package com.example.ideation;

public class IdeationContract {
	//Collections
	public static final String COLLECTION_USERS = "Users";
	public static final String COLLECTION_PROJECTS = "Projects";
	public static final String COLLECTION_ACCESS_REQUESTS = "Access Requests";

	//User Document Fields
	public static final String USER_UID = "UID";
	public static final String USER_EMAIL = "Email";

	public static final String USER_FIRSTNAME = "FirstName";
	public static final String USER_LASTNAME = "LastName";
	public static final String USER_USERNAME = "Username";
	public static final String USER_BIO = "Bio";
	public static final String USER_AGE = "Age";
	public static final String USER_PHOTOURL = "PhotoUrl";

	//Project Document Fields
	public static final String PROJECT_OWNERUID = "OwnerUID";
	public static final String PROJECT_TITLE = "Title";
	public static final String PROJECT_DESCRIPTION = "Description";
	public static final String PROJECT_CATEGORY = "Category";
	public static final String PROJECT_DATE_CREATED = "DateCreated";


	//Access Requests Fields
	public static final String RECIPE_INGREDIENTS_RECIPEID = "recipe_id";
	public static final String RECIPE_INGREDIENTS_INGREDIENTID = "ingredient_id";
}
