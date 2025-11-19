package com.example.newsapp.db

import androidx.room.TypeConverter
import com.example.newsapp.models.Source
import com.google.gson.Gson

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSource(source: Source): String {
        return gson.toJson(source) // chuyển Source thành JSON string
    }

    @TypeConverter
    fun toSource(sourceString: String): Source {
        return gson.fromJson(sourceString, Source::class.java) // chuyển JSON string về Source
    }
}