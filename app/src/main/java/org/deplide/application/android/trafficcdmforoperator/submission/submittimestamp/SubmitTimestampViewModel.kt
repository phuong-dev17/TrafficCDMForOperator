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
import org.deplide.application.android.trafficcdmforoperator.submission.util.DateTimeHelper.Companion.getCurrentDateTime
import java.util.UUID

class SubmitTimestampViewModel: ViewModel() {
    private var _uiState = MutableStateFlow<SubmitTmestampUIState>(SubmitTmestampUIState.Idle(null))
    val uiState: StateFlow<SubmitTmestampUIState> = _uiState

    private val submittedMessageDB: SubmittedMessageDBInterface by lazy {
        SubmittedMessageDBFactory.getSubmittedMessageDB(SubmittedMessageDBFactory.FAKE_DB)!!
    }

    fun submitTCMFMessage(
        submissionData: SubmissionData,
        accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = SubmitTmestampUIState.Processing
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

                        _uiState.value = SubmitTmestampUIState.Success
                    } catch (ex: Exception) {
                        _uiState.value = SubmitTmestampUIState.Error("Failed to submit TCMF Message ${ex.message}")
                    }
                } else {
                    _uiState.value = SubmitTmestampUIState.Error("Invalid TCMF Message")
                }
            } else {
                _uiState.value = SubmitTmestampUIState.Error("Invalid Payload")
            }

            Log.d(TAG, submissionData.toString())
        }
    }

    private fun fillTCMFMessageGrouping(submissionData: SubmissionData) {
        /*(submissionData?.location
            ?.substring(startIndex = SubmissionData.LOCATION_PREFIX.length))?.run {
                submissionData?.grouping?.add("tcmf:grouping:$this")
            }*/

        if (!submissionData.referenceObject.isNullOrEmpty()) {
            (submissionData.referenceObject
                ?.substring(startIndex = SubmissionData.REFERENCE_OBJECT_PREFIX.length))?.run {
                    submissionData.grouping.add("tcmf:grouping:$this")
                }
        }

        if (!submissionData.carrier.isNullOrEmpty()) {
            (submissionData.carrier
                ?.substring(startIndex = SubmissionData.CARRIER_PREFIX.length))?.run {
                    submissionData.grouping.add("tcmf:grouping:$this")
                }
        }

        Log.d(TAG, submissionData.grouping.toString())
    }

    private fun fillTCMFMessageMetaData(
        submissionData: SubmissionData,
        userName: String) {
        submissionData.messageId = "tcmf:message:${UUID.randomUUID()}"
        submissionData.reportedBy = "tcmf:user:RISE:$userName"
        submissionData.reportedAt = getCurrentDateTime(convertToUTC = true)
        submissionData.source = "test"
    }

    private fun getUserNameFromIdToken(idToken: String): String {
        val idTokenPayload =
            String(Base64(true).decode(idToken.split(".")[1]), Charsets.UTF_8)
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<IdToken> = moshi.adapter(IdToken::class.java)
        return adapter.fromJson(idTokenPayload)?.userName!!
    }

    fun loadMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = SubmitTmestampUIState.Processing

            submittedMessageDB.getMessage(messageId)?.let {
                _uiState.value = SubmitTmestampUIState.Idle(it)
            }?: run {
                _uiState.value = SubmitTmestampUIState.Idle(null)
            }
        }
    }

    fun undoMessage(messageId: String, accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = SubmitTmestampUIState.Processing

            submittedMessageDB.getMessage(messageId)?.let {
                val messageOperation = SubmissionData(
                    type = "MessageOperation",
                    operation = "invalidate",
                    grouping = it.grouping,
                    undoMessageId = it.messageId,
                )

                if (messageOperation.isPayloadValid()) {
                    val userName = getUserNameFromIdToken(accessToken)
                    fillTCMFMessageMetaData(messageOperation, userName)
                    fillTCMFMessageGrouping(messageOperation)

                    if (messageOperation.isMessageValid()) {
                        try {
                            TrafficCDMApi.retrofit.submitMessage(
                                token = "Bearer $accessToken",
                                accept = "application/json",
                                message = TCMFMessage(messageOperation)
                            )

                            submittedMessageDB.deleteMessage(messageId)

                            _uiState.value = SubmitTmestampUIState.SuccessUndo
                        } catch (ex: Exception) {
                            _uiState.value = SubmitTmestampUIState.Error("Failed to undo message $messageId")
                        }
                    }
                }
            }?: run {
                _uiState.value = SubmitTmestampUIState.Error("Failed to undo message $messageId")
            }
        }
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