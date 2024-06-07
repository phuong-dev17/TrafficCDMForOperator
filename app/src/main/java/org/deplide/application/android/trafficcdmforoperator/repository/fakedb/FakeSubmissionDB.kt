package org.deplide.application.android.trafficcdmforoperator.repository.fakedb

import org.deplide.application.android.trafficcdmforoperator.repository.SubmissionDBInterface
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class FakeSubmissionDB : SubmissionDBInterface {
    private val db = mutableMapOf<String, SubmissionData>()

    override suspend fun addSubmission(submissionData: SubmissionData) {
        db[submissionData.messageId] = submissionData
    }

    override suspend fun getSubmissions(): List<SubmissionData> {
        return db.values.toList()
    }

    override suspend fun deleteSubmissions() {
        db.clear()
    }

    override suspend fun getSubmissionByMessageId(id: String): SubmissionData? {
        return db[id]
    }

    override suspend fun deleteSubmissionByMessageId(id: String) {
        db.remove(id)
    }

    companion object {
        private var _instance: FakeSubmissionDB? = null
        val instance: FakeSubmissionDB
            get() {
                if (_instance == null) {
                    _instance = FakeSubmissionDB()
                }
                return _instance!!
            }
    }
}