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
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Calendar
import java.util.TimeZone


class SubmissionActivity : AppCompatActivity(), StateFragmentDataUpdateListener {
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
    private var submissionData: SubmissionData? = null

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

            viewModel.submitTCMFMessage(submissionData!!, accessToken!!)
        }
    }

    private fun configureStateDropdownList() {
        binding.edtState.setOnItemClickListener { _, _, position, _ ->
            val timeSequence = resources.getStringArray(R.array.time_sequence)[position]
            currentEditingState = getStateFromTimeSequence(timeSequence)
            newTCMFMessage(
                type = currentEditingState,
                timeSequence = timeSequence
            )

            when (currentEditingState) {
                "LocationState" -> {
                    Log.d(TAG, "LocationState")
                    val fragment = LocationStateFragment()
                    fragment.addStateFragmentDataUpdateListener(this)
                    supportFragmentManager.beginTransaction().replace(R.id.navHost,
                        fragment).commit()
                }
                "AdministrativeState" -> {
                    Log.d(TAG, "AdministrativeState")
                    val fragment = AdministrativeStateFragment()
                    fragment.addStateFragmentDataUpdateListener(this)
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

    private fun newTCMFMessage(type: String, timeSequence: String) {
        submissionData = SubmissionData(
            type = type,
            timeSequence = timeSequence)
        if (submissionData?.type == SubmissionData.TYPE_MESSAGE_OPERATION) {
            submissionData?.operation = "invalidate"
        }
    }

    override fun onStateFragmentDataUpdate(data: Map<String, String>) {
        data.forEach{entry ->
            when(entry.key) {
                SubmissionData.FIELD_TIME -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.time = convertDateTimeToIso8601(
                        entry.value, getString(R.string.date_time_pattern))
                }
                SubmissionData.FIELD_LOCATION -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.location = entry.value
                }
                SubmissionData.FIELD_TIME_TYPE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.timeType = entry.value
                }
                SubmissionData.FIELD_TIME_SEQUENCE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.timeSequence = entry.value
                }
                SubmissionData.FIELD_REFERENCE_OBJECT -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.referenceObject = entry.value
                }
                SubmissionData.FIELD_SERVICE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.service = entry.value
                }
                SubmissionData.FIELD_CARRIER -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.carrier = entry.value
                }
            }
        }
    }

    private fun convertDateTimeToIso8601(dateTime: String, dateTimePattern: String): String {
        var result = ""

        try {
            val parser = DateTimeFormatter
                .ofPattern(dateTimePattern)

            val localDateTime = LocalDateTime
                .parse(dateTime, parser)
                .atZone(ZoneId.of(Calendar.getInstance().timeZone.id))
            Log.d(TAG, "localDateTime: $localDateTime")

            val dateTimeUTC = localDateTime.withZoneSameInstant(ZoneId.of("UTC"))
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            result = outputFormatter.format(dateTimeUTC)

            Log.d(TAG, "result: $result")
        } catch (e: DateTimeParseException) {
            Log.d(TAG, "DateTimeParseException: $e")
        }

        return result
    }

    companion object {
        private const val TAG = "SubmissionActivity"
        fun intent(srcCtx: Context): Intent {
            return Intent(srcCtx, SubmissionActivity::class.java)
        }
    }
}