package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.repository.SubmissionDBFactory
import org.deplide.application.android.trafficcdmforoperator.repository.SubmissionDBInterface
import org.deplide.application.android.trafficcdmforoperator.submission.data.version_0_0_7.SubmissionData

class SubmissionOverviewViewModel(private val applicationContext: Context): ViewModel() {
    private val submissionDB: SubmissionDBInterface by lazy {
        SubmissionDBFactory.getSubmissionDB(SubmissionDBFactory.REAL_DB,
            applicationContext)!!
    }
    private var _uiState: MutableStateFlow<SubmissionOverviewUIState>
                    = MutableStateFlow(SubmissionOverviewUIState.Loading)
    val uiState: StateFlow<SubmissionOverviewUIState> = _uiState

    private var currentFilters = mutableMapOf<String, String>()

    init {
        loadSubmissions()
    }

    fun setFilter(filterName: String, filterValue: String) {
        currentFilters[filterName] = filterValue
        loadSubmissions()
    }

    fun clearFilter(filterName: String) {
        currentFilters.remove(filterName)
        loadSubmissions()
    }

    private fun loadSubmissions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val submissionList = submissionDB.getSubmissions()

                val filteredSubmissionList = runFilters(submissionList)

                _uiState.value = SubmissionOverviewUIState.Success(filteredSubmissionList)
            } catch (ex: Exception) {
                _uiState.value = SubmissionOverviewUIState.Error("$ex")
            }
        }
    }

    private fun runFilters(submissionList: List<SubmissionData>): List<SubmissionData> {
        var returnList = submissionList
        for ((filterName, filterValue) in currentFilters) {
            returnList = when (filterName) {
                applicationContext.getString(R.string.time_sequence)
                -> returnList.filter { it.timeSequence == filterValue }

                applicationContext.getString(R.string.grouping)
                -> returnList.filter { it.grouping.contains(filterValue) }

                else -> emptyList()
            }
        }

        return returnList
    }

    companion object {
        const val TAG = "SubmissionOverviewViewModel"
        fun factory(applicationContext: Context): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SubmissionOverviewViewModel(applicationContext)
                }
            }
        }
    }
}