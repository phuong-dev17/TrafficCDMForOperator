package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.deplide.application.android.trafficcdmforoperator.databinding.CellSubmittedTimestampBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class SubmissionDetailRecyclerViewAdapter(private val submissions: List<SubmissionData>): RecyclerView.Adapter<SubmissionDetailRecyclerViewAdapter.SubmissionDetailViewHolder>() {

    class SubmissionDetailViewHolder(
        private val binding: CellSubmittedTimestampBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(submissionData: SubmissionData) {
            binding.apply {
                textViewMessageId.text = submissionData.messageId.split(":")[2]
                textViewDescription.text = submissionData.getDescription()
            }
        }

        companion object {
            fun create(parent: ViewGroup): SubmissionDetailViewHolder {
                val binding = CellSubmittedTimestampBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return SubmissionDetailViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionDetailViewHolder {
        return SubmissionDetailViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return submissions.size
    }

    override fun onBindViewHolder(holder: SubmissionDetailViewHolder, position: Int) {
        holder.bind(submissions[position])
    }
}