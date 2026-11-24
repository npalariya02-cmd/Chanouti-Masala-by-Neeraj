package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChanoutiDao {

    @Transaction
    @Query("SELECT * FROM spices ORDER BY sortOrder ASC, id ASC")
    fun observeSpicesWithPacks(): Flow<List<SpiceWithPacksRelation>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAtMillis DESC, id DESC")
    fun observeSalesWithDetails(): Flow<List<SaleWithDetailsRelation>>

    @Query("SELECT DISTINCT customerName FROM sales WHERE customerName != 'Cash Customer' ORDER BY createdAtMillis DESC LIMIT 20")
    fun observeRecentCustomers(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM spices")
    suspend fun countSpices(): Int

    @Query("SELECT MAX(sortOrder) FROM spices")
    suspend fun getMaxSortOrder(): Int?

    @Query("SELECT * FROM spice_packs WHERE spiceId = :spiceId AND packGrams = :packGrams LIMIT 1")
    suspend fun getPack(spiceId: Long, packGrams: Int): SpicePackEntity?

    @Query("SELECT * FROM sales WHERE id = :saleId LIMIT 1")
    suspend fun getSaleById(saleId: Long): SaleEntity?

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun countSales(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpice(spice: SpiceEntity): Long

    @Update
    suspend fun updateSpice(spice: SpiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacks(packs: List<SpicePackEntity>)

    @Update
    suspend fun updatePack(pack: SpicePackEntity)

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Insert
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert
    suspend fun insertPaymentRecord(record: PaymentRecordEntity): Long

    @Query("DELETE FROM payment_records")
    suspend fun clearPaymentRecords()

    @Query("DELETE FROM sale_items")
    suspend fun clearSaleItems()

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM spice_packs")
    suspend fun clearSpicePacks()

    @Query("DELETE FROM spices")
    suspend fun clearSpices()

    @Transaction
    suspend fun seedCatalogIfEmpty() {
        if (countSpices() > 0) return
        insertSeedCatalog()
    }

    @Transaction
    suspend fun resetAllAndReseed() {
        clearPaymentRecords()
        clearSaleItems()
        clearSales()
        clearSpicePacks()
        clearSpices()
        insertSeedCatalog()
    }

    suspend fun insertSeedCatalog() {
        val defaults = DefaultCatalogSeed.buildDefaultSpices()
        for (seed in defaults) {
            val spiceId = insertSpice(
                SpiceEntity(
                    nameEn = seed.nameEn,
                    nameHi = seed.nameHi,
                    category = seed.category.labelEn,
                    sortOrder = seed.sortOrder
                )
            )
            val packEntities = seed.packs.map { p ->
                SpicePackEntity(
                    spiceId = spiceId,
                    packGrams = p.packSize.grams,
                    stockPackets = p.stockPackets,
                    costPrice = p.costPrice,
                    sellingPrice = p.sellingPrice
                )
            }
            insertPacks(packEntities)
        }
    }
}
