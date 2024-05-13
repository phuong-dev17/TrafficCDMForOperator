package org.deplide.application.android.trafficcdmforoperator.repository.fakedb

import org.deplide.application.android.trafficcdmforoperator.repository.SubmittedMessageDBInterface
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class FakeSubmittedMessageDB : SubmittedMessageDBInterface {
    private val db = mutableListOf<SubmissionData>()

    override fun addMessage(submissionData: SubmissionData) {
        db.add(submissionData)
    }

    override fun getSubmittedMessages(): List<SubmissionData> {
        return db
    }

    override fun clearSubmittedMessages() {
        db.clear()
    }

    companion object {
        private var _instance: FakeSubmittedMessageDB? = null
        val instance: FakeSubmittedMessageDB
            get() {
                if (_instance == null) {
                    _instance = FakeSubmittedMessageDB()
                }
                return _instance!!
            }
    }
}