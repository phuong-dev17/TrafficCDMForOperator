package org.deplide.application.android.trafficcdmforoperator.submission.util

import android.util.Log
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

class DateTimeHelper() {
    companion object {
        private const val TAG = "DateTimeHelper"

        fun getCurrentDateTime(format: String? = null, convertToUTC: Boolean = false): String {
            val now = Instant.now()

            val dateTime = if (convertToUTC) {
                now.atZone(ZoneId.of("UTC"))
            } else {
                now.atZone(ZoneId.systemDefault())
            }
            Log.d(TAG, "now: $now")
            Log.d(TAG, "format: $format")
            val formatter = if (format != null) {
                DateTimeFormatter.ofPattern(format)
            } else {
                DateTimeFormatter.ISO_INSTANT
            }

            return dateTime.format(formatter)
        }

        fun convertUTCTimeToSystemDefault(utcTime: String, format: String? = null): String {
            val inputFormatter = DateTimeFormatter.ISO_INSTANT
            val outputFormatter = if (format != null) {
                DateTimeFormatter.ofPattern(format)
            } else {
                DateTimeFormatter.ISO_INSTANT
            }

            val instant = Instant.from(inputFormatter.parse(utcTime))
            val dateTime = instant.atZone(ZoneId.systemDefault())
            return dateTime.format(outputFormatter)
        }
    }
}
