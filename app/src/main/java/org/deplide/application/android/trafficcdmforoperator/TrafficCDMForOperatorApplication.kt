package org.deplide.application.android.trafficcdmforoperator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

@SuppressLint("ServiceCast")
fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

class TrafficCDMForOperatorApplication: Application() {
    lateinit var authInfoProvider: AuthInfoProvider

    override fun onCreate() {
        super.onCreate()

        authInfoProvider = AuthInfoProvider.instance(applicationContext)
    }
}