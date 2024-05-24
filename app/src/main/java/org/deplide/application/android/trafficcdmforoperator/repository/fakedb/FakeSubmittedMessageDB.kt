package org.deplide.application.android.trafficcdmforoperator.repository.fakedb

import org.deplide.application.android.trafficcdmforoperator.repository.SubmittedMessageDBInterface
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class FakeSubmittedMessageDB : SubmittedMessageDBInterface {
    private val db = mutableMapOf<String, SubmissionData>()

    override fun addMessage(submissionData: SubmissionData) {
        db[submissionData.messageId] = submissionData
    }

    override fun getSubmittedMessages(): List<SubmissionData> {
        return db.values.toList()
    }

    override fun deleteSubmittedMessages() {
        db.clear()
    }

    override fun getMessage(id: String): SubmissionData? {
        return db[id]
    }

    override fun deleteMessage(id: String) {
        db.remove(id)
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