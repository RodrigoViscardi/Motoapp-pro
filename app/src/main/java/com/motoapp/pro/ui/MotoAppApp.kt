package com.motoapp.pro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.ui.components.*
import com.motoapp.pro.ui.screens.*
import com.motoapp.pro.ui.theme.*
import com.motoapp.pro.util.CentimosFormatter
import com.motoapp.pro.viewmodel.AppViewModel
import kotlinx.coroutines.launch

data class Tab(val label: String, val icon: String)

private val tabs = listOf(
    Tab("Painel", "📊"),
    Tab("Caixas", "📦"),
    Tab("Lançar", "⚡"),
    Tab("Diária", "📋"),
    Tab("Histórico", "📜"),
    Tab("Relatórios", "📈"),
    Tab("Urgência", "🚨")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotoAppApp(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Toast handling
    LaunchedEffect(Unit) {
        viewModel.toast.collect { msg ->
            scope.launch {
                // Actually we handle this via a shared state approach
            }
        }
    }

    var showIncomeModal by remember { mutableStateOf(false) }
    var incomeValue by remember { mutableStateOf("") }
    var showAddCaixinha by remember { mutableStateOf(false) }
    var addCxNome by remember { mutableStateOf("") }
    var addCxValor by remember { mutableStateOf("") }
    var addCxPrioridade by remember { mutableStateOf("") }
    var addCxVencimento by remember { mutableStateOf("") }
    var addCxCor by remember { mutableStateOf("#58A6FF") }

    var showTransferModal by remember { mutableStateOf(false) }
    var transferOrigemId by remember { mutableStateOf("") }
    var transferValor by remember { mutableStateOf("") }

    var showCaixinhaDetail by remember { mutableStateOf<String?>(null) }
    var detailCx by remember { mutableStateOf<Caixinha?>(null) }

    // Toast
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.toastMessage) {
        toastMessage = state.toastMessage
        if (state.toastMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                BottomNavBar(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabClick = { selectedTab = it }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Wallet card header (hidden on Lancar to save space)
                if (selectedTab != 2) {
                    WalletCard(
                        carteira = state.carteira,
                        extra = state.extra,
                        caixinhaCount = state.caixinhas.size,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Screen content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> PainelScreen(
                            carteira = state.carteira,
                            caixinhas = state.caixinhas,
                            transacoes = viewModel.filtrarTransacoes(),
                            jornadaAtiva = state.jornada?.ativa == true,
                            onDistribuir = viewModel::distribuirCascata,
                            onCaixinhaClick = { id ->
                                viewModel.getCaixinha(id)?.let { detailCx = it }
                                showCaixinhaDetail = id
                            },
                            onAddIncome = { showIncomeModal = true }
                        )
                        1 -> CaixasScreen(
                            caixinhas = state.caixinhas,
                            onReorder = viewModel::reordenarCaixinhas,
                            onClick = { id ->
                                viewModel.getCaixinha(id)?.let { detailCx = it }
                                showCaixinhaDetail = id
                            },
                            onAdd = { showAddCaixinha = true },
                            onTransfer = { id ->
                                transferOrigemId = id
                                showTransferModal = true
                            }
                        )
                        2 -> LancarScreen(
                            jornada = state.jornada,
                            lancamentos = state.lancamentos,
                            transacoes = state.transacoes,
                            getCaixinhaNome = { id -> state.caixinhas.find { it.id == id }?.nome },
                            onStartShift = viewModel::iniciarJornada,
                            onEndShift = viewModel::finalizarJornada,
                            onUpdateKM = viewModel::atualizarKM,
                            onLaunch = { tipo ->
                                val valor = when (tipo) {
                                    "ganho" -> 5000L
                                    "gasto" -> 2000L
                                    "combustivel" -> 5000L
                                    else -> 0L
                                }
                                viewModel.adicionarLancamento(tipo, valor)
                            }
                        )
                        3 -> DiariaScreen(
                            jornada = state.jornada,
                            lancamentos = state.lancamentos,
                            onEndShift = viewModel::finalizarJornada,
                            onUpdateKM = viewModel::atualizarKM
                        )
                        4 -> HistoricoScreen(
                            transacoes = viewModel.filtrarTransacoes(),
                            getCaixinhaNome = { id -> state.caixinhas.find { it.id == id }?.nome },
                            onSetPeriodo = viewModel::setPeriodo,
                            periodo = state.periodo
                        )
                        5 -> RelatoriosScreen(
                            transacoes = state.transacoes,
                            caixinhas = state.caixinhas,
                            onSetPeriodo = viewModel::setPeriodo,
                            periodo = state.periodo
                        )
                        6 -> UrgenciaScreen(
                            caixinhas = state.caixinhas,
                            transacoes = state.transacoes,
                            carteira = state.carteira
                        )
                    }
                }
            }
        }

        // Modals
        IncomeModal(
            visible = showIncomeModal,
            value = incomeValue,
            onValueChange = { incomeValue = it },
            onConfirm = {
                val v = CentimosFormatter.parse(incomeValue)
                if (v > 0) {
                    viewModel.adicionarGanho(v)
                    incomeValue = ""
                    showIncomeModal = false
                }
            },
            onDismiss = {
                showIncomeModal = false
                incomeValue = ""
            }
        )

        AddCaixinhaModal(
            visible = showAddCaixinha,
            nome = addCxNome,
            onNomeChange = { addCxNome = it },
            valor = addCxValor,
            onValorChange = { addCxValor = it },
            prioridade = addCxPrioridade,
            onPrioridadeChange = { addCxPrioridade = it },
            vencimento = addCxVencimento,
            onVencimentoChange = { addCxVencimento = it },
            cor = addCxCor,
            onCorChange = { addCxCor = it },
            onConfirm = {
                val v = CentimosFormatter.parse(addCxValor)
                val p = addCxPrioridade.toIntOrNull() ?: (state.caixinhas.size + 1)
                if (addCxNome.isNotBlank() && v > 0) {
                    viewModel.addCaixinha(addCxNome, v, p, addCxVencimento.ifBlank { null }, addCxCor)
                    addCxNome = ""
                    addCxValor = ""
                    addCxPrioridade = ""
                    addCxVencimento = ""
                    addCxCor = "#58A6FF"
                    showAddCaixinha = false
                }
            },
            onDismiss = {
                showAddCaixinha = false
                addCxNome = ""
                addCxValor = ""
                addCxPrioridade = ""
                addCxVencimento = ""
                addCxCor = "#58A6FF"
            }
        )

        TransferModal(
            visible = showTransferModal,
            origemId = transferOrigemId,
            origens = state.caixinhas,
            valor = transferValor,
            onValorChange = { transferValor = it },
            onConfirm = { destId ->
                val v = CentimosFormatter.parse(transferValor)
                if (v > 0 && destId != null) {
                    viewModel.transferir(transferOrigemId, destId, v)
                    transferValor = ""
                    showTransferModal = false
                }
            },
            onDismiss = {
                showTransferModal = false
                transferValor = ""
            }
        )

        // Caixinha detail modal
        CaixinhaDetailModal(
            caixinha = detailCx,
            visible = showCaixinhaDetail != null && detailCx != null,
            onDepositar = { id, valor -> viewModel.depositar(id, valor) },
            onPagar = { id, valor -> viewModel.pagar(id, valor) },
            onDelete = { id -> viewModel.deleteCaixinha(id) },
            onDismiss = {
                showCaixinhaDetail = null
                detailCx = null
            }
        )

        // Toast message
        if (toastMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariant.copy(alpha = 0.95f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        toastMessage!!,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    tabs: List<Tab>,
    selectedTab: Int,
    onTabClick: (Int) -> Unit
) {
    Surface(
        color = Surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedTab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SurfaceVariant else Surface)
                        .clickable { onTabClick(index) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tab.icon, fontSize = 16.sp)
                    Text(
                        tab.label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- Modals ---

@Composable
private fun IncomeModal(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = "Adicionar Ganho"
    ) {
        NumericKeypad(value = value, onValueChange = onValueChange)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancelar", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary)
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Adicionar", color = Background, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AddCaixinhaModal(
    visible: Boolean,
    nome: String,
    onNomeChange: (String) -> Unit,
    valor: String,
    onValorChange: (String) -> Unit,
    prioridade: String,
    onPrioridadeChange: (String) -> Unit,
    vencimento: String,
    onVencimentoChange: (String) -> Unit,
    cor: String,
    onCorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cores = listOf("#58A6FF", "#3FB950", "#D29922", "#F85149", "#8957E5", "#F778BA", "#79C0FF")

    ModalSheet(visible = visible, onDismiss = onDismiss, title = "Nova Caixinha") {
        Column {
            InputField("Nome", nome, onNomeChange, "Ex: Gasolina")
            Spacer(Modifier.height(8.dp))
            InputField("Valor total (R$)", valor, onValorChange, "Ex: 40000")
            Spacer(Modifier.height(8.dp))
            InputField("Prioridade", prioridade, onPrioridadeChange, "Ex: 1")
            Spacer(Modifier.height(8.dp))
            InputField("Vencimento (opcional)", vencimento, onVencimentoChange, "DD/MM")

            Spacer(Modifier.height(10.dp))
            Text("Cor", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cores.forEach { c ->
                    val isSelected = c == cor
                    val corColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(c))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(corColor)
                            .then(
                                if (isSelected) Modifier
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Background)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(corColor)
                                else Modifier
                            )
                            .clickable { onCorChange(c) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariant)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancelar", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Criar", color = Background, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(placeholder, color = TextMuted, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun TransferModal(
    visible: Boolean,
    origemId: String,
    origens: List<Caixinha>,
    valor: String,
    onValorChange: (String) -> Unit,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDestId by remember { mutableStateOf<String?>(null) }
    val origem = origens.find { it.id == origemId }

    ModalSheet(visible = visible, onDismiss = onDismiss, title = "Transferir de: ${origem?.nome ?: "?"}") {
        NumericKeypad(value = valor, onValueChange = onValorChange)
        Spacer(Modifier.height(12.dp))
        Text("Para:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        val destinos = origens.filter { it.id != origemId }
        destinos.forEach { cx ->
            val isSelected = cx.id == selectedDestId
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryVariant.copy(alpha = 0.2f) else SurfaceVariant)
                    .clickable { selectedDestId = if (isSelected) null else cx.id }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cx.nome, color = TextPrimary, fontSize = 13.sp)
                    Text(
                        CentimosFormatter.format(cx.saldoAtual),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancelar", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedDestId != null) Primary else SurfaceVariant)
                    .clickable(enabled = selectedDestId != null) { onConfirm(selectedDestId) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Transferir", color = if (selectedDestId != null) Background else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CaixinhaDetailModal(
    caixinha: Caixinha?,
    visible: Boolean,
    onDepositar: (String, Long) -> Unit,
    onPagar: (String, Long) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (caixinha == null || !visible) return

    var showDeposit by remember { mutableStateOf(false) }
    var depositValue by remember { mutableStateOf("") }
    var showPay by remember { mutableStateOf(false) }
    var payValue by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf("") }

    ModalSheet(visible = visible, onDismiss = onDismiss, title = caixinha.nome) {
        Text(
            "${CentimosFormatter.format(caixinha.saldoAtual)} / ${CentimosFormatter.format(caixinha.valorTotal)}",
            color = TextSecondary,
            fontSize = 14.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        if (caixinha.vencimento != null) {
            Text("Vencimento: ${caixinha.vencimento}", color = TextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusGreenBg)
                    .clickable { showDeposit = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Depositar", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusRedBg)
                    .clickable { showPay = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Pagar", color = StatusRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusRedBg)
                    .clickable { showDeleteConfirm = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Excluir", color = StatusRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showDeposit) {
            Spacer(Modifier.height(12.dp))
            NumericKeypad(value = depositValue, onValueChange = { depositValue = it })
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary)
                    .clickable {
                        val v = CentimosFormatter.parse(depositValue)
                        if (v > 0) {
                            onDepositar(caixinha.id, v)
                            depositValue = ""
                            showDeposit = false
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Confirmar Depósito", color = Background, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showPay) {
            Spacer(Modifier.height(12.dp))
            NumericKeypad(value = payValue, onValueChange = { payValue = it })
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Error)
                    .clickable {
                        val v = CentimosFormatter.parse(payValue)
                        if (v > 0) {
                            onPagar(caixinha.id, v)
                            payValue = ""
                            showPay = false
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Confirmar Pagamento", color = Background, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        ConfirmDialog(
            visible = showDeleteConfirm,
            title = "Excluir ${caixinha.nome}?",
            message = "O saldo atual (${CentimosFormatter.format(caixinha.saldoAtual)}) será devolvido à carteira.",
            confirmText = "Excluir",
            destructive = true,
            onConfirm = {
                onDelete(caixinha.id)
                showDeleteConfirm = false
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
