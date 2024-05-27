package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentLocationStateBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper.Companion.convertUTCTimeToSystemDefault
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper.Companion.getCurrentDateTime
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimePicker

class LocationStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentLocationStateBinding
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
        binding = FragmentLocationStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureAccordingToEditMode()
        setCurrentTimeForTimeField()
        configureListeners()
        loadInitialData()
    }

    private fun configureAccordingToEditMode() {
        val isEnabled = isTheFieldEnabled()

        binding.apply {
            edtTimeLocationState.isEnabled = isEnabled
            edtTimeTypeLocationState.isEnabled = isEnabled
            edtLocationLocationState.isEnabled = isEnabled
            edtReferenceObjectLocationState.isEnabled = isEnabled
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

            edtTimeLocationState.addTextChangedListener(

                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME, text!!.toString())
                }
            )

            edtTimeTypeLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME_TYPE, text!!.toString())
                }
            )

            txtInputLayoutTimeLocationState.setEndIconOnClickListener {
                dateTimePicker = DateTimePicker(
                    getString(R.string.date_time_pattern),
                    is24HourFormat(requireContext()),
                    childFragmentManager
                ) {dateTime ->
                    edtTimeLocationState.setText(dateTime)
                    updateData(SubmissionData.FIELD_TIME, dateTime)
                }
                dateTimePicker.show()
            }

            edtLocationLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_LOCATION, text!!.toString())
                }
            )

            edtReferenceObjectLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_REFERENCE_OBJECT, text!!.toString())
                }
            )
        }
    }

    private fun setCurrentTimeForTimeField() {
        binding.apply {
            val currentDateTime = getCurrentDateTime(format = getString(R.string.date_time_pattern))

            edtTimeLocationState.setText(currentDateTime)
            updateData(SubmissionData.FIELD_TIME, currentDateTime)
        }
    }

    private fun loadInitialData() {
        if (initialData != null) {
            binding.apply {
                val localTime = convertUTCTimeToSystemDefault(
                    initialData!!.time!!,
                    getString(R.string.date_time_pattern)
                )
                edtTimeLocationState.setText(localTime)
                updateData(SubmissionData.FIELD_TIME, localTime)

                edtTimeTypeLocationState.setText(initialData!!.timeType)
                updateData(SubmissionData.FIELD_TIME_TYPE, initialData!!.timeType!!)

                edtLocationLocationState.setText(initialData!!.location)
                updateData(SubmissionData.FIELD_LOCATION, initialData!!.location!!)

                edtReferenceObjectLocationState.setText(initialData!!.referenceObject)
                updateData(SubmissionData.FIELD_REFERENCE_OBJECT, initialData!!.referenceObject!!)
            }
        }
    }

    companion object {
        private const val TAG = "LocationStateFragment"
    }
}