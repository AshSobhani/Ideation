const functions = require('firebase-functions');
const crypto = require('crypto');

// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

exports.helloWorld = functions.https.onRequest((request, response) => {
    console.log('hello')
    response.send("Hello from Firebase!");
});

exports.verifySignature = functions.https.onCall((data) => {
    //Data passed from the client.
    const publicKey = data.publicKey;
    const signature = data.signature;
    const message = data.message;

    //Checking that attributes are present and are numbers.
    if (!publicKey === null || !signature === null || !message === null) {
    // Throwing an Error so that the client gets the error details.
    throw new functions.https.HttpsError('invalid-argument', 'The function must be called with ' +
        'three arguments "publicKey", "signature" and "message".');
    }

    //Creating the verifier
    const verify = crypto.createVerify('RSA-SHA512');
    verify.update(message, 'base64')
    verify.end();

    //Constructing the public key format
    const l1 = "-----BEGIN PUBLIC KEY-----\n"
    const l2 = publicKey
    const l3 = "\n-----END PUBLIC KEY-----"
    const formattedPublicKey = l1 + l2 + l3

    //Returns true or false
    const result = verify.verify(formattedPublicKey, signature, 'base64')
    console.log(result); //Prints Result

      //Returning result.
      return {
          verificationResult: result
      };
    });