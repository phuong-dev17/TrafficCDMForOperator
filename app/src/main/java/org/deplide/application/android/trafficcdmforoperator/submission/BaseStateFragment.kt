package org.deplide.application.android.trafficcdmforoperator.submission

import androidx.fragment.app.Fragment

open class BaseStateFragment: Fragment(), StateFragmentDataUpdater  {
    private val data: MutableMap<String, String> = mutableMapOf()
    private var dataUpdateListener: StateFragmentDataUpdateListener? = null

    protected fun updateData(key: String, value: String) {
        data[key] = value
        informListeners()
    }

    private fun informListeners() {
        dataUpdateListener?.onStateFragmentDataUpdate(data)
    }

    override fun addStateFragmentDataUpdateListener(listener: StateFragmentDataUpdateListener) {
        dataUpdateListener = listener
    }
}