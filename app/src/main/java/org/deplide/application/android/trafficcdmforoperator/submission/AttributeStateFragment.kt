package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentAttributeStateBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimePicker

class AttributeStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentAttributeStateBinding
    private var initialData: SubmissionData? = null
    private var editMode: String? = null
    private lateinit var dateTimePicker: DateTimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initialData = BundleCompat.getParcelable(
                it,
                SubmitTimestampFragment.CHILD_ARGUMENT_INITIAL_DATA,
                SubmissionData::class.java)

            editMode = it.getString(
                SubmitTimestampFragment.CHILD_ARGUMENT_EDIT_MODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAttributeStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCurrentTimeForTimeField()
        configureListeners()
        loadInitialData()
        configureAccordingToEditMode()
    }

    private fun configureAccordingToEditMode() {
        val isEnabled = isTheFieldEnabled()

        binding.apply {
            edtTimeAttributeState.isEnabled = isEnabled
            txtInputLayoutTimeAttributeState.isEndIconVisible = isEnabled
            radioGroupTimeTypeAttributeState.isEnabled = isEnabled
            radioBtnAttributeStatePlanned.isEnabled = isEnabled
            radioBtnAttributeStateEstimated.isEnabled = isEnabled
            radioBtnAttributeStateActual.isEnabled = isEnabled
            edtLocationAttributeState.isEnabled = isEnabled
            edtReferenceObjectAttributeState.isEnabled = isEnabled
        }
    }

    private fun isTheFieldEnabled(): Boolean {
        return if (editMode == null ||
            editMode == SubmitTimestampFragment.EDIT_MODE_UNDO_MESSAGE) {
            false
        } else {
            true
        }
    }

    private fun configureListeners() {
        binding.apply {

            edtTimeAttributeState.addTextChangedListener(

                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME, text!!.toString())
                }
            )

            updateData(
                SubmissionData.FIELD_TIME_TYPE,
                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
            radioGroupTimeTypeAttributeState
                .setOnCheckedChangeListener{ _, checkedId ->
                    when (checkedId) {
                        R.id.radioBtnAttributeStatePlanned -> {
                            updateData(
                                SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_planned).replaceFirstChar { it.lowercase() })
                        }
                        R.id.radioBtnAttributeStateEstimated -> {
                            updateData(
                                SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_estimated).replaceFirstChar { it.lowercase() })
                        }
                        R.id.radioBtnAttributeStateActual -> {
                            updateData(
                                SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
                        }
                    }
                }

            edtAttributeAttributeState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_ATTRIBUTE, text!!.toString())
                }
            )

            txtInputLayoutTimeAttributeState.setEndIconOnClickListener {
                dateTimePicker = DateTimePicker(
                    getString(R.string.date_time_pattern),
                    DateFormat.is24HourFormat(requireContext()),
                    childFragmentManager
                ) {dateTime ->
                    edtTimeAttributeState.setText(dateTime)
                    updateData(SubmissionData.FIELD_TIME, dateTime)
                }
                dateTimePicker.show()
            }

            edtLocationAttributeState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_LOCATION, text!!.toString())
                }
            )

            edtReferenceObjectAttributeState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_REFERENCE_OBJECT, text!!.toString())
                }
            )
        }
    }

    private fun setCurrentTimeForTimeField() {
        binding.apply {
            val currentDateTime =
                DateTimeHelper.getCurrentDateTime(format = getString(R.string.date_time_pattern))

            edtTimeAttributeState.setText(currentDateTime)
            updateData(SubmissionData.FIELD_TIME, currentDateTime)
        }
    }

    private fun loadInitialData() {
        if (initialData != null) {
            binding.apply {
                val localTime = DateTimeHelper.convertUTCTimeToSystemDefault(
                    initialData!!.time!!,
                    getString(R.string.date_time_pattern)
                )
                edtTimeAttributeState.setText(localTime)
                updateData(SubmissionData.FIELD_TIME, localTime)

                when (initialData!!.timeType) {
                    getString(R.string.time_type_planned)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(TAG, "loadInitialData: planned")
                        radioGroupTimeTypeAttributeState.check(R.id.radioBtnAttributeStatePlanned)
                    }
                    getString(R.string.time_type_estimated)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(TAG, "loadInitialData: estimated")
                        radioGroupTimeTypeAttributeState.check(R.id.radioBtnAttributeStateEstimated)
                    }
                    getString(R.string.time_type_actual)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(TAG, "loadInitialData: actual")
                        radioGroupTimeTypeAttributeState.check(R.id.radioBtnAttributeStateActual)
                    }
                    else -> {
                        Log.d(TAG, "loadInitialData: ${initialData!!.timeType}")
                        radioGroupTimeTypeAttributeState.check(R.id.radioBtnAttributeStateActual)
                    }
                }
                updateData(SubmissionData.FIELD_TIME_TYPE, initialData!!.timeType!!)

                edtAttributeAttributeState.setText(initialData!!.attribute)
                updateData(SubmissionData.FIELD_ATTRIBUTE, initialData!!.attribute!!)

                initialData!!.location?.run {
                    edtLocationAttributeState.setText(initialData!!.location)
                    updateData(SubmissionData.FIELD_LOCATION, initialData!!.location!!)
                }

                edtReferenceObjectAttributeState.setText(initialData!!.referenceObject)
                updateData(SubmissionData.FIELD_REFERENCE_OBJECT, initialData!!.referenceObject!!)
            }
        }
    }

    companion object {
        private const val TAG = "AttributeStateFragment"
    }
}