package com.example.activetrack

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoricoDao {

    // Se já existir um registro com a mesma data, ele é SUBSTITUÍDO
    // (evita registros duplicados no mesmo dia).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(registro: HistoricoPassos)

    // Busca os últimos 7 dias, do mais recente para o mais antigo.
    @Query("SELECT * FROM historico_passos ORDER BY data DESC LIMIT 7")
    suspend fun ultimosSeteDias(): List<HistoricoPassos>
}