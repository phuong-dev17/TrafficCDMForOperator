package org.deplide.application.android.trafficcdmforoperator.toolbox

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class AuthToolBox {
    private lateinit var serviceConfiguration: AuthorizationServiceConfiguration
    private lateinit var _authState: AuthState
    val authState: AuthState
        get() = _authState

    init {
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse(REALM_BASE_URI)
        ) { serviceConfig, ex ->
            if (ex != null) {
                Log.e(TAG, "failed to fetch configuration")
                return@fetchFromIssuer
            }

            serviceConfiguration = serviceConfig!!
            _authState = AuthState(serviceConfiguration)
        }
    }

    fun getAuthRequest(): AuthorizationRequest {
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfiguration,  // the authorization service configuration
            CLIENT_ID,  // the client ID, typically pre-registered and static
            ResponseTypeValues.CODE,  // the response_type value: we want a code
            REDIRECT_URI
        ) // the redirect URI to which the auth response is sent

        return authRequestBuilder
            .setScope("openid")
            .build()
    }

    companion object {
        private const val TAG = "AuthStateManager"
        private const val REALM_BASE_URI = "https://id.deplide.org/realms/deplide"
        private const val CLIENT_ID = "tcmf-submission-app"
        private val REDIRECT_URI = "org.deplide.application.android.trafficcdmforoperator:/oauth2redirect".toUri()
        private var authToolBoxInstance: AuthToolBox? = null

        fun getInstance(): AuthToolBox {
            if (authToolBoxInstance == null) {
                authToolBoxInstance = AuthToolBox()
            }

            return authToolBoxInstance!!
        }
    }
}