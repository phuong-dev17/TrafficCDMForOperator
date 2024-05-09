package org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TCMFMessagePayload(
    // common
    @Json(name = "@type") var type: String = "",
    // context based
    @Json(name = "time") var time: String? = null,
    @Json(name = "timeSequence") var timeSequence: String? = null,
    @Json(name = "referenceObject") var referenceObject: String? = null,
    @Json(name = "timeType") var timeType: String? = null,
    @Json(name = "location") var location: String? = null,
    @Json(name = "service") var service: String? = null,
    @Json(name = "carrier") var carrier: String? = null,
    @Json(name = "attribute") var attribute: String? = null,
    @Json(name = "operation") var operation: String? = null
)