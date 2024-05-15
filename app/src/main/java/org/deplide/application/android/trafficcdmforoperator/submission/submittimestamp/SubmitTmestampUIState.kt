package org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp

sealed class SubmitTmestampUIState {
    data object Idle : SubmitTmestampUIState()
    data object Sending : SubmitTmestampUIState()

    data class Error(val message: String) : SubmitTmestampUIState()
    data object Success : SubmitTmestampUIState()
}