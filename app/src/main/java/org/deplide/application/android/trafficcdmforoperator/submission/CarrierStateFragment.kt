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
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentCarrierStateBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimePicker

class CarrierStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentCarrierStateBinding
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
                SubmissionData::class.java)

            editMode = it.getString(
                SubmitTimestampFragment.CHILD_ARGUMENT_EDIT_MODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCarrierStateBinding.inflate(inflater, container, false)
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
            edtTimeCarrierState.isEnabled = isEnabled
            txtInputLayoutTimeCarrierState.isEndIconVisible = isEnabled
            radioGroupTimeTypeCarrierState.isEnabled = isEnabled
            radioBtnCarrierStatePlanned.isEnabled = isEnabled
            radioBtnCarrierStateEstimated.isEnabled = isEnabled
            radioBtnCarrierStateActual.isEnabled = isEnabled
            edtLocationCarrierState.isEnabled = isEnabled
            edtReferenceObjectCarrierState.isEnabled = isEnabled
            edtCarrierCarrierState.isEnabled = isEnabled
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

            edtTimeCarrierState.addTextChangedListener(

                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME, text!!.toString())
                }
            )

            updateData(
                SubmissionData.FIELD_TIME_TYPE,
                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
            radioGroupTimeTypeCarrierState
                .setOnCheckedChangeListener{ _, checkedId ->
                    when (checkedId) {
                        R.id.radioBtnCarrierStatePlanned -> {
                            updateData(
                                SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_planned).replaceFirstChar { it.lowercase() })
                        }
                        R.id.radioBtnCarrierStateEstimated -> {
                            updateData(
                                SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_estimated).replaceFirstChar { it.lowercase() })
                        }
                        R.id.radioBtnCarrierStateActual -> {
                            updateData(
                                SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
                        }
                    }
                }

            txtInputLayoutTimeCarrierState.setEndIconOnClickListener {
                dateTimePicker = DateTimePicker(
                    getString(R.string.date_time_pattern),
                    DateFormat.is24HourFormat(requireContext()),
                    childFragmentManager
                ) {dateTime ->
                    edtTimeCarrierState.setText(dateTime)
                    updateData(SubmissionData.FIELD_TIME, dateTime)
                }
                dateTimePicker.show()
            }

            listPopupWindow.anchorView = edtLocationCarrierState
            edtLocationCarrierState.setOnFocusChangeListener { _, hasFocus ->
                listPopupWindow.dismiss()
                if (hasFocus) {
                    loadLocationTypesToListPopupWindow()
                }
            }
            edtLocationCarrierState.addTextChangedListener(
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

            edtReferenceObjectCarrierState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_REFERENCE_OBJECT, text!!.toString())
                }
            )

            edtCarrierCarrierState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_CARRIER, text!!.toString())
                }
            )
        }
    }

    private fun setCurrentTimeForTimeField() {
        binding.apply {
            val currentDateTime =
                DateTimeHelper.getCurrentDateTime(format = getString(R.string.date_time_pattern))

            edtTimeCarrierState.setText(currentDateTime)
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
                edtTimeCarrierState.setText(localTime)
                updateData(SubmissionData.FIELD_TIME, localTime)

                when (initialData!!.timeType) {
                    getString(R.string.time_type_planned)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(TAG, "loadInitialData: planned")
                        radioGroupTimeTypeCarrierState.check(R.id.radioBtnCarrierStatePlanned)
                    }
                    getString(R.string.time_type_estimated)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(TAG, "loadInitialData: estimated")
                        radioGroupTimeTypeCarrierState.check(R.id.radioBtnCarrierStateEstimated)
                    }
                    getString(R.string.time_type_actual)
                        .replaceFirstChar { it.lowercase() } -> {
                        Log.d(TAG, "loadInitialData: actual")
                        radioGroupTimeTypeCarrierState.check(R.id.radioBtnCarrierStateActual)
                    }
                    else -> {
                        Log.d(TAG, "loadInitialData: ${initialData!!.timeType}")
                        radioGroupTimeTypeCarrierState.check(R.id.radioBtnCarrierStateActual)
                    }
                }
                updateData(SubmissionData.FIELD_TIME_TYPE, initialData!!.timeType!!)

                initialData!!.location?.run {
                    edtLocationCarrierState.setText(initialData!!.location)
                    updateData(SubmissionData.FIELD_LOCATION, initialData!!.location!!)
                }

                edtReferenceObjectCarrierState.setText(initialData!!.referenceObject)
                updateData(SubmissionData.FIELD_REFERENCE_OBJECT, initialData!!.referenceObject!!)

                edtCarrierCarrierState.setText(initialData!!.carrier)
                updateData(SubmissionData.FIELD_CARRIER, initialData!!.carrier!!)
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

                binding.edtLocationCarrierState.setText(location)
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

            binding.edtLocationCarrierState.setText(locationPrefix)
            updateData(SubmissionData.FIELD_LOCATION, locationPrefix)

            listPopupWindow.dismiss()
        }

        listPopupWindow.show()
    }

    companion object {
        private const val TAG = "CarrierStateFragment"
    }
}