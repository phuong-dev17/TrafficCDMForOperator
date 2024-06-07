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
import org.deplide.application.android.trafficcdmforoperator.repository.SubmissionDBFactory
import org.deplide.application.android.trafficcdmforoperator.repository.SubmissionDBInterface
import org.deplide.application.android.trafficcdmforoperator.submission.submittimestamp.SubmitTimestampViewModel

class SubmissionOverviewViewModel(applicationContext: Context): ViewModel() {
    private val submissionDB: SubmissionDBInterface by lazy {
        SubmissionDBFactory.getSubmissionDB(SubmissionDBFactory.REAL_DB,
            applicationContext)!!
    }
    private var _uiState: MutableStateFlow<SubmissionOverviewUIState>
                    = MutableStateFlow(SubmissionOverviewUIState.Loading)
    val uiState: StateFlow<SubmissionOverviewUIState> = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val submittedMessages = submissionDB.getSubmissions()

                _uiState.value = SubmissionOverviewUIState.Success(submittedMessages)
            } catch (ex: Exception) {
                _uiState.value = SubmissionOverviewUIState.Error("$ex")
            }
        }
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