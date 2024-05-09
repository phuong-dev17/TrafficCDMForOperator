package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentLocationStateBinding

class LocationStateFragment : Fragment(), StateFragmentDataUpdater {
    private lateinit var binding: FragmentLocationStateBinding
    private val data: MutableMap<String, String> = mutableMapOf(
        SubmissionData.FIELD_LOCATION to "",
        SubmissionData.FIELD_REFERENCE_OBJECT to "",
        SubmissionData.FIELD_TIME_TYPE to "",
    )
    private var dataUpdateListener: StateFragmentDataUpdateListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLocationStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            edtLocationLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_LOCATION, text!!.toString())
                }
            )

            edtReferenceObjectLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_REFERENCE_OBJECT, text!!.toString())
                }
            )

            edtTimeTypeLocationState.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    updateData(SubmissionData.FIELD_TIME_TYPE, text!!.toString())
                }
            )
        }
    }

    private fun updateData(key: String, value: String) {
        data[key] = value.toString()
        informListeners()
    }

    private fun informListeners() {
        dataUpdateListener?.onStateFragmentDataUpdate(data)
    }

    override fun addStateFragmentDataUpdateListener(listener: StateFragmentDataUpdateListener) {
        dataUpdateListener = listener
    }
}