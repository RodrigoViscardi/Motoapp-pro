package com.motoapp.pro.util

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.motoapp.pro.data.AppData
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.model.Transacao

data class BackupData(
    val versao: Int = 1,
    val data: String = DateUtils.today(),
    val caixinhas: List<Caixinha> = emptyList(),
    val transacoes: List<Transacao> = emptyList(),
    val carteira: Long = 0,
    val extra: Long = 0,
    val custoMensal: Long = 150000,
    val emgId: String? = null
)

object BackupManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun exportData(appData: AppData): String {
        val backup = BackupData(
            caixinhas = appData.caixinhas,
            transacoes = appData.transacoes,
            carteira = appData.carteira,
            extra = appData.extra,
            custoMensal = appData.custoMensal,
            emgId = appData.emgId
        )
        return gson.toJson(backup)
    }

    fun importData(json: String): AppData? {
        return try {
            val backup = gson.fromJson(json, BackupData::class.java)
            AppData(
                caixinhas = backup.caixinhas,
                transacoes = backup.transacoes,
                jornada = null,
                carteira = backup.carteira,
                extra = backup.extra,
                custoMensal = backup.custoMensal,
                emgId = backup.emgId,
                loaded = true
            )
        } catch (e: Exception) {
            null
        }
    }
}
