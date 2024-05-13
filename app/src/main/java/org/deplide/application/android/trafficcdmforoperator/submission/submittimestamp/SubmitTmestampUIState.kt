package org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp

sealed class SubmitTmestampUIState {
    data object Idle : SubmitTmestampUIState()
    data object Sending : SubmitTmestampUIState()

    data object Success : SubmitTmestampUIState()
    data object Error : SubmitTmestampUIState()

}