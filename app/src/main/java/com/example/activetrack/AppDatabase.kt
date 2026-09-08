package com.example.activetrack

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HistoricoPassos::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historicoDao(): HistoricoDao

    companion object {

        // Garante que exista apenas UMA instância do banco no app inteiro
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {

            return instancia ?: synchronized(this) {

                val novaInstancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pedometer_db"
                ).build()

                instancia = novaInstancia

                novaInstancia
            }
        }
    }
}