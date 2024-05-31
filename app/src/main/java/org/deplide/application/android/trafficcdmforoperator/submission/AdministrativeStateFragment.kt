package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentAdministrativeStateBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimePicker

class AdministrativeStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentAdministrativeStateBinding
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
    ): View? {
        binding = FragmentAdministrativeStateBinding.inflate(inflater, container, false)
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
            edtTimeAdministrativeState.isEnabled = isEnabled
            txtInputLayoutTimeAdministrativeState.isEndIconVisible = isEnabled
            edtServiceAdministrativeState.isEnabled = isEnabled
            edtLocationAdministrativeState.isEnabled = isEnabled
            edtReferenceObjectAdministrativeState.isEnabled = isEnabled
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

            edtTimeAdministrativeState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME, text!!.toString())
                }
            )

            txtInputLayoutTimeAdministrativeState.setEndIconOnClickListener {
                dateTimePicker = DateTimePicker(
                    getString(R.string.date_time_pattern),
                    DateFormat.is24HourFormat(requireContext()),
                    childFragmentManager
                ) {dateTime ->
                    edtTimeAdministrativeState.setText(dateTime)
                    updateData(SubmissionData.FIELD_TIME, dateTime)
                }
                dateTimePicker.show()
            }

            edtServiceAdministrativeState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_SERVICE, text!!.toString())
                }
            )

            edtLocationAdministrativeState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_LOCATION, text!!.toString())
                }
            )

            edtReferenceObjectAdministrativeState.addTextChangedListener(
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

            edtTimeAdministrativeState.setText(currentDateTime)
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
                edtTimeAdministrativeState.setText(localTime)
                updateData(SubmissionData.FIELD_TIME, localTime)

                edtServiceAdministrativeState.setText(initialData!!.service!!)
                updateData(SubmissionData.FIELD_SERVICE, initialData!!.service!!)

                initialData!!.location?.run {
                    edtLocationAdministrativeState.setText(initialData!!.location)
                    updateData(SubmissionData.FIELD_LOCATION, initialData!!.location!!)
                }

                initialData!!.referenceObject?.run {
                    edtReferenceObjectAdministrativeState.setText(initialData!!.referenceObject)
                    updateData(
                        SubmissionData.FIELD_REFERENCE_OBJECT,
                        initialData!!.referenceObject!!
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "AdministrativeStateFragment"
    }
}