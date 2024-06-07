package org.deplide.application.android.trafficcdmforoperator.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.deplide.application.android.trafficcdmforoperator.repository.SubmissionDBInterface
import org.deplide.application.android.trafficcdmforoperator.repository.fakedb.FakeSubmissionDB
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

@Database(entities = [SubmissionEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class SubmissionRoomDB : RoomDatabase() {
    abstract fun submissionDao() : SubmissionDao
}

class RealSubmissionDB private constructor(applicationContext: Context): SubmissionDBInterface {
    private val instance: SubmissionRoomDB by lazy {
        Room.databaseBuilder(
            applicationContext,
            SubmissionRoomDB::class.java,
            "Submission History Database"
        ).build()
    }
    private val submissionDao: SubmissionDao
        get() = instance.submissionDao()

    override suspend fun addSubmission(submissionData: SubmissionData) {
        submissionDao.addSubmission(SubmissionEntity(submissionData))
    }

    override suspend fun getSubmissions(): List<SubmissionData> {
        return submissionDao.getAllSubmissions().map {
            SubmissionData(it)
        }
    }

    override suspend fun deleteSubmissions() {
        submissionDao.deleteAllSubmissions()
    }

    override suspend fun getSubmissionByMessageId(id: String): SubmissionData {
        return SubmissionData(submissionDao.getSubmissionById(id))
    }

    override suspend fun deleteSubmissionByMessageId(id: String) {
        submissionDao.deleteSubmissionById(id)
    }

    companion object {
        private var _instance: RealSubmissionDB? = null
        fun instance(applicationContext: Context): RealSubmissionDB {
            if (_instance == null) {
                _instance = RealSubmissionDB(applicationContext)
            }
            return _instance!!
        }
    }
}