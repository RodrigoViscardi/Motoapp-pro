package com.motoapp.pro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.motoapp.pro.model.Caixinha
import com.motoapp.pro.model.Config
import com.motoapp.pro.model.Jornada
import com.motoapp.pro.model.Transacao
import com.motoapp.pro.util.Converters

@Database(
    entities = [Caixinha::class, Transacao::class, Jornada::class, Config::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun caixinhaDao(): CaixinhaDao
    abstract fun transacaoDao(): TransacaoDao
    abstract fun jornadaDao(): JornadaDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motoapp_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
