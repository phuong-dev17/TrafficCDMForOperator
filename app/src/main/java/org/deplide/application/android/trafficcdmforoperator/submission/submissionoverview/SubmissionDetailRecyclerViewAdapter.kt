package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.deplide.application.android.trafficcdmforoperator.databinding.CellSubmittedTimestampBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class SubmissionDetailRecyclerViewAdapter(
    private val submissions: List<SubmissionData>,
    private val onViewMessage: (String) -> Unit,
    private val onCopyMessage: (String) -> Unit,
    private val onModifyMessage: (String) -> Unit,
    private val onUndoMessage: (String) -> Unit): RecyclerView.Adapter<SubmissionDetailRecyclerViewAdapter.SubmissionDetailViewHolder>() {

    class SubmissionDetailViewHolder(
        private val binding: CellSubmittedTimestampBinding,
        private val onViewMessage: (String) -> Unit,
        private val onCopyMessage: (String) -> Unit,
        private val onModifyMessage: (String) -> Unit,
        private val onUndoMessage: (String) -> Unit
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(submissionData: SubmissionData) {
            binding.apply {
                textViewMessageId.text = submissionData.messageId.split(":")[2]
                textViewDescription.text = submissionData.getDescription()

                root.setOnClickListener {
                    onViewMessage(submissionData.messageId)
                }
                root.setOnLongClickListener {
                    onCopyMessage(submissionData.messageId)
                    true
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup,
                       onViewMessage: (String) -> Unit,
                       onCopyMessage: (String) -> Unit,
                       onModifyMessage: (String) -> Unit,
                       onUndoMessage: (String) -> Unit): SubmissionDetailViewHolder {
                val binding = CellSubmittedTimestampBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return SubmissionDetailViewHolder(binding,
                    onViewMessage,
                    onCopyMessage,
                    onModifyMessage,
                    onUndoMessage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionDetailViewHolder {
        return SubmissionDetailViewHolder.create(parent,
            onViewMessage, onCopyMessage, onModifyMessage, onUndoMessage)
    }

    override fun getItemCount(): Int {
        return submissions.size
    }

    override fun onBindViewHolder(holder: SubmissionDetailViewHolder, position: Int) {
        holder.bind(submissions[position])
    }
}