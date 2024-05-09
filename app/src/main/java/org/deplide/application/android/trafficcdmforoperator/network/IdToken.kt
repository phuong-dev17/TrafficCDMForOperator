package org.deplide.application.android.trafficcdmforoperator.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class IdToken(
    @Json(name = "preferred_username") val userName: String = "")