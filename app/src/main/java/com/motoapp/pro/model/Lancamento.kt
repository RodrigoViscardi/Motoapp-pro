package com.motoapp.pro.model

data class Lancamento(
    val tipo: String,
    val valor: Long,
    val descricao: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
