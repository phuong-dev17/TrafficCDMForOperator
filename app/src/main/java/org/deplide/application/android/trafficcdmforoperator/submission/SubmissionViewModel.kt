package org.deplide.application.android.trafficcdmforoperator.submission

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Base64
import org.deplide.application.android.trafficcdmforoperator.network.IdToken
import org.deplide.application.android.trafficcdmforoperator.network.TrafficCDMApi
import org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7.TCMFMessage
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

class SubmissionViewModel: ViewModel(), StateFragmentDataUpdateListener {
    private var submissionData: SubmissionData? = null


    fun newTCMFMessage(type: String, timeSequence: String) {
        submissionData = SubmissionData(
            type = type,
            timeSequence = timeSequence)
        if (submissionData?.type != SubmissionData.TYPE_MESSAGE_OPERATION) {
            submissionData?.time = getCurrentDateTime()
        } else {
            submissionData?.operation = "invalidate"
        }
    }

    fun submitTCMFMessage(accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userName = getUserNameFromIdToken(accessToken)
            Log.d(TAG, "submitTCMFMessage for user $userName")
            if (submissionData?.isPayloadValid()!!) {
                fillTCMFMessageMetaData(userName)
                fillTCMFMessageGrouping()

                if (submissionData?.isMessageValid()!!) {
                    Log.d(TAG, "Submitting TCMF Message")
                    try {
                        TrafficCDMApi.retrofit.submitMessage(
                            token = "Bearer $accessToken",
                            accept = "application/json",
                            message = TCMFMessage(submissionData!!)
                        )
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to submit TCMF Message", ex)
                    }
                }
            }

            Log.d(TAG, submissionData.toString())
        }
    }

    private fun fillTCMFMessageGrouping() {
        /*(submissionData?.location
            ?.substring(startIndex = SubmissionData.LOCATION_PREFIX.length))?.run {
                submissionData?.grouping?.add("tcmf:grouping:$this")
            }*/

        (submissionData?.referenceObject
            ?.substring(startIndex = SubmissionData.REFERENCE_OBJECT_PREFIX.length))?.run {
                submissionData?.grouping?.add("tcmf:grouping:$this")
            }

        (submissionData?.carrier
            ?.substring(startIndex = SubmissionData.CARRIER_PREFIX.length))?.run {
                submissionData?.grouping?.add("tcmf:grouping:$this")
            }

        Log.d(TAG, submissionData?.grouping.toString())
    }

    private fun fillTCMFMessageMetaData(userName: String) {
        submissionData?.messageId = "tcmf:message:${UUID.randomUUID()}"
        submissionData?.reportedBy = "tcmf:user:RISE:$userName"
        submissionData?.reportedAt = getCurrentDateTime()
    }

    private fun getCurrentDateTime(): String {
        val now = Instant.now()
        val formatter = DateTimeFormatter.ISO_INSTANT
        return formatter.format(now)
    }

    private fun getUserNameFromIdToken(idToken: String): String {
        val idTokenPayload =
            String(Base64(true).decode(idToken.split(".")[1]), Charsets.UTF_8)
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<IdToken> = moshi.adapter(IdToken::class.java)
        return adapter.fromJson(idTokenPayload)?.userName!!
    }

    override fun onStateFragmentDataUpdate(data: Map<String, String>) {
        data.forEach{entry ->
            when(entry.key) {
                SubmissionData.FIELD_LOCATION -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.location = entry.value
                }
                SubmissionData.FIELD_TIME_TYPE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.timeType = entry.value
                }
                SubmissionData.FIELD_TIME_SEQUENCE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.timeSequence = entry.value
                }
                SubmissionData.FIELD_REFERENCE_OBJECT -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.referenceObject = entry.value
                }
                SubmissionData.FIELD_SERVICE -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.service = entry.value
                }
                SubmissionData.FIELD_CARRIER -> {
                    Log.d(TAG, "${entry.key}: ${entry.value}")
                    submissionData?.carrier = entry.value
                }
            }
        }
    }
    companion object {
        const val TAG = "SubmissionViewModel"
        fun factory(): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SubmissionViewModel()
                }
            }
        }
    }
}