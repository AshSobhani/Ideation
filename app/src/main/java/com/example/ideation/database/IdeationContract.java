package com.example.ideation.database;

public class IdeationContract {
	//Storage
	public static final String STORAGE_NDA_FORMS = "NDAForms";

	//Collections
	public static final String COLLECTION_USERS = "Users";
	public static final String COLLECTION_PROJECTS = "Projects";

	//Project Sub Collections
	public static final String COLLECTION_ACCESS_REQUESTS = "Access Requests";

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
	public static final String PROJECT_WHITELIST = "Whitelist";
	public static final String PROJECT_NDA_PATH = "NDAPath";

	//Project Sub Collection: Request Fields
	public static final String PROJECT_REQUESTS_OWNERUID = "OwnerUID";
	public static final String PROJECT_REQUESTS_OWNERNAME = "OwnerName";
	public static final String PROJECT_REQUESTS_USERUID = "UserUID";
	public static final String PROJECT_REQUESTS_USERNAME = "UserName";
	public static final String PROJECT_REQUESTS_PROJECT = "Project";
	public static final String PROJECT_REQUESTS_DATETIME = "DateTime";
	public static final String PROJECT_REQUESTS_REASON = "Reason";
	public static final String PROJECT_REQUESTS_STATUS = "Status";
	public static final String PROJECT_REQUESTS_NDA_FLAG = "NDAFlag";
	public static final String PROJECT_REQUESTS_APPLICABLE = "Applicable";

	//Request Status States
	public static final String REQUESTS_STATUS_ACCESS_REQUESTED = "Access Requested"; //A user has requested access from a project
	public static final String REQUESTS_STATUS_SIGNATURE_PENDING = "Signature Pending"; //The request has been accepted with the condition that the NDA is signed off
	public static final String REQUESTS_STATUS_REQUEST_ACCEPTED = "Request Accepted"; //The request has been full accepted the user now has access
	public static final String REQUESTS_STATUS_REQUEST_DECLINED = "Request Declined"; //The project owner has declined the users request
	public static final String REQUESTS_STATUS_ACCESS_REVOKED = "Access Revoked"; //The user has revoked their access privilege after gaining access
	public static final String REQUESTS_STATUS_SIGNATURE_DENIED = "Signature Denied"; //The user has disagreed to the NDA form and revoked their request
	public static final String REQUESTS_STATUS_PROJECT_ARCHIVED = "Project Archived"; //The project has been archived

	//Boolean States
	public static final Boolean TRUE = true;
	public static final Boolean FALSE = false;
}
