package org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.deplide.application.android.trafficcdmforoperator.AuthInfoProvider
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.TrafficCDMForOperatorApplication
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentSubmitTimestampBinding
import org.deplide.application.android.trafficcdmforoperator.hideKeyboard
import org.deplide.application.android.trafficcdmforoperator.submission.AdministrativeStateFragment
import org.deplide.application.android.trafficcdmforoperator.submission.AttributeStateFragment
import org.deplide.application.android.trafficcdmforoperator.submission.CarrierStateFragment
import org.deplide.application.android.trafficcdmforoperator.submission.LocationStateFragment
import org.deplide.application.android.trafficcdmforoperator.submission.ServiceStateFragment
import org.deplide.application.android.trafficcdmforoperator.submission.StateFragmentDataUpdateListener
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Calendar


class SubmitTimestampFragment : Fragment(), StateFragmentDataUpdateListener {
    private lateinit var binding: FragmentSubmitTimestampBinding
    private val viewModel: SubmitTimestampViewModel by viewModels {
        SubmitTimestampViewModel.factory(requireContext()) }
    private var submissionData: SubmissionData? = null
    private val _authInfoProvider: AuthInfoProvider by lazy {
        (requireActivity().application as TrafficCDMForOperatorApplication).authInfoProvider
    }
    private val authState
        get() = _authInfoProvider.authState
    private val authService
        get() = _authInfoProvider.authService

