package org.deplide.application.android.trafficcdmforoperator.toolbox

data class TCMFMessage(
    //meta data
    var messageId: String,
    var reportedAt: String,
    var reportedBy: String,
    var source: String?,

    //grouping
    var grouping: MutableList<String>,

    //payload
    //common
    var type: String,
    //context based
    var time: String?,
    var timeSequence: String?,
    var referenceObject: String?,
    var timeType: String?,
    var location: String?,
    var service: String?,
    var carrier: String?,
    var attribute: String?,
    var operation: String?
) {
    fun isValid(): Boolean {
        return when(type) {
            "LocationState" -> isLocationStatePayloadValid()
            else -> false //unknown type
        }
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

    companion object {
        private const val VERSION = "0.0.7"
    }
}
