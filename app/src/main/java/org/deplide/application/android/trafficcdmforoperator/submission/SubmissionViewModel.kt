package org.deplide.application.android.trafficcdmforoperator.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class SubmissionViewModel: ViewModel() {
    companion object {
        fun factory(): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SubmissionViewModel()
                }
            }
        }
    }
}