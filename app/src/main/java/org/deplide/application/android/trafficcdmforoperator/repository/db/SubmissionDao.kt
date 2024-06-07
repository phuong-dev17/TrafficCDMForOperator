package org.deplide.application.android.trafficcdmforoperator.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SubmissionDao {
    @Insert
    suspend fun addSubmission(submission: SubmissionEntity)

    @Query("SELECT * FROM submissions")
    suspend fun getAllSubmissions(): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE message_id = :messageId")
    suspend fun getSubmissionById(messageId: String): SubmissionEntity

    @Query("DELETE FROM submissions WHERE message_id = :messageId")
    suspend fun deleteSubmissionById(messageId: String)

    @Query("DELETE FROM submissions")
    suspend fun deleteAllSubmissions()
}