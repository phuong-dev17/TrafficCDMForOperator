package org.deplide.application.android.trafficcdmforoperator.repository.db

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type


class Converters {
    private val moshi: Moshi = Moshi.Builder().build()
    val type: Type = Types.newParameterizedType(
        List::class.java,
        String::class.java
    )
    var adapter: JsonAdapter<List<String>> = moshi.adapter(type)

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toList(jsonString: String?): List<String> {
        return adapter.fromJson(jsonString)!!
    }
}