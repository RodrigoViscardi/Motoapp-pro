package com.motoapp.pro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motoapp.pro.data.AppData
import com.motoapp.pro.data.AppDatabase
import com.motoapp.pro.data.Repository
import com.motoapp.pro.model.*
import com.motoapp.pro.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppUiState(
    val carteira: Long = 0,
    val extra: Long = 0,
    val caixinhas: List<Caixinha> = emptyList(),
    val transacoes: List<Transacao> = emptyList(),
    val jornada: Jornada? = null,
    val lancamentos: List<Lancamento> = emptyList(),
    val custoMensal: Long = 150000,
    val emgId: String? = null,
    val periodo: String = "hoje",
    val survivalDays: Int = 0,
    val loading: Boolean = true,
    val toastMessage: String? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: Repository
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repo = Repository(db)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val data = repo.loadAllData()
                val lancs = if (data.jornada != null) repo.lancamentosFromJson(data.jornada.lancamentos) else emptyList()
                val survival = calcSurvival(data.caixinhas, data.emgId, data.custoMensal)

                _uiState.value = AppUiState(
                    carteira = data.carteira,
                    extra = data.extra,
                    caixinhas = data.caixinhas,
                    transacoes = data.transacoes,
                    jornada = data.jornada,
                    lancamentos = lancs,
                    custoMensal = data.custoMensal,
                    emgId = data.emgId,
                    survivalDays = survival,
                    loading = false
                )

                if (!data.loaded) carregarDemo()
            } catch (e: Exception) {
                _toast.emit("Erro ao carregar: ${e.message}")
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    private fun carregarDemo() {
        viewModelScope.launch {
            val futureDate = {
                val d = java.util.Calendar.getInstance()
                d.add(java.util.Calendar.DAY_OF_YEAR, (5..30).random())
                DateUtils.formatApi(d.time)
            }

            val demos = listOf(
                Caixinha(id = repo.generateId(), nome = "Gasolina", valorTotal = 40000, prioridade = 1, saldoAtual = 12000, vencimento = futureDate(), cor = "#F85149"),
                Caixinha(id = repo.generateId(), nome = "Aluguel", valorTotal = 120000, prioridade = 2, saldoAtual = 60000, vencimento = futureDate(), cor = "#D29922"),
                Caixinha(id = repo.generateId(), nome = "Internet", valorTotal = 12000, prioridade = 3, saldoAtual = 12000, vencimento = futureDate(), cor = "#3FB950"),
                Caixinha(id = repo.generateId(), nome = "Reserva", valorTotal = 300000, prioridade = 10, saldoAtual = 80000, vencimento = futureDate(), cor = "#8957E5"),
                Caixinha(id = repo.generateId(), nome = "Manutenção", valorTotal = 30000, prioridade = 4, saldoAtual = 10000, vencimento = futureDate(), cor = "#58A6FF")
            )

            val emgId = demos[3].id
            val tx = Transacao(repo.generateId(), System.currentTimeMillis(), "ENTRADA", 25000, nota = "Saldo inicial", data = DateUtils.today())

            repo.saveCaixinhas(demos)
            repo.saveTransacao(tx)
            repo.setConfig(Config.CARREIRA, "25000")
            repo.setConfig(Config.EXTRA, "0")
            repo.setConfig(Config.CUSTO_MENSAL, "150000")
            repo.setConfig(Config.EMERGENCIA_ID, emgId)
            repo.setConfig(Config.LOADED, "true")

            _uiState.value = _uiState.value.copy(
                carteira = 25000,
                caixinhas = demos,
                transacoes = listOf(tx),
                emgId = emgId,
                survivalDays = calcSurvival(demos, emgId, 150000),
                loading = false
            )
        }
    }

    fun adicionarGanho(valor: Long, origem: String = "Manual") {
        viewModelScope.launch {
            val tx = Transacao(repo.generateId(), System.currentTimeMillis(), "ENTRADA", valor, nota = "Ganho via $origem", data = DateUtils.today())
            repo.saveTransacao(tx)
            val novaCarteira = _uiState.value.carteira + valor
            repo.setConfig(Config.CARREIRA, novaCarteira.toString())
            _uiState.value = _uiState.value.copy(
                carteira = novaCarteira,
                transacoes = listOf(tx) + _uiState.value.transacoes
            )
            _toast.emit("${CentimosFormatter.format(valor)} adicionado!")
        }
    }

    fun distribuirCascata() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.carteira <= 0) {
                _toast.emit("Carteira vazia!")
                return@launch
            }

            val alocacoes = mutableListOf<Alocacao>()
            val caixinhas = state.caixinhas.map { it.copy() }.toMutableList()
            var saldo = state.carteira
            val indicesPorPrioridade = caixinhas.indices.sortedBy { caixinhas[it].prioridade }

            for (i in indicesPorPrioridade) {
                if (saldo <= 0) break
                val cx = caixinhas[i]
                if (cx.valorTotal <= 0) continue
                val falta = cx.valorTotal - cx.saldoAtual
                if (falta <= 0) continue
                val valor = saldo.coerceAtMost(falta)
                caixinhas[i] = cx.copy(saldoAtual = cx.saldoAtual + valor)
                saldo -= valor
                alocacoes.add(Alocacao(cx.id, valor, valor >= falta))
            }

            if (alocacoes.isEmpty()) {
                _toast.emit("Todas já pagas")
                return@launch
            }

            val lucroExtra = if (saldo > 0) saldo else 0L

            val novasTransacoes = mutableListOf<Transacao>()
            alocacoes.forEach { a ->
                val cx = caixinhas.find { it.id == a.caixinhaId } ?: return@forEach
                val tipo = if (a.completa) "Completa" else "Parcial"
                novasTransacoes.add(
                    Transacao(repo.generateId(), System.currentTimeMillis(), "DISTRIB", a.valor,
                        destino = a.caixinhaId, nota = "$tipo → ${cx.nome}", data = DateUtils.today())
                )
            }
            if (lucroExtra > 0) {
                novasTransacoes.add(
                    Transacao(repo.generateId(), System.currentTimeMillis(), "ENTRADA", lucroExtra,
                        nota = "Lucro extra", data = DateUtils.today())
                )
            }

            repo.saveCaixinhas(caixinhas.map { it.copy() })
            novasTransacoes.forEach { repo.saveTransacao(it) }
            repo.setConfig(Config.CARREIRA, "0")
            repo.setConfig(Config.EXTRA, (state.extra + lucroExtra).toString())

            _uiState.value = _uiState.value.copy(
                carteira = 0,
                extra = state.extra + lucroExtra,
                caixinhas = caixinhas,
                transacoes = novasTransacoes.reversed() + state.transacoes
            )
            _toast.emit("Distribuído!")
        }
    }

    fun addCaixinha(nome: String, valorTotal: Long, prioridade: Int, vencimento: String?, cor: String) {
        viewModelScope.launch {
            val caixinhas = _uiState.value.caixinhas.toMutableList()
            caixinhas.forEach { if (it.prioridade >= prioridade) it.copy(prioridade = it.prioridade + 1) }
            val nova = Caixinha(repo.generateId(), nome, valorTotal, prioridade, 0, vencimento, cor)
            caixinhas.add(nova)
            val reordenadas = caixinhas.sortedBy { it.prioridade }.mapIndexed { i, c -> c.copy(prioridade = i + 1) }
            repo.saveCaixinhas(reordenadas)
            _uiState.value = _uiState.value.copy(caixinhas = reordenadas)
        }
    }

    fun updateCaixinha(id: String, nome: String, valorTotal: Long, prioridade: Int, vencimento: String?, cor: String) {
        viewModelScope.launch {
            val caixinhas = _uiState.value.caixinhas.toMutableList()
            val idx = caixinhas.indexOfFirst { it.id == id }
            if (idx < 0) return@launch

            val antiga = caixinhas[idx]
            val reordenadas = caixinhas.toMutableList()

            if (prioridade != antiga.prioridade) {
                reordenadas.forEachIndexed { i, c ->
                    if (c.id == id) return@forEachIndexed
                    if (prioridade < antiga.prioridade) {
                        if (c.prioridade >= prioridade && c.prioridade < antiga.prioridade)
                            reordenadas[i] = c.copy(prioridade = c.prioridade + 1)
                    } else {
                        if (c.prioridade > antiga.prioridade && c.prioridade <= prioridade)
                            reordenadas[i] = c.copy(prioridade = c.prioridade - 1)
                    }
                }
            }

            reordenadas[idx] = antiga.copy(nome = nome, valorTotal = valorTotal, prioridade = prioridade, vencimento = vencimento, cor = cor)
            val finais = reordenadas.sortedBy { it.prioridade }.mapIndexed { i, c -> c.copy(prioridade = i + 1) }
            repo.saveCaixinhas(finais)
            _uiState.value = _uiState.value.copy(caixinhas = finais)
        }
    }

    fun deleteCaixinha(id: String) {
        viewModelScope.launch {
            val cx = _uiState.value.caixinhas.find { it.id == id } ?: return@launch
            val caixinhas = _uiState.value.caixinhas.filter { it.id != id }
            val reordenadas = caixinhas.sortedBy { it.prioridade }.mapIndexed { i, c -> c.copy(prioridade = i + 1) }
            if (cx.saldoAtual > 0) {
                val novaCarteira = _uiState.value.carteira + cx.saldoAtual
                repo.setConfig(Config.CARREIRA, novaCarteira.toString())
                _uiState.value = _uiState.value.copy(carteira = novaCarteira)
            }
            repo.deleteCaixinha(id)
            repo.saveCaixinhas(reordenadas)
            _uiState.value = _uiState.value.copy(caixinhas = reordenadas)
            _toast.emit("Caixinha removida")
        }
    }

    fun reordenarCaixinhas(deIndice: Int, paraIndice: Int) {
        viewModelScope.launch {
            val caixinhas = _uiState.value.caixinhas.toMutableList()
            if (deIndice < 0 || deIndice >= caixinhas.size || paraIndice < 0 || paraIndice >= caixinhas.size) return@launch
            val item = caixinhas.removeAt(deIndice)
            caixinhas.add(paraIndice, item)
            val reordenadas = caixinhas.mapIndexed { i, c -> c.copy(prioridade = i + 1) }
            repo.saveCaixinhas(reordenadas)
            _uiState.value = _uiState.value.copy(caixinhas = reordenadas)
        }
    }

    fun depositar(id: String, valor: Long) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.carteira < valor) { _toast.emit("Saldo insuficiente"); return@launch }
            val caixinhas = state.caixinhas.map { if (it.id == id) it.copy(saldoAtual = it.saldoAtual + valor) else it }
            val tx = Transacao(repo.generateId(), System.currentTimeMillis(), "TRANSF", valor,
                origem = "Carteira", destino = id, nota = "Depósito", data = DateUtils.today())
            repo.saveCaixinhas(caixinhas)
            repo.saveTransacao(tx)
            repo.setConfig(Config.CARREIRA, (state.carteira - valor).toString())
            _uiState.value = _uiState.value.copy(carteira = state.carteira - valor, caixinhas = caixinhas, transacoes = listOf(tx) + state.transacoes)
        }
    }

    fun pagar(id: String, valor: Long) {
        viewModelScope.launch {
            val state = _uiState.value
            val cx = state.caixinhas.find { it.id == id } ?: return@launch
            val pago = valor.coerceAtMost(cx.saldoAtual)
            val caixinhas = state.caixinhas.map {
                if (it.id == id) it.copy(saldoAtual = it.saldoAtual - pago, valorTotal = (it.valorTotal - pago).coerceAtLeast(0))
                else it
            }
            val tx = Transacao(repo.generateId(), System.currentTimeMillis(), "SAIDA", pago,
                destino = id, nota = "Pagamento: ${cx.nome}", data = DateUtils.today())
            repo.saveCaixinhas(caixinhas)
            repo.saveTransacao(tx)
            _uiState.value = _uiState.value.copy(caixinhas = caixinhas, transacoes = listOf(tx) + state.transacoes)
        }
    }

    fun transferir(origemId: String, destinoId: String, valor: Long) {
        viewModelScope.launch {
            val state = _uiState.value
            val orig = state.caixinhas.find { it.id == origemId } ?: return@launch
            if (orig.saldoAtual < valor) { _toast.emit("Saldo insuficiente"); return@launch }
            val caixinhas = state.caixinhas.map {
                when (it.id) {
                    origemId -> it.copy(saldoAtual = it.saldoAtual - valor)
                    destinoId -> it.copy(saldoAtual = it.saldoAtual + valor)
                    else -> it
                }
            }
            val tx = Transacao(repo.generateId(), System.currentTimeMillis(), "TRANSF", valor,
                origem = orig.nome, destino = destinoId, nota = "${orig.nome} → Transferência", data = DateUtils.today())
            repo.saveCaixinhas(caixinhas)
            repo.saveTransacao(tx)
            _uiState.value = _uiState.value.copy(caixinhas = caixinhas, transacoes = listOf(tx) + state.transacoes)
            _toast.emit("Transferido!")
        }
    }

    fun iniciarJornada() {
        viewModelScope.launch {
            val jornada = Jornada(
                id = repo.generateId(),
                data = DateUtils.today(),
                inicio = System.currentTimeMillis()
            )
            repo.saveJornada(jornada)
            _uiState.value = _uiState.value.copy(jornada = jornada, lancamentos = emptyList())
            _toast.emit("Turno iniciado!")
        }
    }

    fun atualizarKM(km: Int) {
        viewModelScope.launch {
            val j = _uiState.value.jornada ?: return@launch
            val updated = j.copy(kmFinal = km)
            repo.updateJornada(updated)
            _uiState.value = _uiState.value.copy(jornada = updated)
        }
    }

    fun adicionarLancamento(tipo: String, valor: Long, descricao: String = "") {
        viewModelScope.launch {
            val j = _uiState.value.jornada ?: return@launch
            val lanc = Lancamento(tipo, valor, descricao, System.currentTimeMillis())
            val lancs = _uiState.value.lancamentos + lanc
            val updated = j.copy(lancamentos = repo.lancamentosToJson(lancs))
            repo.updateJornada(updated)

            val tipoTx = if (tipo == "ganho") "ENTRADA" else "SAIDA"
            val tx = Transacao(repo.generateId(), System.currentTimeMillis(), tipoTx, valor,
                nota = descricao.ifEmpty { tipo }, data = DateUtils.today())
            repo.saveTransacao(tx)

            _uiState.value = _uiState.value.copy(jornada = updated, lancamentos = lancs, transacoes = listOf(tx) + _uiState.value.transacoes)
        }
    }

    fun finalizarJornada() {
        viewModelScope.launch {
            val j = _uiState.value.jornada ?: return@launch
            val lucro = j.getLucro(_uiState.value.lancamentos)
            if (lucro > 0) {
                val tx = Transacao(repo.generateId(), System.currentTimeMillis(), "ENTRADA", lucro,
                    nota = "Fechamento diária", data = DateUtils.today())
                repo.saveTransacao(tx)
                val novaCarteira = _uiState.value.carteira + lucro
                repo.setConfig(Config.CARREIRA, novaCarteira.toString())
                _uiState.value = _uiState.value.copy(carteira = novaCarteira, transacoes = listOf(tx) + _uiState.value.transacoes)
            }
            val updated = j.copy(fim = System.currentTimeMillis())
            repo.updateJornada(updated)
            _uiState.value = _uiState.value.copy(jornada = updated)
            _toast.emit(if (lucro > 0) "Diária fechada! ${CentimosFormatter.format(lucro)}" else "Diária fechada!")
        }
    }

    fun cancelarJornada() {
        viewModelScope.launch {
            val j = _uiState.value.jornada ?: return@launch
            repo.deleteJornada(j.id)
            _uiState.value = _uiState.value.copy(jornada = null, lancamentos = emptyList())
            _toast.emit("Turno cancelado")
        }
    }

    fun setPeriodo(periodo: String) {
        _uiState.value = _uiState.value.copy(periodo = periodo)
    }

    fun filtrarTransacoes(): List<Transacao> {
        val state = _uiState.value
        val desde = when (state.periodo) {
            "hoje" -> DateUtils.inicioDoDia()
            "semana" -> DateUtils.inicioDaSemana()
            "mes" -> DateUtils.inicioDoMes()
            else -> 0L
        }
        return state.transacoes.filter { it.timestamp >= desde }
    }

    fun getCaixinha(id: String): Caixinha? = _uiState.value.caixinhas.find { it.id == id }

    fun exportarBackup(callback: (String) -> Unit) {
        viewModelScope.launch {
            val data = AppData(
                caixinhas = _uiState.value.caixinhas,
                transacoes = _uiState.value.transacoes,
                carteira = _uiState.value.carteira,
                extra = _uiState.value.extra,
                custoMensal = _uiState.value.custoMensal,
                emgId = _uiState.value.emgId
            )
            callback(BackupManager.exportData(data))
        }
    }

    fun importarBackup(json: String) {
        viewModelScope.launch {
            val data = BackupManager.importData(json) ?: run {
                _toast.emit("Backup inválido"); return@launch
            }
            repo.saveAllData(data)
            val survival = calcSurvival(data.caixinhas, data.emgId, data.custoMensal)
            _uiState.value = AppUiState(
                carteira = data.carteira,
                extra = data.extra,
                caixinhas = data.caixinhas,
                transacoes = data.transacoes,
                jornada = null,
                lancamentos = emptyList(),
                custoMensal = data.custoMensal,
                emgId = data.emgId,
                survivalDays = survival,
                loading = false
            )
            _toast.emit("Backup restaurado!")
        }
    }

    fun setCustoMensal(valor: Long) {
        viewModelScope.launch {
            repo.setConfig(Config.CUSTO_MENSAL, valor.toString())
            _uiState.value = _uiState.value.copy(custoMensal = valor)
        }
    }

    private fun calcSurvival(caixinhas: List<Caixinha>, emgId: String?, custo: Long): Int {
        val emg = caixinhas.find { it.id == emgId }
        val saldo = emg?.saldoAtual ?: 0L
        return if (custo > 0) (saldo / (custo / 30)).toInt() else 0
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
