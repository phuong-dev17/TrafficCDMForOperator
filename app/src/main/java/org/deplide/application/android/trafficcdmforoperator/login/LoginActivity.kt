package org.deplide.application.android.trafficcdmforoperator.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import org.deplide.application.android.trafficcdmforoperator.databinding.ActivityLoginBinding
import org.deplide.application.android.trafficcdmforoperator.submission.SubmissionActivity
import org.deplide.application.android.trafficcdmforoperator.toolbox.AuthToolBox


class LoginActivity : AppCompatActivity() {
    private val authToolBox = AuthToolBox.getInstance()
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authService: AuthorizationService
    private lateinit var startForResult: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authService = AuthorizationService(this)
        binding.btnLogin.setOnClickListener {
            doAuthorization()
        }
    }

    override fun onStart() {
        super.onStart()

        startForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Process the data returned by the launched activity
                val data = result.data

                val resp = AuthorizationResponse.fromIntent(data!!)
                val ex = AuthorizationException.fromIntent(data)

                authToolBox.authState.update(resp, ex)

                if (resp != null) {
                    doTokenRequest(resp)
                } else {
                    Log.e(TAG, "Authorization EX: $ex")
                }
            }
        }
    }

    private fun doAuthorization() {
        val authIntent = authService.getAuthorizationRequestIntent(authToolBox.getAuthRequest())
        startForResult.launch(authIntent)
    }

    private fun doTokenRequest(authResp: AuthorizationResponse) {
        authService.performTokenRequest(
            authResp.createTokenExchangeRequest()
        ) { tokenResp, ex ->
            if (tokenResp != null) {
                authToolBox.authState.update(tokenResp, ex)

                startActivity(SubmissionActivity.intent(this))

                finish()
            } else {
                Log.e(TAG, "Authorization EX: $ex")
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}