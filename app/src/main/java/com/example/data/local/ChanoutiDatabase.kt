package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SpiceEntity::class,
        SpicePackEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChanoutiDatabase : RoomDatabase() {
    abstract fun chanoutiDao(): ChanoutiDao

    companion object {
        @Volatile
        private var INSTANCE: ChanoutiDatabase? = null

        fun getInstance(context: Context): ChanoutiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChanoutiDatabase::class.java,
                    "chanouti_masala.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
