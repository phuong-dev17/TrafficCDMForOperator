package org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7.TCMFMessage
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper.Companion.convertUTCTimeToSystemDefault

@Parcelize
data class SubmissionData(
    // meta data
    val version: String = TCMFMessage.VERSION,
    var messageId: String = "",
    var reportedAt: String = "",
    var reportedBy: String = "",
    var source: String? = null,

    // grouping
    var grouping: MutableList<String> = mutableListOf(),

    // payload
    // common
    var type: String = "",
    // context based
    var time: String? = null,
    var timeSequence: String? = null,
    var referenceObject: String? = null,
    var timeType: String? = null,
    var location: String? = null,
    var service: String? = null,
    var carrier: String? = null,
    var attribute: String? = null,
    var operation: String? = null,
    var undoMessageId: String? = null
) : Parcelable {
    fun isPayloadValid(): Boolean {
        return when(type) {
            "LocationState" -> isLocationStatePayloadValid()
            "MessageOperation" -> isMessageOperationPayloadValid()
            "AdministrativeState" -> isAdministrativeStatePayloadValid()
            else -> false //unknown type
        }
    }

    fun isMessageValid(): Boolean {
        return isPayloadValid() and isGroupingValid() and isMetaDataValid()
    }

    private fun isGroupingValid(): Boolean {
        return grouping.isNotEmpty()
    }

    private fun isMetaDataValid(): Boolean {
        var isValid = true

        if (messageId.isEmpty() || reportedAt.isEmpty() || reportedBy.isEmpty()) {
            isValid = false
        }

        return isValid
    }

    private fun isLocationStatePayloadValid(): Boolean {
        var isValid = true

        //mandatory fields
        if (time == null || timeType == null || timeSequence == null
            || referenceObject == null || location == null) {
            isValid = false
        }
        //prohibited fields
        if (service != null || carrier != null
            || attribute != null || operation != null) {
            isValid = false
        }

        return isValid
    }

    private fun isAdministrativeStatePayloadValid(): Boolean {
        var isValid = true

        //mandatory fields
        if (time == null || service == null ||
                timeSequence == null || referenceObject == null) {
            isValid = false
        }

        //optional fields: location

        //prohibited fields
        if (timeType != null || carrier != null
            || attribute != null || operation != null) {
            isValid = false
        }

        return isValid
    }

    private fun isMessageOperationPayloadValid(): Boolean {
        var isValid = true

        //mandatory fields
        if (operation == null || undoMessageId == null) {
            isValid = false
        }

        return isValid
    }

    fun getDescription(dateTimeFormat: String): String {
        return when(type) {
            "LocationState" -> getDescriptionForLocationState(dateTimeFormat)
            "AdministrativeState" -> getDescriptionForAdministrativeState(dateTimeFormat)
            else -> "" //unknown type
        }
    }

    private fun getDescriptionForLocationState(dateTimeFormat: String): String {
        val timeTypeString = if (timeType != null) {
            if (timeType == "actual") {
                "has"
            } else {
                "$timeType to"
            }
        } else {
            ""
        }
        val locationType = location?.split(":")!![2]
        val locationId = location?.split("${locationType}:")!![1]
        val timeSequenceString = timeSequence?.replace("_", " ")
        val localTime = convertUTCTimeToSystemDefault(time!!, dateTimeFormat)
        return "$timeTypeString $timeSequenceString" +
                " $locationType $locationId" +
                " at $localTime"
    }

    private fun getDescriptionForAdministrativeState(dateTimeFormat: String): String {
        val timeSequenceString = timeSequence?.replace("_", " ")
        val localTime = convertUTCTimeToSystemDefault(time!!, dateTimeFormat)
        val locationString = if (location != null) {
            val locationType = location?.split(":")!![2]
            val locationId = location?.split("${locationType}:")!![1]

            "at $locationType $locationId"
        } else {
            ""
        }

        val objectString = if (referenceObject != null) {
            val objectType = referenceObject?.split(":")!![2]
            val objectId = referenceObject?.split("${objectType}:")!![1]

            "for $objectType $objectId"
        } else {
            ""
        }

        return "$timeSequenceString at $localTime $objectString $locationString"
    }

    fun getObjectInConcern(): String {
        return when(type) {
            "LocationState" -> getObjectInConcernForLocationState()
            "AdministrativeState" -> getObjectInConcernForAdministrativeState()
            else -> "" //unknown type
        }
    }

    private fun getObjectInConcernForLocationState(): String {
        val objectType = referenceObject?.split(":")!![2]
        val objectTypeUppercase = objectType.replaceFirstChar { it.uppercase() }
        val objectId = referenceObject?.split("${objectType}:")!![1]

        return "$objectTypeUppercase $objectId"
    }

    private fun getObjectInConcernForAdministrativeState(): String {
        val service = service?.split(":")!![2]

        return "Service $service"
    }

    companion object {
        const val CARRIER_PREFIX = "tcmf:carrier:"
        const val REFERENCE_OBJECT_PREFIX = "tcmf:reference_object:"
        const val FIELD_TIME = "Time"
        const val FIELD_LOCATION = "Location"
        const val FIELD_REFERENCE_OBJECT = "Reference Object"
        const val FIELD_TIME_TYPE = "Time Type"
        const val FIELD_TIME_SEQUENCE = "Time Sequence"
        const val FIELD_SERVICE = "Service"
        const val FIELD_CARRIER = "Carrier"
    }
}