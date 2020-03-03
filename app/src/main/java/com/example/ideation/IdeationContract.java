package com.example.ideation;

public class IdeationContract {
	//Collections
	public static final String USERS_COLLECTION = "Users";
	public static final String PROJECTS_COLLECTION = "Projects";
	public static final String ACCESS_REQUESTS_COLLECTION = "Access Requests";

	//User Document Fields
	public static final String USER_UID = "UID";
	public static final String USER_EMAIL = "Email";

	public static final String USER_USERNAME = "UserName";
	public static final String USER_BIO = "Bio";
	public static final String USER_AGE = "Age";
	public static final String USER_FIRSTNAME = "FirstName";
	public static final String USER_LASTNAME = "LastName";
	public static final String USER_PHOTOURL = "PhotoUrl";

	//Project Document Fields
	public static final String INGREDIENTS_ID = "_id";
	public static final String INGREDIENTS_NAME = "ingredientname";

	//Access Requests Fields
	public static final String RECIPE_INGREDIENTS_RECIPEID = "recipe_id";
	public static final String RECIPE_INGREDIENTS_INGREDIENTID = "ingredient_id";
}
