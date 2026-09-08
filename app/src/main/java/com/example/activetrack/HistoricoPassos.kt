package com.example.activetrack

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cada linha desta tabela representa UM DIA de atividade.
// A chave primária é a própria data (formato "yyyy-MM-dd").
// Assim, se salvarmos duas vezes no mesmo dia, o registro é atualizado
// (não duplicado) — é isso que forma a "série temporal".

@Entity(tableName = "historico_passos")
data class HistoricoPassos(

    @PrimaryKey
    val data: String,

    val passos: Int,
    val distancia: Double,
    val calorias: Int
)