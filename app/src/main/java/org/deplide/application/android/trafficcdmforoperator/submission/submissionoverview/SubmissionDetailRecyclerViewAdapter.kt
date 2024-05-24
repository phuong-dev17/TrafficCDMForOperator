package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.deplide.application.android.trafficcdmforoperator.databinding.CellSubmittedTimestampBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class SubmissionDetailRecyclerViewAdapter(
    private val submissions: List<SubmissionData>,
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (String) -> Unit,
    private val onItemSwipe: (String) -> Unit): RecyclerView.Adapter<SubmissionDetailRecyclerViewAdapter.SubmissionDetailViewHolder>() {

    class SubmissionDetailViewHolder(
        private val binding: CellSubmittedTimestampBinding,
        private val onItemClick: (String) -> Unit,
        private val onItemLongClick: (String) -> Unit,
        private val onItemSwipe: (String) -> Unit
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(submissionData: SubmissionData) {
            binding.apply {
                textViewMessageId.text = submissionData.messageId.split(":")[2]
                textViewDescription.text = submissionData.getDescription()

                root.setOnClickListener {
                    onItemClick(submissionData.messageId)
                }
                root.setOnLongClickListener {
                    onItemLongClick(submissionData.messageId)
                    true
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup,
                       onItemClick: (String) -> Unit,
                       onItemLongClick: (String) -> Unit,
                       onItemSwipe: (String) -> Unit): SubmissionDetailViewHolder {
                val binding = CellSubmittedTimestampBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return SubmissionDetailViewHolder(binding,
                    onItemClick,
                    onItemLongClick,
                    onItemSwipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionDetailViewHolder {
        return SubmissionDetailViewHolder.create(parent,
            onItemClick, onItemLongClick, onItemSwipe)
    }

    override fun getItemCount(): Int {
        return submissions.size
    }

    override fun onBindViewHolder(holder: SubmissionDetailViewHolder, position: Int) {
        holder.bind(submissions[position])
    }
}