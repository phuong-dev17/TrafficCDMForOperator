package org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7

import org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7.TCMFMessage

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
    var operation: String? = null
) {
    fun isPayloadValid(): Boolean {
        return when(type) {
            "LocationState" -> isLocationStatePayloadValid()
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

    fun getDescription(): String {
        return when(type) {
            "LocationState" -> getDescriptionForLocationState()
            else -> "" //unknown type
        }
    }

    private fun getDescriptionForLocationState(): String {
        val objectType = referenceObject?.split(":")!![2]
        val objectId = referenceObject?.split("${objectType}:")!![1]
        val locationType = location?.split(":")!![2]
        val locationId = location?.split("${locationType}:")!![1]
        return "$objectType $objectId" +
                " is $timeType $timeSequence" +
                " $locationType $locationId" +
                " at $time"
    }

    companion object {
        const val TYPE_MESSAGE_OPERATION = "MessageOperation"
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