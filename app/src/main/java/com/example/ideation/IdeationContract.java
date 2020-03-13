package com.example.ideation;

public class IdeationContract {
	//Collections
	public static final String COLLECTION_USERS = "Users";
	public static final String COLLECTION_PROJECTS = "Projects";

	//Project Sub Collections
	public static final String COLLECTION_PROJECT_WHITELIST = "Whitelist";
	public static final String COLLECTION_PROJECT_REQUESTS = "Access Requests";


	//User Document Fields
	public static final String USER_FIRSTNAME = "FirstName";
	public static final String USER_LASTNAME = "LastName";
	public static final String USER_USERNAME = "UserName";
	public static final String USER_BIO = "Bio";
	public static final String USER_AGE = "Age";
	public static final String USER_PHOTOURL = "PhotoUrl";

	//Project Document Fields
	public static final String PROJECT_OWNERUID = "OwnerUID";
	public static final String PROJECT_OWNERNAME = "OwnerName";
	public static final String PROJECT_TITLE = "Title";
	public static final String PROJECT_DESCRIPTION = "Description";
	public static final String PROJECT_CATEGORY = "Category";
	public static final String PROJECT_DATE_CREATED = "DateCreated";

	//Project Sub Collection: Whitelist Fields
	public static final String PROJECT_WHITELIST_DATETIME = "WhitelistDateTime";

	//Project Sub Collection: Whitelist Fields
	public static final String PROJECT_REQUESTS_USERUID = "UserUID";
	public static final String PROJECT_REQUESTS_USERNAME = "UserName";
	public static final String PROJECT_REQUESTS_PROJECT = "Project";
	public static final String PROJECT_REQUESTS_DATETIME = "DateTime";
	public static final String PROJECT_REQUESTS_REASON = "Reason";
	public static final String PROJECT_REQUESTS_STATUS = "Status";
	//Request status states
	public static final String REQUESTS_STATUS_ACCESS_REQUESTED = "Access Requested";
	public static final String REQUESTS_STATUS_SIGNATURE_PENDING = "Signature Pending";
	public static final String REQUESTS_STATUS_PROJECT_ARCHIVED = "Project Archived";

}
