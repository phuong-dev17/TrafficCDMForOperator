package org.deplide.application.android.trafficcdmforoperator.submission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var currentEditingState = ""
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

        configureStateDropdownList()

        configSubmitButton()
    }

    private fun configSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            submitTCMFMessage()
        }
    }

    private fun submitTCMFMessage() {
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            if (ex != null) {
                // negotiation for fresh tokens failed, check ex for more details
                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show()
            }

            viewModel.submitTCMFMessage(accessToken!!)
        }
    }

    private fun configureStateDropdownList() {
        binding.edtState.setOnItemClickListener { _, _, position, _ ->
            val timeSequence = resources.getStringArray(R.array.time_sequence)[position]
            currentEditingState = getStateFromTimeSequence(timeSequence)
            viewModel.newTCMFMessage(
                type = currentEditingState,
                timeSequence = timeSequence
            )

            when (currentEditingState) {
                "LocationState" -> {
                    Log.d(TAG, "LocationState")
                    val fragment = LocationStateFragment()
                    fragment.addStateFragmentDataUpdateListener(viewModel)
                    supportFragmentManager.beginTransaction().replace(R.id.navHost,
                        fragment).commit()
                }
                "AdministrativeState" -> {
                    Log.d(TAG, "AdministrativeState")
                    val fragment = AdministrativeStateFragment()
                    fragment.addStateFragmentDataUpdateListener(viewModel)
                    supportFragmentManager.beginTransaction().replace(R.id.navHost,
                        fragment).commit()
                }
                else -> {
                    Log.d(TAG, "Unhandled State")
                }
            }
        }
    }

    private fun getStateFromTimeSequence(timeSequence: String): String {
        return when (timeSequence) {
            "arrived_to" -> "LocationState"
            "departed_from" -> "LocationState"
            "passed_by" -> "LocationState"

            "requested" -> "AdministrativeState"
            "request_received" -> "AdministrativeState"
            "confirmed" -> "AdministrativeState"
            "cancelled" -> "AdministrativeState"
            "denied" -> "AdministrativeState"
            "assigned" -> "AdministrativeState"

            "commenced" -> "ServiceState"
            "completed" -> "ServiceState"

            "bound_to" -> "CarrierState"
            "unbound_from" -> "CarrierState"

            "set" -> "AttributeState"
            "unset" -> "AttributeState"

            else -> {
                Log.d(TAG, "Unhandled State")
                ""
            }
        }
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
        logout()
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        authState.performActionWithFreshTokens(authService) { _, idToken, ex ->
            if (ex != null) {
                // negotiation for fresh tokens failed, check ex for more details
                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show()
            }

            val logOutIntent = authService.getEndSessionRequestIntent(getEndSessionRequest(idToken!!))
            logOutActivityLauncher.launch(logOutIntent)
        }
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