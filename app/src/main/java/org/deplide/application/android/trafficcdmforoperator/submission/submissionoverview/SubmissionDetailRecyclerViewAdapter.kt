package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.CellSubmittedTimestampBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class SubmissionDetailRecyclerViewAdapter(
    private val submissions: List<SubmissionData>,
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (String) -> Unit,
    private val onItemSwipeLeft: (String) -> Unit,
    private val onItemSwipeRight: (String) -> Unit): RecyclerView.Adapter<SubmissionDetailRecyclerViewAdapter.SubmissionDetailViewHolder>() {

    class SubmissionDetailViewHolder(
        private val binding: CellSubmittedTimestampBinding,
        private val onItemClick: (String) -> Unit,
        private val onItemLongClick: (String) -> Unit
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(submissionData: SubmissionData) {
            binding.apply {
                val objectInConcern = submissionData.getObjectInConcern()
                val objectType = objectInConcern.split(" ")
                imageViewObjectInConcern.setImageResource(getObjectTypeIcon(objectType[0]))
                textViewObjectInConcern.text = objectInConcern
                textViewDescription.text = submissionData.getDescription(
                    root.context.getString(R.string.date_time_pattern))

                root.setOnClickListener {
                    onItemClick(submissionData.messageId)
                }
                root.setOnLongClickListener {
                    onItemLongClick(submissionData.messageId)
                    true
                }
            }
        }

        private fun getObjectTypeIcon(objectType: String): Int {
            return when (objectType) {
                "Truck" -> R.drawable.object_in_concern_truck_24dp
                "Ship" -> R.drawable.object_in_conern_ship_24dp
                "Train" -> R.drawable.object_in_concern_train_24dp
                "Airplane" -> R.drawable.object_in_concern_airplane_24dp
                "Service" -> R.drawable.object_in_concern_service_24dp
                else -> R.drawable.default_24dp
            }
        }

        companion object {
            fun create(parent: ViewGroup,
                       onItemClick: (String) -> Unit,
                       onItemLongClick: (String) -> Unit): SubmissionDetailViewHolder {
                val binding = CellSubmittedTimestampBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return SubmissionDetailViewHolder(binding,
                    onItemClick,
                    onItemLongClick)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionDetailViewHolder {
        return SubmissionDetailViewHolder.create(parent,
            onItemClick, onItemLongClick)
    }

    override fun getItemCount(): Int {
        return submissions.size
    }

    override fun onBindViewHolder(holder: SubmissionDetailViewHolder, position: Int) {
        holder.bind(submissions[position])
    }

    fun itemSwipedRight(position: Int) {
        val messageId = getMessageIdAtPosition(position)

        onItemSwipeRight(messageId)
    }

    fun itemSwipedLeft(position: Int) {
        val messageId = getMessageIdAtPosition(position)

        onItemSwipeLeft(messageId)
    }

    private fun getMessageIdAtPosition(position: Int): String {
        return submissions[position].messageId
    }
}