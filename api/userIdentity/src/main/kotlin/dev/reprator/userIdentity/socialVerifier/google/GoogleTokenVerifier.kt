package dev.reprator.userIdentity.socialVerifier.google

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import dev.reprator.base.action.AppLogger
import java.util.*

class GoogleTokenVerifier(private val logger: AppLogger) {

    private val transport: HttpTransport = NetHttpTransport()
    private val jsonFactory: JsonFactory = GsonFactory()
    private val MY_APP_GOOGLE_CLIENT_ID = "wouldntyouliketoknow"

    fun verifyToken(idTokenString: String) {
        logger.e {
            "inside verifyToken of verifyToken"
        }

        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
// Specify the CLIENT_ID of the app that accesses the backend:
            .setAudience(Collections.singletonList(MY_APP_GOOGLE_CLIENT_ID))
// Or, if multiple clients access the backend:
//.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
            .build()

// (Receive idTokenString by HTTPS POST)

        val idToken: GoogleIdToken? = verifier.verify(idTokenString)
        if (idToken != null) {
            val payload: Payload = idToken.payload

            // Print user identifier
            val userId = payload.subject
            System.out.println("User ID: " + userId)

            // Get profile information from payload
            val email = payload.email
            val emailVerified = payload.emailVerified
            val name = payload["name"] as? String
            val pictureUrl = payload["picture"] as? String
            val locale = payload["locale"] as? String
            val familyName = payload["family_name"] as? String
            val givenName = payload["given_name"] as? String

            // Use or store profile information
            // ...

        } else {
            System.out.println("Invalid ID token.")
        }
    }
}

/*
https://stackoverflow.com/questions/43043526/java-web-googlesignin-googleidtokenverifier-verify-token-string-returns-null
https://developers.google.com/identity/sign-in/web/backend-auth
https://developers.google.com/identity/gsi/web/reference/js-reference#credential


https://www.youtube.com/watch?v=j_31hJtWjlw
https://gist.github.com/henriquemenezes/c6da14afeb201603ef54a1a4bde0c0d0
https://developers.google.com/identity/sign-in/android/offline-access
* */