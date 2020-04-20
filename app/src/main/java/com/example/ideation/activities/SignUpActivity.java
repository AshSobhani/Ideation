package com.example.ideation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideation.database.IdeationContract;
import com.example.ideation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
	private static final String TAG = "SignUpActivity";

	//Initialise variables
	private FirebaseAuth firebaseAuth;
	private EditText firstNameField, lastNameField, userNameField, emailField, passwordField, confirmPasswordField;
	private String emailText, passwordText, confirmPasswordText, firstNameText, lastNameText, userNameText;
	private TextView signUpFailedTextField;

	//Make an database instance
	private FirebaseFirestore db = FirebaseFirestore.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		// Initialize Firebase Auth
		firebaseAuth = FirebaseAuth.getInstance();

		//Assign the views to object
		firstNameField = findViewById(R.id.firstNameText);
		lastNameField = findViewById(R.id.lastNameText);
		userNameField = findViewById(R.id.userNameText);
		emailField = findViewById(R.id.emailText);
		passwordField = findViewById(R.id.newPasswordText);
		confirmPasswordField = findViewById(R.id.confirmPasswordText);
		signUpFailedTextField = findViewById(R.id.signUpFailedText);
	}

	public void onCreateAccount(View v) {
		//Retrieve the strings
		firstNameText = firstNameField.getText().toString();
		lastNameText = lastNameField.getText().toString();
		emailText = emailField.getText().toString();
		passwordText = passwordField.getText().toString();
		confirmPasswordText = confirmPasswordField.getText().toString();
		userNameText = userNameField.getText().toString();

		//If fields are not empty, try to create the account
		if (!emailText.equals("") && !passwordText.equals("") && !firstNameText.equals("") && !lastNameText.equals("") && !userNameText.equals("")) {
			//If the password match
			if (passwordText.equals(confirmPasswordText)) {
				//Create the account
				createAccount(emailText, passwordText);
			} else {
				signUpFailedTextField.setText("Passwords do not match");
			}
		} else {
			signUpFailedTextField.setText("Please fill out all the empty fields.");
		}
	}

	public void createAccount(String email, final String password) {
		Log.d(TAG, "createAccount: Creating account");

		//If email and password comply with expected format create the user
		firebaseAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign up success
							Log.d(TAG, "createAccount: Success");

							//Add user to users collection
							addUserToCollection();

							//Email for verification
							sendEmailVerification();

							//Sign out of the account
							firebaseAuth.signOut();

							//Finish activity and return to login activity
							finish();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(TAG, "createAccount: Failed");

						// Sign up failure, check why the sign up failed
						if (e instanceof FirebaseAuthWeakPasswordException) {
							// Password too weak
							signUpFailedTextField.setText("Password is too week");
						} else if (e instanceof FirebaseAuthInvalidCredentialsException) {
							// Email address is not a real email address
							signUpFailedTextField.setText("Please enter a valid email address");
						} else if (e instanceof FirebaseAuthUserCollisionException) {
							// Collision with existing user email
							signUpFailedTextField.setText("Email address is already in use");
						} else {
							//If its not any of the issues above just inform of creation failure
							signUpFailedTextField.setText("Account creation was unsuccessful");
						}
					}
				});
	}

	private void sendEmailVerification() {
		//Get the current user
		final FirebaseUser user = firebaseAuth.getCurrentUser();

		//Send the mail verification
		user.sendEmailVerification()
				.addOnCompleteListener(this, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Toast.makeText(SignUpActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
						} else {
							Log.e(TAG, "sendEmailVerification", task.getException());
							Toast.makeText(SignUpActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	private void addUserToCollection() {
		//Get the users unique ID
		String userUID = firebaseAuth.getUid();

		//Generate the keys (public and private together) using KeyStore
		String encodedPublicKey = storeKeyAsymmetric(userUID);

		//Create a hash to store the data before inserting into firebase
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put(IdeationContract.USER_FIRSTNAME, firstNameText);
		userInfo.put(IdeationContract.USER_LASTNAME, lastNameText);
		userInfo.put(IdeationContract.USER_USERNAME, userNameText);
		userInfo.put(IdeationContract.USER_PUBLIC_KEY, encodedPublicKey);

		//Insert user into users collection
		db.collection(IdeationContract.COLLECTION_USERS).document(userUID).set(userInfo)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Toast.makeText(SignUpActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(SignUpActivity.this, "Error!", Toast.LENGTH_SHORT).show();
						Log.d(TAG, e.toString());
					}
				});
	}

	public String storeKeyAsymmetric(String userUID){
		//Initialise the key key pair generator
		KeyPairGenerator keyPairGenerator = null;

		//Get instance of android keystore's RSA key algorithm
		try {
			keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}

		//Initialise and build a custom keypair generator
		try {
			keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(userUID, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
					.setDigests(KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA256)
					.setKeySize(2048)
					.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1, KeyProperties.ENCRYPTION_PADDING_RSA_OAEP, KeyProperties.ENCRYPTION_PADDING_NONE)
					.setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1, KeyProperties.SIGNATURE_PADDING_RSA_PSS)
					.build());

			//Generate the key pair and get the public key
			KeyPair keyPairAsymmetric = keyPairGenerator.generateKeyPair();
			PublicKey devicePublic = keyPairAsymmetric.getPublic();

			//Convert to bytes
			byte[] encodedPublic = devicePublic.getEncoded();

			//Convert to string using Base 64 and return
			return Base64.encodeToString(encodedPublic, Base64.NO_WRAP);

		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}
	}

	PublicKey pubKey;

	public void storeKeyAsymmetricTest(View v){
		//Initialise the key key pair generator
		KeyPairGenerator keyPairGenerator = null;

		//Get instance of android keystore's RSA key algorithm
		try {
			keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}

		//Initialise and build a custom keypair generator
		try {
			keyPairGenerator.initialize(new KeyGenParameterSpec.Builder("TEST", KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
					.setDigests(KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA256)
					.setKeySize(2048)
					.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1, KeyProperties.ENCRYPTION_PADDING_RSA_OAEP, KeyProperties.ENCRYPTION_PADDING_NONE)
					.setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1, KeyProperties.SIGNATURE_PADDING_RSA_PSS)
					.build());

			//Generate the key pair and get the public key
			KeyPair keyPairAsymmetric = keyPairGenerator.generateKeyPair();
			PublicKey devicePublic = keyPairAsymmetric.getPublic();

			//Convert to bytes
			byte[] encodedPublic = devicePublic.getEncoded();

			Log.d(TAG, "SignVerifyTest - Encoded Public: " + encodedPublic);

			//String encodedPublicString = new String(encodedPublic, StandardCharsets.ISO_8859_1);
			//byte[] encodedPublicKey = encodedPublicString.getBytes(StandardCharsets.ISO_8859_1);

			String encodedString = Base64.encodeToString(encodedPublic, Base64.NO_WRAP);

			Log.d(TAG, "SignVerifyTest - Encoded Public String After Base64 Encode: " + encodedString);

			byte[] encodedPublicKey = Base64.decode(encodedString, Base64.NO_WRAP);

			Log.d(TAG, "SignVerifyTest - Encoded Public After Base64 Decode: " + encodedPublicKey);

			//Takes your byte array of the key as constructor parameter
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			//Takes algorithm used (RSA) to generate keys
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			//Creates a new PublicKey object
			pubKey = keyFactory.generatePublic(pubKeySpec);

		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public void verifiedDeviceSignature(View v){
		String dataToSign = "MAMAMAMA";

		boolean verified = false;
		String signature = null;
		MessageDigest digest = null;

		try {
			digest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		digest.update(dataToSign.getBytes(StandardCharsets.UTF_8));
		byte[] hash = digest.digest();

		try {
			KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
			ks.load(null);

			//******This is a PrivateKeyEntry
			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("TEST", null);  //null if you don't have key locked up with password
			PrivateKey privateKey = privateKeyEntry.getPrivateKey();

			Signature s = Signature.getInstance("SHA512withRSA");
			s.initSign(privateKey);
			s.update(hash);
			byte[] sig = s.sign();

			Log.d(TAG, "SignVerifyTest - pubKey after generation: " + pubKey);

			Signature v1 = Signature.getInstance("SHA512withRSA");
			v1.initVerify(pubKey);
			v1.update(hash);
			verified = v1.verify(sig);

			String strSig = new String(Base64.encode(sig, 2));
			signature = strSig;



		} catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableEntryException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}

		if(verified){
			Log.d(TAG, "SignVerifyTest: Verified");
		} else {
			Log.d(TAG, "SignVerifyTest: Not Verified");
		}
	}

//	public void onEncryptTestA (View v) throws Exception {
//		String message = "Annie is gay";
//		byte[] data = message.getBytes(StandardCharsets.UTF_8);
//
//		KeyGenerator keygen = KeyGenerator.getInstance("AES");
//		keygen.init(256);
//		SecretKey key = keygen.generateKey();
//
//		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//		cipher.init(Cipher.ENCRYPT_MODE, key);
//
//		byte[] ciphertext = cipher.doFinal(data);
//		Log.d(TAG, "onEncryptTest: " + ciphertext);
//
//		byte[] iv = cipher.getIV();
//		Log.d(TAG, "onEncryptTest: " + iv);
//
//		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
//		byte[] decryptedText = cipher.doFinal(ciphertext);
//
//		String result = new String(decryptedText, StandardCharsets.UTF_8);
//
//		Log.d(TAG, "onEncryptTestResult: " + result);
//	}
//
//	public void onSignatureTest(View v) throws Exception {
//		//Create message
//		String message = "Annie is gay";
//		byte[] data = message.getBytes(StandardCharsets.UTF_8);
//
//		//Get keys
//		KeyPair clientKeys = getKeyPair();
//		PrivateKey privateKey = clientKeys.getPrivate();
//		PublicKey publicKey = clientKeys.getPublic();
//
//		//Sign
//		Signature s = Signature.getInstance("SHA512withRSA");
//		s.initSign(privateKey);
//		s.update(data);
//		byte[] signature = s.sign();
//
//		//Verify
//		s.initVerify(publicKey);
//		s.update(data);
//		boolean valid = s.verify(signature);
//
//		Log.d(TAG, "onEncryptTest: " + valid);
//	}
//
//	public static KeyPair getKeyPair() {
//		KeyPair kp = null;
//		try {
//			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//			kpg.initialize(2048);
//			kp = kpg.generateKeyPair();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return kp;
//	}

}
