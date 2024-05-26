package org.deplide.application.android.trafficcdmforoperator.submission.util

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.submission.LocationStateFragment
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class DateTimePicker(
    private val outputDatTimeFormat: String,
    private val isSystem24Hour: Boolean,
    private val fragmentManager: FragmentManager,
    val onDateTimeSelected: (String) -> Unit) {
    private var datePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select date")
        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        .build()
    private lateinit var timePicker: MaterialTimePicker


    init {
        datePicker.addOnPositiveButtonClickListener {
            Log.d(TAG, "selected date: $it. ${datePicker.selection}")
            var timeInMilliseconds = it
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

                val sdf = DateTimeFormatter.ofPattern(outputDatTimeFormat)
                val dateTime = sdf.format(zonedDateTime)

                Log.d(TAG, "dateTime: $dateTime")

                onDateTimeSelected(dateTime)
            }

            timePicker.show(fragmentManager, TAG)
        }
    }

    fun show() {
        datePicker.show(fragmentManager, TAG)
    }

    companion object {
        private const val TAG = "DateTimePicker"
    }
}