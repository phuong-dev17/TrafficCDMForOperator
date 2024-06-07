package org.deplide.application.android.trafficcdmforoperator.repository

import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

interface SubmissionDBInterface {
    suspend fun addSubmission(submissionData: SubmissionData)

    suspend fun getSubmissions(): List<SubmissionData>

    suspend fun deleteSubmissions()

    suspend fun getSubmissionByMessageId(id: String): SubmissionData?

    suspend fun deleteSubmissionByMessageId(id: String)
}