package com.motoapp.pro.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.motoapp.pro.model.Lancamento

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLancamentoList(value: String): List<Lancamento> {
        return try {
            val type = object : TypeToken<List<Lancamento>>() {}.type
            gson.fromJson(value, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toLancamentoList(list: List<Lancamento>): String {
        return gson.toJson(list)
    }
}
