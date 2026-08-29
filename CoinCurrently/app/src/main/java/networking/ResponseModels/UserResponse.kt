package networking.ResponseModels

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val referralCount: Int,
    val user: RemoteUser,
)

data class RemoteUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val providerId: String,
    val emailVerified: Boolean,
    val disabled: Boolean,
    val providerData: List<ProviderUserData>,
    val tokensValidAfterTimestamp: String,
    val userMetaData: UserMetaData,
    val customClaims: UserCustomClaims?,
    val tenantId: String?,
)

data class ProviderUserData(
    val uid: String?,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val providerId: String?,
)

data class UserMetaData(
    val creationTimestamp: String,
    val lastSignInTimestamp: String,
    val lastRefreshTimestamp: String,
)

data class UserCustomClaims(
    @SerializedName("fcm_token")
    val fcmToken: String?,
    @SerializedName("preferred_currency")
    val preferredCurrency: String?,
)
