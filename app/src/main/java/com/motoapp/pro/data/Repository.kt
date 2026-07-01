package com.motoapp.pro.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.motoapp.pro.model.*
import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    private val cxDao = db.caixinhaDao()
    private val txDao = db.transacaoDao()
    private val jDao = db.jornadaDao()
    private val cDao = db.configDao()
    private val gson = Gson()

    // Caixinhas
    fun allCaixinhasFlow(): Flow<List<Caixinha>> = cxDao.getAllFlow()
    suspend fun allCaixinhas(): List<Caixinha> = cxDao.getAll()
    suspend fun getCaixinha(id: String): Caixinha? = cxDao.getById(id)
    suspend fun saveCaixinhas(list: List<Caixinha>) = cxDao.insertAll(list)
    suspend fun saveCaixinha(c: Caixinha) = cxDao.insert(c)
    suspend fun updateCaixinha(c: Caixinha) = cxDao.update(c)
    suspend fun deleteCaixinha(id: String) = cxDao.deleteById(id)
    suspend fun updateSaldo(id: String, saldo: Long) = cxDao.updateSaldo(id, saldo)
    suspend fun caixinhaCount(): Int = cxDao.count()
    suspend fun deleteAllCaixinhas() = cxDao.deleteAll()

    // Transacoes
    fun allTransacoesFlow(): Flow<List<Transacao>> = txDao.getAllFlow()
    suspend fun allTransacoes(): List<Transacao> = txDao.getAll()
    suspend fun transacoesFrom(desde: Long): List<Transacao> = txDao.getFrom(desde)
    suspend fun transacoesByDate(data: String): List<Transacao> = txDao.getByDate(data)
    suspend fun saveTransacao(t: Transacao) = txDao.insert(t)
    suspend fun saveTransacoes(list: List<Transacao>) = txDao.insertAll(list)
    suspend fun deleteAllTransacoes() = txDao.deleteAll()

    // Jornada
    suspend fun getJornadaAtiva(): Jornada? = jDao.getAtiva()
    suspend fun saveJornada(j: Jornada) = jDao.insert(j)
    suspend fun updateJornada(j: Jornada) = jDao.update(j)
    suspend fun finalizarJornada(id: String, km: Int, fim: Long) = jDao.finalizar(id, km, fim)
    suspend fun deleteJornada(id: String) = jDao.deleteById(id)
    suspend fun deleteAllJornadas() = jDao.deleteAll()

    // Config
    suspend fun getConfig(chave: String): String? = cDao.getValor(chave)
    suspend fun setConfig(chave: String, valor: String) = cDao.set(Config(chave, valor))
    suspend fun getAllConfig(): List<Config> = cDao.getAll()
    suspend fun deleteAllConfig() = cDao.deleteAll()

    // Lancamentos JSON
    fun lancamentosToJson(list: List<Lancamento>): String = gson.toJson(list)
    fun lancamentosFromJson(json: String): List<Lancamento> {
        return try {
            val type = object : TypeToken<List<Lancamento>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun generateId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val random = (1..16).map { chars.random() }.joinToString("")
        return "$random${System.currentTimeMillis().toString(36)}"
    }

    suspend fun loadAllData(): AppData {
        return AppData(
            caixinhas = cxDao.getAll(),
            transacoes = txDao.getAll(),
            jornada = jDao.getAtiva(),
            carteira = getConfig(Config.CARREIRA)?.toLongOrNull() ?: 0L,
            extra = getConfig(Config.EXTRA)?.toLongOrNull() ?: 0L,
            custoMensal = getConfig(Config.CUSTO_MENSAL)?.toLongOrNull() ?: 150000L,
            emgId = getConfig(Config.EMERGENCIA_ID),
            loaded = getConfig(Config.LOADED) == "true"
        )
    }

    suspend fun saveAllData(data: AppData) {
        cxDao.insertAll(data.caixinhas)
        txDao.insertAll(data.transacoes)
        data.jornada?.let { jDao.insert(it) }
        setConfig(Config.CARREIRA, data.carteira.toString())
        setConfig(Config.EXTRA, data.extra.toString())
        setConfig(Config.CUSTO_MENSAL, data.custoMensal.toString())
        data.emgId?.let { setConfig(Config.EMERGENCIA_ID, it) }
        setConfig(Config.LOADED, data.loaded.toString())
    }

    suspend fun clearAll() {
        cxDao.deleteAll()
        txDao.deleteAll()
        jDao.deleteAll()
        cDao.deleteAll()
    }
}

data class AppData(
    val caixinhas: List<Caixinha> = emptyList(),
    val transacoes: List<Transacao> = emptyList(),
    val jornada: Jornada? = null,
    val carteira: Long = 0,
    val extra: Long = 0,
    val custoMensal: Long = 150000,
    val emgId: String? = null,
    val loaded: Boolean = false
)
