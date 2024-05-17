package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

sealed class SubmissionOverviewUIState {
    data object Loading : SubmissionOverviewUIState()
    data class Success(val submissions: List<SubmissionData>) : SubmissionOverviewUIState()
    data class Error(val message: String) : SubmissionOverviewUIState()
}