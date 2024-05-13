package org.deplide.application.android.trafficcdmforoperator.repository

import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

interface SubmittedMessageDBInterface {
    fun addMessage(submissionData: SubmissionData)

    fun getSubmittedMessages(): List<SubmissionData>

    fun clearSubmittedMessages()

}