    private lateinit var navController: NavController
    private var editMode: String? = null
    private var messageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            Log.d(TAG, "onCreate: $it")
            messageId = it.getString(ARGUMENT_MSG_ID)
            editMode = it.getString(ARGUMENT_EDIT_MODE)
        }

        if (messageId != null) {
            Log.d(TAG, "onCreate: loadMessage: messageId: $messageId")
            viewModel.loadMessage(messageId!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSubmitTimestampBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateBackToSubmissionOverview()
                }
            }
        )

        configureAccordingToEditMode()

        configureStateDropdownList()
        configBackButton()
        configProceedButton()

        observerUiState()
    }

    private fun configureAccordingToEditMode() {
        binding.edtState.isEnabled = if (editMode == null || editMode == EDIT_MODE_UNDO_MESSAGE) {
            false
        } else {
            true
        }

        if (editMode == null) {
            binding.btnProceed.visibility =  View.GONE
            binding.btnBack.visibility = View.VISIBLE

            configureBackButtonWidthToMatchParent()
        } else {
            binding.btnProceed.visibility = View.VISIBLE
        }

        if (editMode == EDIT_MODE_MODIFY_MESSAGE) {
            binding.btnProceed.setText(R.string.modify_message)
        } else if (editMode == EDIT_MODE_UNDO_MESSAGE) {
            binding.btnProceed.setText(R.string.undo_message)
            binding.btnProceed.setBackgroundColor(getColor(requireContext(), R.color.warning))
        }
    }

    private fun configureBackButtonWidthToMatchParent() {
        val params: LayoutParams = binding.btnBack.layoutParams as LayoutParams
        params.marginEnd = 0
        binding.btnBack.setLayoutParams(params)

        val parentConstraintLayout: ConstraintLayout = binding.controlButtonGroup
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentConstraintLayout)
        constraintSet.connect(
            binding.btnBack.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        constraintSet.applyTo(parentConstraintLayout)
    }

    private fun observerUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is SubmitTmestampUIState.Idle -> onIdle(uiState.initialData)
                        SubmitTmestampUIState.Processing -> onProcessing()
                        SubmitTmestampUIState.Success -> onSuccess()
                        is SubmitTmestampUIState.Error -> onError(uiState.message)
                        SubmitTmestampUIState.SuccessUndo -> onSuccessUndo()
                    }
                }
            }
        }
    }

    private fun onSuccessUndo() {
        if (editMode == EDIT_MODE_UNDO_MESSAGE) {
            navigateBackToSubmissionOverview()
        } else {
            submitTCMFMessage(submissionData!!)
        }
    }

    private fun onIdle(initialData: SubmissionData?) {
        binding.idleView.visibility = View.VISIBLE
        binding.processingView.visibility = View.GONE

        initialData?.let {data ->
            binding.apply {
                val timeSequence = data.timeSequence!!
                edtState.setText(timeSequence)

                loadStateFragment(timeSequence, data)
            }
        }
    }

    private fun onProcessing() {
        binding.idleView.visibility = View.GONE
        binding.processingView.visibility = View.VISIBLE
    }

    private fun onSuccess() {
        requireContext().hideKeyboard(binding.root)
        binding.idleView.visibility = View.GONE
        binding.processingView.visibility = View.GONE
        Snackbar.make(binding.root, "Timestamp submitted successfully", Snackbar.LENGTH_SHORT)
            .show()
        navigateBackToSubmissionOverview()
    }

    private fun navigateBackToSubmissionOverview() {
        val action =
            SubmitTimestampFragmentDirections.actionSubmitTimestampFragmentToSubmissionOverviewFragment()

        navController.navigate(action)
    }

    private fun onError(message: String) {
        binding.processingView.visibility = View.GONE
        binding.idleView.visibility = View.VISIBLE
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun configProceedButton() {
        binding.btnProceed.setOnClickListener {
            if (editMode == EDIT_MODE_UNDO_MESSAGE || editMode == EDIT_MODE_MODIFY_MESSAGE) {
                Log.d(TAG, "undoMessage: $messageId")
                submitUndoMessage(messageId!!)
            } else {
                submitTCMFMessage(submissionData!!)
            }
        }
    }

    private fun configBackButton() {
        binding.btnBack.setOnClickListener {
            navigateBackToSubmissionOverview()
        }
    }

    private fun submitUndoMessage(messageId: String, updateUI: Boolean = false) {
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            if (ex != null) {
                // negotiation for fresh tokens failed, check ex for more details
                Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show()
            }

            viewModel.undoMessage(messageId, accessToken!!)
        }
    }

    private fun submitTCMFMessage(data: SubmissionData) {
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            if (ex != null) {
                // negotiation for fresh tokens failed, check ex for more details
                Toast.makeText(requireContext(), ex.toString(), Toast.LENGTH_LONG).show()
            }

            viewModel.submitTCMFMessage(data, accessToken!!)
        }
    }

    private fun configureStateDropdownList() {
        binding.edtState.addTextChangedListener {
            val timeSequence = it.toString()
            val validTimeSequences = resources.getStringArray(R.array.time_sequence)

            if (timeSequence in validTimeSequences) {
                loadStateFragment(timeSequence)
            }
        }
    }

    private fun loadStateFragment(timeSequence: String, initialData: SubmissionData? = null) {
        val currentEditingState = getStateFromTimeSequence(timeSequence)

        submissionData = SubmissionData(type = currentEditingState)
        submissionData!!.timeSequence = timeSequence

        val bundle = Bundle()
        bundle.putParcelable(CHILD_ARGUMENT_INITIAL_DATA, initialData)
        bundle.putString(CHILD_ARGUMENT_EDIT_MODE, editMode)

        when (currentEditingState) {
            "LocationState" -> {
                Log.d(TAG, "LocationState")
                val fragment = LocationStateFragment()
                fragment.addStateFragmentDataUpdateListener(this)
                fragment.arguments = bundle
                childFragmentManager.beginTransaction().replace(R.id.navHost,
                    fragment).commit()
            }
            "AdministrativeState" -> {
                Log.d(TAG, "AdministrativeState")
                val fragment = AdministrativeStateFragment()
                fragment.addStateFragmentDataUpdateListener(this)
                fragment.arguments = bundle
                childFragmentManager.beginTransaction().replace(R.id.navHost,
                    fragment).commit()
            }
            "ServiceState" -> {
                Log.d(TAG, "ServiceState")
                val fragment = ServiceStateFragment()
                fragment.addStateFragmentDataUpdateListener(this)
                fragment.arguments = bundle
                childFragmentManager.beginTransaction().replace(R.id.navHost,
                    fragment).commit()
            }
            "CarrierState" -> {
                Log.d(TAG, "CarrierState")
                val fragment = CarrierStateFragment()
                fragment.addStateFragmentDataUpdateListener(this)
                fragment.arguments = bundle
                childFragmentManager.beginTransaction().replace(R.id.navHost,
                    fragment).commit()
            }
            "AttributeState" -> {
                Log.d(TAG, "AttributeState")
                val fragment = AttributeStateFragment()
                fragment.addStateFragmentDataUpdateListener(this)
                fragment.arguments = bundle
                childFragmentManager.beginTransaction().replace(R.id.navHost,
                    fragment).commit()
            }
            else -> {
                Log.d(TAG, "Unhandled State")
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
                SubmissionData.FIELD_ATTRIBUTE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.attribute = entry.value
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

        const val EDIT_MODE_NEW_MESSAGE = "New Message"
        const val EDIT_MODE_MODIFY_MESSAGE = "Modify Message"
        const val EDIT_MODE_UNDO_MESSAGE = "Undo Message"

        const val ARGUMENT_MSG_ID = "messageId"
        const val ARGUMENT_EDIT_MODE = "editMode"

        const val CHILD_ARGUMENT_INITIAL_DATA = "initialData"
        const val CHILD_ARGUMENT_EDIT_MODE = "editMode"
    }
}