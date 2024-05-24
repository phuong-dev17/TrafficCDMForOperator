package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentSubmissionOverviewBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment

class SubmissionOverviewFragment : Fragment() {
    private lateinit var binding: FragmentSubmissionOverviewBinding
    private lateinit var navController: NavController
    private val viewModel: SubmissionOverviewViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubmissionOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()

        observeUIState()

        binding.fabSubmitNewTimestamp.setOnClickListener {
            navigateToSubmitNewTimestamp()
        }
    }

    private fun navigateToSubmitNewTimestamp() {
        navigateToTimestampFragment(
            messageId = null,
            editMode = SubmitTimestampFragment.EDIT_MODE_NEW_MESSAGE)
    }

    private fun navigateToViewExistingTimestamp(messageId: String) {
        navigateToTimestampFragment(messageId = messageId, editMode = null)
    }

    private fun navigateToEditCopiedTimestamp(messageId: String) {
        navigateToTimestampFragment(
            messageId = messageId,
            editMode = SubmitTimestampFragment.EDIT_MODE_NEW_MESSAGE)
    }

    private fun navigateToModifyTimestamp(messageId: String) {
        navigateToTimestampFragment(
            messageId = messageId,
            editMode = SubmitTimestampFragment.EDIT_MODE_MODIFY_MESSAGE)
    }

    private fun navigateToUndoTimestamp(messageId: String) {
        navigateToTimestampFragment(
            messageId = messageId,
            editMode = SubmitTimestampFragment.EDIT_MODE_UNDO_MESSAGE)
    }

    private fun navigateToTimestampFragment(messageId: String? = null, editMode: String? = null) {
        val action =
            SubmissionOverviewFragmentDirections.actionSubmissionOverviewFragmentToSubmitTimestampFragment(
                messageId = messageId,
                editMode = editMode
            )

        navController.navigate(action)
    }

    private fun observeUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is SubmissionOverviewUIState.Error -> onError(uiState.message)
                        SubmissionOverviewUIState.Loading -> onLoading()
                        is SubmissionOverviewUIState.Success -> onSuccess(uiState.submissions)
                    }
                }
            }
        }
    }

    private fun onSuccess(submissions: List<SubmissionData>) {
        Log.d(TAG, "onSuccess: $submissions")
        binding.pbSubmittedTimeStamp.visibility = View.GONE
        binding.rvSubmittedTimeStamp.visibility = View.VISIBLE

        val adapter = SubmissionDetailRecyclerViewAdapter(
            submissions,
            onItemClick = ::navigateToViewExistingTimestamp,
            onItemLongClick = ::navigateToEditCopiedTimestamp,
            onItemSwipe = ::navigateToModifyTimestamp)

        binding.rvSubmittedTimeStamp.adapter = adapter

        binding.rvSubmittedTimeStamp.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return makeMovementFlags(0, ItemTouchHelper.LEFT)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val messageId = submissions[position].messageId
                    navigateToUndoTimestamp(messageId)
                }

            }
        )
        itemTouchHelper.attachToRecyclerView(binding.rvSubmittedTimeStamp)
    }

    private fun onLoading() {
        Log.d(TAG, "onLoading")
        binding.pbSubmittedTimeStamp.visibility = View.VISIBLE
        binding.rvSubmittedTimeStamp.visibility = View.GONE
    }

    private fun onError(message: String) {
        Log.d(TAG, "onError: $message")
        binding.pbSubmittedTimeStamp.visibility = View.GONE
        binding.rvSubmittedTimeStamp.visibility = View.GONE
    }

    companion object {
        private const val TAG = "SubmissionOverviewFragment"
    }
}