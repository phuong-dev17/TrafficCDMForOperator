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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentSubmissionOverviewBinding
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class SubmissionOverviewFragment : Fragment() {
    private lateinit var binding: FragmentSubmissionOverviewBinding
    private lateinit var navController: NavController
    private val viewModel: SubmissionOverviewViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()

        observeUIState()

        binding.fabSubmitNewTimestamp.setOnClickListener {
            val action =
                SubmissionOverviewFragmentDirections.actionSubmissionOverviewFragmentToSubmitTimestampFragment(
                    messageId = null,
                    editable = true)

            navController.navigate(action)
        }
    }

    private fun observeUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect() { uiState ->
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

        binding.rvSubmittedTimeStamp.adapter =
            SubmissionDetailRecyclerViewAdapter(submissions)

        binding.rvSubmittedTimeStamp.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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