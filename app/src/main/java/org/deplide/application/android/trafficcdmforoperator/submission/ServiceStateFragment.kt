package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentServiceStateBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimePicker

class ServiceStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentServiceStateBinding
    private var initialData: SubmissionData? = null
    private var editMode: String? = null
    private lateinit var dateTimePicker: DateTimePicker
    private val listPopupWindow by lazy { ListPopupWindow(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initialData = BundleCompat.getParcelable(
                it,
                SubmitTimestampFragment.CHILD_ARGUMENT_INITIAL_DATA,
                SubmissionData::class.java
            )

            editMode = it.getString(
                SubmitTimestampFragment.CHILD_ARGUMENT_EDIT_MODE
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServiceStateBinding.inflate(inflater, container, false)
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
            edtTimeServiceState.isEnabled = isEnabled
            txtInputLayoutTimeServiceState.isEndIconVisible = isEnabled
            radioGroupTimeTypeServiceState.isEnabled = isEnabled
            radioBtnServiceStatePlanned.isEnabled = isEnabled
            radioBtnServiceStateEstimated.isEnabled = isEnabled
            radioBtnServiceStateActual.isEnabled = isEnabled
            edtServiceServiceState.isEnabled = isEnabled
            edtLocationServiceState.isEnabled = isEnabled
            edtReferenceObjectServiceState.isEnabled = isEnabled
        }
    }

    private fun isTheFieldEnabled(): Boolean {
        return if (editMode == null ||
            editMode == SubmitTimestampFragment.EDIT_MODE_UNDO_MESSAGE
        ) {
            false
        } else {
            true
        }
    }

    private fun configureListeners() {
        binding.apply {

            edtTimeServiceState.addTextChangedListener(

                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME, text!!.toString())
                }
            )

            updateData(SubmissionData.FIELD_TIME_TYPE,
                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
            radioGroupTimeTypeServiceState
                .setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.radioBtnServiceStatePlanned -> {
                            updateData(SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_planned).replaceFirstChar { it.lowercase() })
                        }

                        R.id.radioBtnServiceStateEstimated -> {
                            updateData(SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_estimated).replaceFirstChar { it.lowercase() })
                        }

                        R.id.radioBtnServiceStateActual -> {
                            updateData(SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
                        }
                    }
                }

            txtInputLayoutTimeServiceState.setEndIconOnClickListener {
                dateTimePicker = DateTimePicker(
                    getString(R.string.date_time_pattern),
                    DateFormat.is24HourFormat(requireContext()),
                    childFragmentManager
                ) { dateTime ->
                    edtTimeServiceState.setText(dateTime)
                    updateData(SubmissionData.FIELD_TIME, dateTime)
                }
                dateTimePicker.show()
            }
            edtServiceServiceState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_SERVICE, text!!.toString())
                }
            )

            listPopupWindow.anchorView = edtLocationServiceState
            edtLocationServiceState.setOnFocusChangeListener { _, hasFocus ->
                listPopupWindow.dismiss()
                if (hasFocus) {
                    loadLocationTypesToListPopupWindow()
                }
            }
            edtLocationServiceState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    listPopupWindow.dismiss()

                    val locationArray = text.toString().split(":")

                    if (locationArray.size < 4) {
                        loadLocationTypesToListPopupWindow()
                    } else if (locationArray.size == 4) {
                        loadLocationsToListPopupWindow(
                            "${locationArray[0]}:${locationArray[1]}:${locationArray[2]}",
                            locationArray[3].replaceFirstChar { it.uppercase() })
                    }
                }
            )

            edtReferenceObjectServiceState.addTextChangedListener(
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

            edtTimeServiceState.setText(currentDateTime)
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
                edtTimeServiceState.setText(localTime)
                updateData(SubmissionData.FIELD_TIME, localTime)

                when (initialData!!.timeType) {
                    getString(R.string.time_type_planned)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(ServiceStateFragment.TAG, "loadInitialData: planned")
                        radioGroupTimeTypeServiceState.check(R.id.radioBtnServiceStatePlanned)
                    }

                    getString(R.string.time_type_estimated)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(ServiceStateFragment.TAG, "loadInitialData: estimated")
                        radioGroupTimeTypeServiceState.check(R.id.radioBtnServiceStateEstimated)
                    }

                    getString(R.string.time_type_actual)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(ServiceStateFragment.TAG, "loadInitialData: actual")
                        radioGroupTimeTypeServiceState.check(R.id.radioBtnServiceStateActual)
                    }

                    else -> {
                        Log.d(
                            ServiceStateFragment.TAG,
                            "loadInitialData: ${initialData!!.timeType}"
                        )
                        radioGroupTimeTypeServiceState.check(R.id.radioBtnServiceStateActual)
                    }
                }
                updateData(SubmissionData.FIELD_TIME_TYPE, initialData!!.timeType!!)

                edtServiceServiceState.setText(initialData!!.service)
                updateData(SubmissionData.FIELD_SERVICE, initialData!!.service!!)

                edtLocationServiceState.setText(initialData!!.location)
                updateData(SubmissionData.FIELD_LOCATION, initialData!!.location!!)

                edtReferenceObjectServiceState.setText(initialData!!.referenceObject)
                updateData(SubmissionData.FIELD_REFERENCE_OBJECT, initialData!!.referenceObject!!)
            }
        }
    }

    private fun loadLocationsToListPopupWindow(
        locationPrefix: String,
        locationName: String
    ) {
        val items = locations.keys.toList().filter {
            it.contains(locationName)
        }
        if (items.isNotEmpty()) {
            val adapter = ArrayAdapter<String>(
                requireContext(),
                R.layout.cell_location,
                items
            )
            listPopupWindow.setAdapter(adapter)

            listPopupWindow.setOnItemClickListener { _, _, position: Int, _ ->
                // Respond to list popup window item click.
                val locationAbbreviation = locations[items[position]]
                val location = "$locationPrefix:$locationAbbreviation"

                binding.edtLocationServiceState.setText(location)
                updateData(SubmissionData.FIELD_LOCATION, location)

                // Dismiss popup.
                listPopupWindow.dismiss()
            }

            listPopupWindow.show()
        }
    }

    private fun loadLocationTypesToListPopupWindow() {
        val locationPrefixes =
            resources.getStringArray(R.array.predefined_location_prefix)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.cell_location,
            locationPrefixes
        )
        listPopupWindow.setAdapter(adapter)

        listPopupWindow.setOnItemClickListener { _, _, position: Int, _ ->
            // Respond to list popup window item click.
            val locationPrefix = locationPrefixes[position]

            binding.edtLocationServiceState.setText(locationPrefix)
            updateData(SubmissionData.FIELD_LOCATION, locationPrefix)

            listPopupWindow.dismiss()
        }

        listPopupWindow.show()
    }

    companion object {
        private const val TAG = "ServiceStateFragment"
    }
}