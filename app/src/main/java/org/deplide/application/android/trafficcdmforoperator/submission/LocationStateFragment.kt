package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentLocationStateBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper.Companion.convertUTCTimeToSystemDefault
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper.Companion.getCurrentDateTime
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimePicker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.lang.reflect.Type


class LocationStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentLocationStateBinding
    private var initialData: SubmissionData? = null
    private var editMode: String? = null
    private lateinit var dateTimePicker: DateTimePicker
    private lateinit var locations: Map<String, String>

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

        loadLocations()
        setCurrentTimeForTimeField()
        setDefaultForFields()
        configureListeners()
        loadInitialData()
        configureAccordingToEditMode()
    }

    private fun setDefaultForFields() {
        binding.apply {
            radioGroupTimeTypeLocationState.check(
                R.id.radioBtnLocationStateActual)
            updateData(SubmissionData.FIELD_TIME_TYPE,
                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })

            val defaultLocationType = resources.getStringArray(R.array.predefined_location_prefix_for_location_state)[0]
            edtLocationTypeLocationState.setText(
                defaultLocationType,
                false
            )
            updateData(SubmissionData.FIELD_LOCATION,
                defaultLocationType)
        }
    }

    private fun loadLocations() {
        val locationJsonString = readLocationJsonFile()

        val loadedLocations: Map<String, String> = parseLocationJsonString(locationJsonString)

        locations = makeLocationFullNameAsKeyInTheMap(loadedLocations)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.cell_location,
            locations.keys.toList())
        binding.edtLocationLocationState.setAdapter(adapter)
    }

    private fun makeLocationFullNameAsKeyInTheMap(loadedLocations: Map<String, String>) =
        loadedLocations.map { (k, v) -> v to k }.toMap()

    private fun parseLocationJsonString(locationJsonString: String): Map<String, String> {
        val moshi: Moshi = Moshi.Builder().build()

        val type: Type = Types.newParameterizedType(
            MutableMap::class.java,
            String::class.java,
            String::class.java
        )
        val jsonAdapter: JsonAdapter<Map<String, String>> = moshi.adapter(type)

        val loadedLocations: Map<String, String> = jsonAdapter.fromJson(locationJsonString)!!
        return loadedLocations
    }

    /*
     * This complete function is copied from accepted answer in Stackoverflow
     * https://stackoverflow.com/questions/6349759/using-json-file-in-android-app-resources
     */
    private fun readLocationJsonFile(): String {
        val inputStream = resources.openRawResource(org.deplide.application.android.trafficcdmforoperator.R.raw.places)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader: Reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var n: Int
            while ((reader.read(buffer).also { n = it }) != -1) {
                writer.write(buffer, 0, n)
            }
        } finally {
            inputStream.close()
        }

        return writer.toString()
    }

    private fun configureAccordingToEditMode() {
        val isEnabled = isTheFieldEnabled()

        binding.apply {
            edtTimeLocationState.isEnabled = isEnabled
            txtInputLayoutTimeLocationState.isEndIconVisible = isEnabled
            radioGroupTimeTypeLocationState.isEnabled = isEnabled
            radioBtnLocationStatePlanned.isEnabled = isEnabled
            radioBtnLocationStateEstimated.isEnabled = isEnabled
            radioBtnLocationStateActual.isEnabled = isEnabled
            edtLocationTypeLocationState.isEnabled = isEnabled
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

            radioGroupTimeTypeLocationState
                .setOnCheckedChangeListener{ _, checkedId ->
                    when (checkedId) {
                        R.id.radioBtnLocationStatePlanned -> {
                            updateData(SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_planned).replaceFirstChar { it.lowercase() })
                        }
                        R.id.radioBtnLocationStateEstimated -> {
                            updateData(SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_estimated).replaceFirstChar { it.lowercase() })
                        }
                        R.id.radioBtnLocationStateActual -> {
                            updateData(SubmissionData.FIELD_TIME_TYPE,
                                getString(R.string.time_type_actual).replaceFirstChar { it.lowercase() })
                        }
                    }
            }

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

            edtLocationTypeLocationState.addTextChangedListener(
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

                when (initialData!!.timeType) {
                    getString(R.string.time_type_planned)
                        .replaceFirstChar { it.lowercase() } -> {
                            Log.d(TAG, "loadInitialData: planned")
                        radioGroupTimeTypeLocationState.check(R.id.radioBtnLocationStatePlanned)
                    }
                    getString(R.string.time_type_estimated)
                        .replaceFirstChar { it.lowercase() } -> {
                            Log.d(TAG, "loadInitialData: estimated")
                        radioGroupTimeTypeLocationState.check(R.id.radioBtnLocationStateEstimated)
                    }
                    getString(R.string.time_type_actual)
                        .replaceFirstChar { it.lowercase() } -> {
                            Log.d(TAG, "loadInitialData: actual")
                        radioGroupTimeTypeLocationState.check(R.id.radioBtnLocationStateActual)
                    }
                    else -> {
                        Log.d(TAG, "loadInitialData: ${initialData!!.timeType}")
                        radioGroupTimeTypeLocationState.check(R.id.radioBtnLocationStateActual)
                    }
                }
                updateData(SubmissionData.FIELD_TIME_TYPE, initialData!!.timeType!!)

                edtLocationTypeLocationState.setText(initialData!!.location)
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