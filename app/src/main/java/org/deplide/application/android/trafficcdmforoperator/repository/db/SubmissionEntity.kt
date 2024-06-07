package org.deplide.application.android.trafficcdmforoperator.repository.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey @ColumnInfo(name = "message_id") var messageId: String,
    //meta data
    @ColumnInfo(name = "version") var version: String,
    @ColumnInfo(name = "reported_at") var reportedAt: String,
    @ColumnInfo(name = "reported_by") var reportedBy: String,
    @ColumnInfo(name = "source") var source: String? = null,

    //grouping
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "grouping") var grouping: List<String>,

    //payload
    @ColumnInfo("type") var type: String,
    @ColumnInfo(name = "time") var time: String? = null,
    @ColumnInfo("time_sequence") var timeSequence: String?  = null,
    @ColumnInfo("time_type") var timeType: String?  = null,
    @ColumnInfo("reference_object") var referenceObject: String?  = null,
    @ColumnInfo("location") var location: String?  = null,
    @ColumnInfo("service") var service: String?  = null,
    @ColumnInfo("carrier") var carrier: String?  = null,
    @ColumnInfo("attribute") var attribute: String?  = null,
) {
    constructor(submissionData: SubmissionData): this(
        messageId = submissionData.messageId,
        version = submissionData.version,
        reportedAt = submissionData.reportedAt,
        reportedBy = submissionData.reportedBy,
        source = submissionData.source,
        grouping = submissionData.grouping,
        type = submissionData.type,
        time = submissionData.time,
        timeSequence = submissionData.timeSequence,
        timeType = submissionData.timeType,
        referenceObject = submissionData.referenceObject,
        location = submissionData.location,
        service = submissionData.service,
        carrier = submissionData.carrier,
        attribute = submissionData.attribute
    )
}