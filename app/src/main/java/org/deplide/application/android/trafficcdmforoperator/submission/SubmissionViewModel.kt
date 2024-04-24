package org.deplide.application.android.trafficcdmforoperator.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.deplide.application.android.trafficcdmforoperator.toolbox.AuthToolBox

class SubmissionViewModel: ViewModel() {
    private val authToolBox = AuthToolBox.getInstance()

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