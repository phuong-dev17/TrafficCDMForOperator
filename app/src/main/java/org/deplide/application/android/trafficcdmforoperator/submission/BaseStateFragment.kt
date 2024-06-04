package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.lang.reflect.Type

open class BaseStateFragment: Fragment(), StateFragmentDataUpdater  {
    private val data: MutableMap<String, String> = mutableMapOf()
    private var dataUpdateListener: StateFragmentDataUpdateListener? = null
    protected lateinit var locations: Map<String, String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadLocations()
    }

    protected fun updateData(key: String, value: String) {
        data[key] = value
        informListeners()
    }

    private fun informListeners() {
        dataUpdateListener?.onStateFragmentDataUpdate(data)
    }

    private fun loadLocations() {
        val locationJsonString = readLocationJsonFile()

        val loadedLocations: Map<String, String> = parseLocationJsonString(locationJsonString)

        locations = makeLocationFullNameAsKeyInTheMap(loadedLocations)
    }

    private fun makeLocationFullNameAsKeyInTheMap(loadedLocations: Map<String, String>) =
        loadedLocations.map { (k, v) -> v to k }.toMap()
    private fun parseLocationJsonString(locationJsonString: String): Map<String, String> {
        val moshi: Moshi = Moshi.Builder().build()

        val type: Type = Types.newParameterizedType(
            MutableMap::class.java,
            String::class.java,
            String::class.java
        )
        val jsonAdapter: JsonAdapter<Map<String, String>> = moshi.adapter(type)

        val loadedLocations: Map<String, String> = jsonAdapter.fromJson(locationJsonString)!!
        return loadedLocations
    }

    /*
     * This complete function is copied from accepted answer in Stackoverflow
     * https://stackoverflow.com/questions/6349759/using-json-file-in-android-app-resources
     */
    private fun readLocationJsonFile(): String {
        val inputStream = resources.openRawResource(org.deplide.application.android.trafficcdmforoperator.R.raw.places)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader: Reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var n: Int
            while ((reader.read(buffer).also { n = it }) != -1) {
                writer.write(buffer, 0, n)
            }
        } finally {
            inputStream.close()
        }

        return writer.toString()
    }

    override fun addStateFragmentDataUpdateListener(listener: StateFragmentDataUpdateListener) {
        dataUpdateListener = listener
    }
}