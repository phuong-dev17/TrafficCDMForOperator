package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.deplide.application.android.trafficcdmforoperator.repository.SubmittedMessageDBFactory
import org.deplide.application.android.trafficcdmforoperator.repository.SubmittedMessageDBInterface

class SubmissionOverviewViewModel: ViewModel() {
    private val submittedMessageDB: SubmittedMessageDBInterface by lazy {
        SubmittedMessageDBFactory.getSubmittedMessageDB(SubmittedMessageDBFactory.FAKE_DB)!!
    }
    private var _uiState: MutableStateFlow<SubmissionOverviewUIState>
                    = MutableStateFlow(SubmissionOverviewUIState.Loading)
    val uiState: StateFlow<SubmissionOverviewUIState> = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val submittedMessages = submittedMessageDB.getSubmittedMessages()

                _uiState.value = SubmissionOverviewUIState.Success(submittedMessages)
            } catch (ex: Exception) {
                _uiState.value = SubmissionOverviewUIState.Error("$ex")
            }
        }
    }
}