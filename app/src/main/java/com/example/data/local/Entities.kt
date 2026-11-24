package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "spices")
data class SpiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameHi: String,
    val category: String,
    val sortOrder: Int
)

@Entity(
    tableName = "spice_packs",
    foreignKeys = [
        ForeignKey(
            entity = SpiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["spiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["spiceId"]),
        Index(value = ["spiceId", "packGrams"], unique = true)
    ]
)
data class SpicePackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spiceId: Long,
    val packGrams: Int,
    val stockPackets: Int,
    val costPrice: Double,
    val sellingPrice: Double
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val totalAmount: Double,
    val totalCost: Double,
    val estimatedProfit: Double,
    val paidAmount: Double,
    val paymentStatus: String,
    val createdAtMillis: Long
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val spiceId: Long,
    val spiceNameEn: String,
    val spiceNameHi: String,
    val packGrams: Int,
    val quantityPackets: Int,
    val unitCostPrice: Double,
    val unitSellingPrice: Double,
    val lineTotal: Double,
    val lineProfit: Double
)

@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val amountReceived: Double,
    val note: String,
    val paidAtMillis: Long
)

data class SpiceWithPacksRelation(
    @Embedded val spice: SpiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "spiceId"
    )
    val packs: List<SpicePackEntity>
)

data class SaleWithDetailsRelation(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val payments: List<PaymentRecordEntity>
)
