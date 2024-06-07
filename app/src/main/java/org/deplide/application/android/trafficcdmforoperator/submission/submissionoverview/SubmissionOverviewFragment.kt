package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentSubmissionOverviewBinding
import org.deplide.application.android.trafficcdmforoperator.submission.OnBackPressListener
import org.deplide.application.android.trafficcdmforoperator.submission.SubmissionActivity
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampFragment

class SubmissionOverviewFragment : Fragment() {
    private lateinit var binding: FragmentSubmissionOverviewBinding
    private lateinit var navController: NavController
    private val viewModel: SubmissionOverviewViewModel by viewModels{
        SubmissionOverviewViewModel.factory(requireContext())
    }
    private var backPressListener: OnBackPressListener? = null
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (requireActivity() as SubmissionActivity).onBackPress()
                }
            }
        )

        configureListeners()
        observeUIState()
    }

    fun addOnBackPressListener(listener: OnBackPressListener) {
        backPressListener = listener
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
        binding.chipGroupSubmissionFilers.visibility = View.VISIBLE

        val timeSequenceSet = mutableSetOf<String>()
        for (submission in submissions) {
            timeSequenceSet.add(submission.timeSequence!!)
        }
        val timeSequenceList = timeSequenceSet.toMutableList()
        timeSequenceList.add(0, getString(R.string.time_sequence))
        configureChip(
            binding.chipTimeSequence,
            getString(R.string.time_sequence),
            timeSequenceList
        )

        val groupingSet = mutableSetOf<String>()

        for (submission in submissions) {
            val tempSet = submission.grouping.toSet()

            groupingSet.addAll(tempSet)
        }
        val groupingList = groupingSet.toMutableList()
        groupingList.add(0, getString(R.string.grouping))
        configureChip(
            binding.chipGrouping,
            getString(R.string.grouping),
            groupingList
        )

        val adapter = SubmissionDetailRecyclerViewAdapter(
            submissions,
            onItemClick = ::navigateToViewExistingTimestamp,
            onItemLongClick = ::navigateToEditCopiedTimestamp,
            onItemSwipeLeft = ::navigateToUndoTimestamp,
            onItemSwipeRight = ::navigateToModifyTimestamp)

        binding.rvSubmittedTimeStamp.adapter = adapter

        binding.rvSubmittedTimeStamp.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val itemTouchHelper = ItemTouchHelper(
            SubmissionOverviewSwipeCallback(adapter, requireContext()))
        itemTouchHelper.attachToRecyclerView(binding.rvSubmittedTimeStamp)
    }

    private fun onLoading() {
        Log.d(TAG, "onLoading")
        binding.pbSubmittedTimeStamp.visibility = View.VISIBLE
        binding.rvSubmittedTimeStamp.visibility = View.GONE
        binding.chipGroupSubmissionFilers.visibility = View.GONE
    }

    private fun onError(message: String) {
        Log.d(TAG, "onError: $message")
        binding.pbSubmittedTimeStamp.visibility = View.GONE
        binding.rvSubmittedTimeStamp.visibility = View.GONE
        binding.chipGroupSubmissionFilers.visibility = View.GONE
    }

    private fun configureListeners() {
        binding.fabSubmitNewTimestamp.setOnClickListener {
            navigateToSubmitNewTimestamp()
        }
    }

    private fun configureChip(chip: Chip, chipTitle: String, items: List<String>) {
        val listPopupWindow = ListPopupWindow(requireContext())
        val adapter = ArrayAdapter(
            requireContext(), R.layout.cell_location, items)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.anchorView = chip

        chip.setOnClickListener { it ->
            listPopupWindow.show()
            (it as? Chip)?.apply {
                if (isChecked) {
                    text = chipTitle
                }
                updateChipDropdownArrow(chip)
            }
        }

        listPopupWindow.setOnItemClickListener { _, view, _, _ ->
            chip.text = (view as? TextView)?.text
            if (chip.text == chipTitle) {
                viewModel.clearFilter(chipTitle)
            } else {
                viewModel.setFilter(chipTitle, chip.text!!.toString())
            }
            chip.isChecked = false
            updateChipDropdownArrow(chip)
            listPopupWindow.dismiss()
        }
        listPopupWindow.setOnDismissListener {
            chip.isChecked = false
            updateChipDropdownArrow(chip)
        }
    }

    private fun updateChipDropdownArrow(
        chip: Chip,
    ) {
        chip.closeIcon = if (chip.isChecked) {
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.baseline_arrow_drop_down_24
            )
        } else {
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.baseline_arrow_drop_down_24
            )
        }
    }

    companion object {
        private const val TAG = "SubmissionOverviewFragment"
    }
}