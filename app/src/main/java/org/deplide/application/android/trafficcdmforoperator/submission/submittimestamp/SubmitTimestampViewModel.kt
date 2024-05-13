package org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Base64
import org.deplide.application.android.trafficcdmforoperator.network.IdToken
import org.deplide.application.android.trafficcdmforoperator.network.TrafficCDMApi
import org.deplide.application.android.trafficcdmforoperator.network.dto.tcmf.version_0_0_7.TCMFMessage
import org.deplide.application.android.trafficcdmforoperator.repository.SubmittedMessageDBFactory
import org.deplide.application.android.trafficcdmforoperator.repository.SubmittedMessageDBInterface
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

class SubmitTimestampViewModel: ViewModel() {
    private var _uiState = MutableStateFlow<SubmitTmestampUIState>(SubmitTmestampUIState.Idle)
    val uiState: StateFlow<SubmitTmestampUIState> = _uiState

    private val submittedMessageDB: SubmittedMessageDBInterface by lazy {
        SubmittedMessageDBFactory.getSubmittedMessageDB(SubmittedMessageDBFactory.FAKE_DB)!!
    }

    fun submitTCMFMessage(
        submissionData: SubmissionData,
        accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = SubmitTmestampUIState.Sending
            val userName = getUserNameFromIdToken(accessToken)
            Log.d(TAG, "submitTCMFMessage for user $userName")
            if (submissionData.isPayloadValid()) {
                fillTCMFMessageMetaData(submissionData, userName)
                fillTCMFMessageGrouping(submissionData)

                if (submissionData.isMessageValid()) {
                    Log.d(TAG, "Submitting TCMF Message")
                    try {
                        TrafficCDMApi.retrofit.submitMessage(
                            token = "Bearer $accessToken",
                            accept = "application/json",
                            message = TCMFMessage(submissionData)
                        )

                        submittedMessageDB.addMessage(submissionData)

                        submittedMessageDB.getSubmittedMessages().forEach {
                            Log.d(TAG, it.toString())
                        }
                        _uiState.value = SubmitTmestampUIState.Success
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to submit TCMF Message", ex)
                        _uiState.value = SubmitTmestampUIState.Error
                    }
                }
            }

            Log.d(TAG, submissionData.toString())
        }
    }

    private fun fillTCMFMessageGrouping(submissionData: SubmissionData) {
        /*(submissionData?.location
            ?.substring(startIndex = SubmissionData.LOCATION_PREFIX.length))?.run {
                submissionData?.grouping?.add("tcmf:grouping:$this")
            }*/

        (submissionData.referenceObject
            ?.substring(startIndex = SubmissionData.REFERENCE_OBJECT_PREFIX.length))?.run {
                submissionData.grouping.add("tcmf:grouping:$this")
            }

        (submissionData.carrier
            ?.substring(startIndex = SubmissionData.CARRIER_PREFIX.length))?.run {
                submissionData.grouping.add("tcmf:grouping:$this")
            }

        Log.d(TAG, submissionData.grouping.toString())
    }

    private fun fillTCMFMessageMetaData(
        submissionData: SubmissionData,
        userName: String) {
        submissionData.messageId = "tcmf:message:${UUID.randomUUID()}"
        submissionData.reportedBy = "tcmf:user:RISE:$userName"
        submissionData.reportedAt = getCurrentDateTime()
        submissionData.source = "test"
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

    companion object {
        const val TAG = "SubmitTimestampViewModel"
        fun factory(): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SubmitTimestampViewModel()
                }
            }
        }
    }
}