package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.TrafficCDMForOperatorApplication
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentSubmitTimestampBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Calendar

class SubmitTimestampFragment : Fragment(), StateFragmentDataUpdateListener {
    private lateinit var binding: FragmentSubmitTimestampBinding
    private val viewModel: SubmitTimestampViewModel by viewModels { SubmitTimestampViewModel.factory() }
    private var submissionData: SubmissionData? = null
    private val _authInfoProvider: AuthInfoProvider by lazy {
        (requireActivity().application as TrafficCDMForOperatorApplication).authInfoProvider
    }
    private val authState
        get() = _authInfoProvider.authState
    private val authService
        get() = _authInfoProvider.authService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSubmitTimestampBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show()
            }

            viewModel.submitTCMFMessage(submissionData!!, accessToken!!)
        }
    }

    private fun configureStateDropdownList() {
        binding.edtState.setOnItemClickListener { _, _, position, _ ->
            val timeSequence = resources.getStringArray(R.array.time_sequence)[position]
            val currentEditingState = getStateFromTimeSequence(timeSequence)
            newTCMFMessage(
                type = currentEditingState,
                timeSequence = timeSequence
            )

            when (currentEditingState) {
                "LocationState" -> {
                    Log.d(TAG, "LocationState")
                    val fragment = LocationStateFragment()
                    fragment.addStateFragmentDataUpdateListener(this)
                    childFragmentManager.beginTransaction().replace(R.id.navHost,
                        fragment).commit()
                }
                "AdministrativeState" -> {
                    Log.d(TAG, "AdministrativeState")
                    val fragment = AdministrativeStateFragment()
                    fragment.addStateFragmentDataUpdateListener(this)
                    childFragmentManager.beginTransaction().replace(R.id.navHost,
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
        private const val TAG = "SubmitTimestampFragment"
    }
}