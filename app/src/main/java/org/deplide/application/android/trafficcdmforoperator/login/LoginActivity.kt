package org.deplide.application.android.trafficcdmforoperator.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.ResponseTypeValues
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider.Companion.CLIENT_ID
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider.Companion.REDIRECT_URI
import org.deplide.application.android.trafficcdmforoperator.TrafficCDMForOperatorApplication
import org.deplide.application.android.trafficcdmforoperator.databinding.ActivityLoginBinding
import org.deplide.application.android.trafficcdmforoperator.submission.SubmissionActivity


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var logInActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var _authInfoProvider: AuthInfoProvider
    private val authState
        get() = _authInfoProvider.authState
    private val authService
        get() = _authInfoProvider.authService
    private val serviceConfiguration
        get() = _authInfoProvider.serviceConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logInActivityLauncher = setupLogInActivityLauncher()

        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    override fun onResume() {
        super.onResume()

        _authInfoProvider = (application as TrafficCDMForOperatorApplication).authInfoProvider
    }

    private fun login() {
        doAuthorizationRequest()
    }

    private fun doAuthorizationRequest() {
        val logInIntent = authService.getAuthorizationRequestIntent(getAuthRequest())
        logInActivityLauncher.launch(logInIntent)
    }

    private fun doTokenRequest(authResp: AuthorizationResponse) {
        authService.performTokenRequest(
            authResp.createTokenExchangeRequest()
        ) { tokenResp, ex ->
            if (tokenResp != null) {
                authState.update(tokenResp, ex)
                startActivity(SubmissionActivity.intent(this))
                finish()
            } else {
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLogInActivityLauncher() = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Process the data returned by the launched activity
            val data = result.data

            val resp = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)

            authState.update(resp, ex)

            if (resp != null) {
                doTokenRequest(resp)
            } else {
                Log.e(TAG, "Authorization EX: $ex")
            }
        }
    }

    private fun getAuthRequest(): AuthorizationRequest {
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
        private const val TAG = "LoginActivity"

        fun intent(srcContext: Context): Intent {
            return Intent(srcContext, LoginActivity::class.java)
        }
    }
}
