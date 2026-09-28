package com.example.data.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

data class AuthAccountInfo(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val isAnonymous: Boolean = false,
    val provider: String = "FIREBASE"
)

object FirebaseAuthService {

    private const val TAG = "FirebaseAuthService"

    fun getAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not initialized: ${e.message}")
            null
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return getAuth()?.currentUser
    }

    fun getCurrentAccountInfo(): AuthAccountInfo? {
        val user = getCurrentUser() ?: return null
        return AuthAccountInfo(
            uid = user.uid,
            email = user.email.orEmpty(),
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
            photoUrl = user.photoUrl?.toString().orEmpty(),
            isAnonymous = user.isAnonymous,
            provider = user.providerId
        )
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthAccountInfo> {
        return try {
            val auth = getAuth() ?: throw IllegalStateException("Firebase Auth unavailable")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after sign-in")
            Result.success(
                AuthAccountInfo(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                    photoUrl = user.photoUrl?.toString().orEmpty(),
                    isAnonymous = user.isAnonymous,
                    provider = "GOOGLE"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Google Credential sign in failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmailPassword(email: String, pass: String): Result<AuthAccountInfo> {
        return try {
            val auth = getAuth() ?: throw IllegalStateException("Firebase Auth unavailable")
            val authResult = try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (signInErr: Exception) {
                // If account doesn't exist, create account
                auth.createUserWithEmailAndPassword(email, pass).await()
            }
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null")
            Result.success(
                AuthAccountInfo(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: email.substringBefore("@"),
                    photoUrl = user.photoUrl?.toString().orEmpty(),
                    provider = "EMAIL"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Email/password sign-in failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfileDetails(displayName: String, photoUrl: String? = null): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: return Result.failure(IllegalStateException("No user logged in"))
            val reqBuilder = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
            if (!photoUrl.isNullOrBlank()) {
                reqBuilder.setPhotoUri(android.net.Uri.parse(photoUrl))
            }
            user.updateProfile(reqBuilder.build()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            getAuth()?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error: ${e.message}")
        }
    }
}
