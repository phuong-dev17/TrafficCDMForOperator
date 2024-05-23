package org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp

import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

sealed class SubmitTmestampUIState {
    data class Idle(val initialData: SubmissionData? = null) : SubmitTmestampUIState()
    data object Processing : SubmitTmestampUIState()

    data class Error(val message: String) : SubmitTmestampUIState()
    data object Success : SubmitTmestampUIState()
}