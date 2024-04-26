package org.deplide.application.android.trafficcdmforoperator.submission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.EndSessionRequest
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider.Companion.END_SESSION_REDIRECT_URI
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.TrafficCDMForOperatorApplication
import org.deplide.application.android.trafficcdmforoperator.databinding.ActivitySubmissionBinding
import org.deplide.application.android.trafficcdmforoperator.login.LoginActivity


class SubmissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubmissionBinding
    private lateinit var logOutActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var _authInfoProvider: AuthInfoProvider
    private val authState
        get() = _authInfoProvider.authState
    private val authService
        get() = _authInfoProvider.authService
    private val serviceConfiguration
        get() = _authInfoProvider.serviceConfiguration

    private val viewModel: SubmissionViewModel by viewModels { SubmissionViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logOutActivityLauncher = setupLogOutActivityLauncher()

        configureActionBar()
    }

    override fun onResume() {
        super.onResume()

        _authInfoProvider = (application as TrafficCDMForOperatorApplication).authInfoProvider
    }

    private fun configureActionBar() {
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.log_out_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        performActionWithFreshToken(::actionLogOut)
        return super.onOptionsItemSelected(item)
    }

    private fun performActionWithFreshToken(action: (idToken: String) -> Unit) {
        authState.performActionWithFreshTokens(authService) { _, idToken, ex ->
            if (ex != null) {
                // negotiation for fresh tokens failed, check ex for more details
                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show()
            }

            action(idToken!!)
        }
    }
    private fun actionLogOut(idToken: String) {
        val logOutIntent = authService.getEndSessionRequestIntent(getEndSessionRequest(idToken))
        logOutActivityLauncher.launch(logOutIntent)
    }

    private fun getEndSessionRequest(idToken: String): EndSessionRequest {
        return EndSessionRequest.Builder(serviceConfiguration)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(END_SESSION_REDIRECT_URI)
            .build()
    }

    private fun setupLogOutActivityLauncher() = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (authState.lastRegistrationResponse != null) {
                authState.update(authState.lastRegistrationResponse)
            }

            startActivity(LoginActivity.intent(this))
        }
    }

    companion object {
        private const val TAG = "SubmissionActivity"
        fun intent(srcCtx: Context): Intent {
            return Intent(srcCtx, SubmissionActivity::class.java)
        }
    }
}