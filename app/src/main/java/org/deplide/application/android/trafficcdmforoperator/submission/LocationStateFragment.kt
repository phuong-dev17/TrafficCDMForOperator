package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentLocationStateBinding
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class LocationStateFragment : Fragment(), StateFragmentDataUpdater {
    private lateinit var binding: FragmentLocationStateBinding
    private val data: MutableMap<String, String> = mutableMapOf(
        SubmissionData.FIELD_TIME to "",
        SubmissionData.FIELD_LOCATION to "",
        SubmissionData.FIELD_REFERENCE_OBJECT to "",
        SubmissionData.FIELD_TIME_TYPE to "",
    )
    private var dataUpdateListener: StateFragmentDataUpdateListener? = null
    private lateinit var datePicker:  MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLocationStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            edtTimeLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME, text!!.toString())
                }
            )

            txtInputLayoutTimeLocationState.setEndIconOnClickListener {
                datePicker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build()

                datePicker.addOnPositiveButtonClickListener {
                    Log.d(TAG, "selected date: $it. ${datePicker.selection}")
                    var timeInMilliseconds = it

                    val isSystem24Hour = is24HourFormat(requireContext())
                    val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

                    timePicker =
                        MaterialTimePicker.Builder()
                            .setTimeFormat(clockFormat)
                            .setTitleText("Select time")
                            .build()

                    timePicker.addOnPositiveButtonClickListener {
                        Log.d(TAG, "selected time: ${timePicker.hour}:${timePicker.minute}")

                        // Convert hours to milliseconds
                        val hoursInMilliseconds = timePicker.hour * 60 * 60 * 1000

                        // Convert minutes to milliseconds
                        val minutesInMilliseconds = timePicker.minute * 60 * 1000
                        timeInMilliseconds += hoursInMilliseconds + minutesInMilliseconds

                        val instant = Instant.ofEpochMilli(timeInMilliseconds)
                        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))

                        val sdf = DateTimeFormatter.ofPattern(getString(R.string.date_time_pattern))
                        val dateTime = sdf.format(zonedDateTime)

                        Log.d(TAG, "dateTime: $dateTime")

                        edtTimeLocationState.setText(dateTime)
                        updateData(SubmissionData.FIELD_TIME, dateTime)
                    }

                    timePicker.show(childFragmentManager, TAG)
                }
                datePicker.show(childFragmentManager, TAG)
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

            edtTimeTypeLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME_TYPE, text!!.toString())
                }
            )
        }
    }

    private fun updateData(key: String, value: String) {
        data[key] = value.toString()
        informListeners()
    }

    private fun informListeners() {
        dataUpdateListener?.onStateFragmentDataUpdate(data)
    }

    override fun addStateFragmentDataUpdateListener(listener: StateFragmentDataUpdateListener) {
        dataUpdateListener = listener
    }

    companion object {
        private const val TAG = "LocationStateFragment"
    }
}