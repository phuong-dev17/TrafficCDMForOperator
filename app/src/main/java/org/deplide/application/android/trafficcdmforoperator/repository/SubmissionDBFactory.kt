package org.deplide.application.android.trafficcdmforoperator.repository

import android.content.Context
import org.deplide.application.android.trafficcdmforoperator.repository.db.RealSubmissionDB
import org.deplide.application.android.trafficcdmforoperator.repository.fakedb.FakeSubmissionDB

class SubmissionDBFactory {
    companion object {
        const val FAKE_DB = "Fake DB"
        const val REAL_DB = "Real DB"

        fun getSubmissionDB(type: String, applicationContext: Context? = null): SubmissionDBInterface? {
            return when (type) {
                FAKE_DB -> {
                    FakeSubmissionDB.instance
                }
                REAL_DB -> {
                    if (applicationContext != null) {
                        RealSubmissionDB.instance(applicationContext)
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
    }
}