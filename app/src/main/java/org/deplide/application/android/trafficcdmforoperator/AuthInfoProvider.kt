package org.deplide.application.android.trafficcdmforoperator

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration

class AuthInfoProvider private constructor(applicationContext: Context) {
    private var _serviceConfiguration: AuthorizationServiceConfiguration? = null
    private var _authState: AuthState? = null
    private var _authService: AuthorizationService = AuthorizationService(applicationContext)
    val serviceConfiguration
        get() = _serviceConfiguration!!
    val authState
        get() = _authState!!

    val authService
        get() = _authService

    init {
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse(REALM_BASE_URI)
        ) { serviceConfig, ex ->
            if (ex != null) {
                Log.d(TAG, ex.toString())
            }

            _serviceConfiguration = serviceConfig
            _authState = AuthState(serviceConfig!!)
        }
    }
    companion object {
        private const val TAG = "AuthInfoProvider"
        private const val REALM_BASE_URI = "https://id.deplide.org/realms/deplide"
        const val CLIENT_ID = "tcmf-submission-app"
        val REDIRECT_URI = "org.deplide.application.android.trafficcdmforoperator:/oauth2redirect".toUri()
        val END_SESSION_REDIRECT_URI = "org.deplide.application.android.trafficcdmforoperator:/oauth2redirect".toUri()

        private var instance: AuthInfoProvider? = null

        fun instance(applicationContext: Context): AuthInfoProvider {
            if (instance == null) {
                instance = AuthInfoProvider(applicationContext)
            }

             return instance!!
        }
    }
}