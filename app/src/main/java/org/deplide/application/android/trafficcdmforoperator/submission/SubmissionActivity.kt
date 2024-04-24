package org.deplide.application.android.trafficcdmforoperator.submission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.deplide.application.android.trafficcdmforoperator.R

class SubmissionActivity : AppCompatActivity() {
    private val viewModel: SubmissionViewModel by viewModels { SubmissionViewModel.factory() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submission)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        fun intent(srcCtx: Context): Intent {
            return Intent(srcCtx, SubmissionActivity::class.java)
        }
    }
}