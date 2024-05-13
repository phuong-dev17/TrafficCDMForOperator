package org.deplide.application.android.trafficcdmforoperator.repository

import org.deplide.application.android.trafficcdmforoperator.repository.fakedb.FakeSubmittedMessageDB

class SubmittedMessageDBFactory {
    companion object {
        const val FAKE_DB = "Fake DB"
        const val REAL_DB = "Real DB"

        fun getSubmittedMessageDB(type: String): SubmittedMessageDBInterface? {
            return when (type) {
                FAKE_DB -> {
                    FakeSubmittedMessageDB.instance
                }
                else -> null
            }
        }
    }
}