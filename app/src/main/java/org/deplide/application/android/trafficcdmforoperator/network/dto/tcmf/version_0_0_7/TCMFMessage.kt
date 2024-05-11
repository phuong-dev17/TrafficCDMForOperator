package org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.deplide.application.android.trafficcdmforoperator.submission.SubmissionData

@JsonClass(generateAdapter = true)
data class TCMFMessage(
    // meta data
    @Json(name = "version") val version: String = VERSION,
    @Json(name = "messageId") val messageId: String,
    @Json(name = "reportedAt") val reportedAt: String,
    @Json(name = "reportedBy") val reportedBy: String,
    @Json(name = "source") val source: String? = null,

    // grouping
    @Json(name = "grouping") val grouping: List<String>,

    // payload
    @Json(name = "payload") val payload: TCMFMessagePayload
) {

    constructor(submissionData: SubmissionData) : this(
        messageId = submissionData.messageId,
        reportedAt = submissionData.reportedAt,
        reportedBy = submissionData.reportedBy,
        source = submissionData.source,
        grouping = submissionData.grouping,
        payload = TCMFMessagePayload(
            type = submissionData.type,
            time = submissionData.time,
            timeType = submissionData.timeType,
            timeSequence = submissionData.timeSequence,
            location = submissionData.location,
            referenceObject = submissionData.referenceObject,
            service = submissionData.service,
            carrier = submissionData.carrier,
            operation = submissionData.operation,
            attribute = submissionData.attribute
        )
    )
    companion object {
        private const val VERSION = "0.0.7"
    }
}
