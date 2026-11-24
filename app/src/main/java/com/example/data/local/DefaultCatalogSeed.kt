package com.example.data.local

import com.example.data.model.PackSize
import com.example.data.model.SpiceCategory

data class SeedPackConfig(
    val packSize: PackSize,
    val stockPackets: Int,
    val costPrice: Double,
    val sellingPrice: Double
)

data class SeedSpiceConfig(
    val nameEn: String,
    val nameHi: String,
    val category: SpiceCategory,
    val sortOrder: Int,
    val packs: List<SeedPackConfig>
)

object DefaultCatalogSeed {
    fun buildDefaultSpices(): List<SeedSpiceConfig> = listOf(
        SeedSpiceConfig(
            nameEn = "Haldi",
            nameHi = "हल्दी",
            category = SpiceCategory.BASIC,
            sortOrder = 1,
            packs = standardPacks(
                c10 = 3.0, s10 = 5.0, q10 = 50,
                c50 = 13.0, s50 = 20.0, q50 = 8,
                c100 = 25.0, s100 = 36.0, q100 = 40,
                c200 = 48.0, s200 = 68.0, q200 = 24
            )
        ),
        SeedSpiceConfig(
            nameEn = "Mirch",
            nameHi = "मिर्च",
            category = SpiceCategory.BASIC,
            sortOrder = 2,
            packs = standardPacks(
                c10 = 3.5, s10 = 5.0, q10 = 45,
                c50 = 16.0, s50 = 24.0, q50 = 30,
                c100 = 31.0, s100 = 44.0, q100 = 32,
                c200 = 60.0, s200 = 84.0, q200 = 18
            )
        ),
        SeedSpiceConfig(
            nameEn = "Dhaniya",
            nameHi = "धनिया",
            category = SpiceCategory.BASIC,
            sortOrder = 3,
            packs = standardPacks(
                c10 = 3.0, s10 = 5.0, q10 = 40,
                c50 = 12.0, s50 = 18.0, q50 = 28,
                c100 = 22.0, s100 = 32.0, q100 = 35,
                c200 = 42.0, s200 = 60.0, q200 = 20
            )
        ),
        SeedSpiceConfig(
            nameEn = "Sabji Masala",
            nameHi = "सब्जी मसाला",
            category = SpiceCategory.BLENDED,
            sortOrder = 4,
            packs = standardPacks(
                c10 = 4.0, s10 = 6.0, q10 = 60,
                c50 = 18.0, s50 = 26.0, q50 = 34,
                c100 = 34.0, s100 = 48.0, q100 = 26,
                c200 = 65.0, s200 = 90.0, q200 = 15
            )
        ),
        SeedSpiceConfig(
            nameEn = "Garam Masala",
            nameHi = "गरम मसाला",
            category = SpiceCategory.BLENDED,
            sortOrder = 5,
            packs = standardPacks(
                c10 = 5.0, s10 = 8.0, q10 = 42,
                c50 = 24.0, s50 = 34.0, q50 = 22,
                c100 = 46.0, s100 = 64.0, q100 = 6,
                c200 = 88.0, s200 = 120.0, q200 = 14
            )
        ),
        SeedSpiceConfig(
            nameEn = "Jeera Powder",
            nameHi = "जीरा पाउडर",
            category = SpiceCategory.BASIC,
            sortOrder = 6,
            packs = standardPacks(
                c10 = 5.0, s10 = 8.0, q10 = 30,
                c50 = 22.0, s50 = 32.0, q50 = 25,
                c100 = 42.0, s100 = 58.0, q100 = 20,
                c200 = 80.0, s200 = 110.0, q200 = 12
            )
        ),
        SeedSpiceConfig(
            nameEn = "Bhuna Jeera Powder",
            nameHi = "भुना जीरा पाउडर",
            category = SpiceCategory.SPECIAL,
            sortOrder = 7,
            packs = standardPacks(
                c10 = 6.0, s10 = 9.0, q10 = 28,
                c50 = 26.0, s50 = 38.0, q50 = 18,
                c100 = 50.0, s100 = 70.0, q100 = 16,
                c200 = 96.0, s200 = 132.0, q200 = 7
            )
        ),
        SeedSpiceConfig(
            nameEn = "Takhauri Mirch",
            nameHi = "तखौरी मिर्च",
            category = SpiceCategory.SPECIAL,
            sortOrder = 8,
            packs = standardPacks(
                c10 = 4.5, s10 = 7.0, q10 = 32,
                c50 = 20.0, s50 = 28.0, q50 = 24,
                c100 = 38.0, s100 = 54.0, q100 = 22,
                c200 = 74.0, s200 = 102.0, q200 = 14
            )
        ),
        SeedSpiceConfig(
            nameEn = "Bhuna Jeera",
            nameHi = "भुना जीरा",
            category = SpiceCategory.WHOLE_SPECIAL,
            sortOrder = 9,
            packs = standardPacks(
                c10 = 6.0, s10 = 9.0, q10 = 24,
                c50 = 28.0, s50 = 40.0, q50 = 18,
                c100 = 54.0, s100 = 74.0, q100 = 15,
                c200 = 104.0, s200 = 140.0, q200 = 10
            )
        ),
        SeedSpiceConfig(
            nameEn = "Meat Masala",
            nameHi = "मीट मसाला",
            category = SpiceCategory.BLENDED,
            sortOrder = 10,
            packs = standardPacks(
                c10 = 5.0, s10 = 8.0, q10 = 38,
                c50 = 22.0, s50 = 32.0, q50 = 26,
                c100 = 42.0, s100 = 60.0, q100 = 22,
                c200 = 82.0, s200 = 114.0, q200 = 14
            )
        ),
        SeedSpiceConfig(
            nameEn = "Chicken Masala",
            nameHi = "चिकन मसाला",
            category = SpiceCategory.BLENDED,
            sortOrder = 11,
            packs = standardPacks(
                c10 = 5.0, s10 = 8.0, q10 = 36,
                c50 = 22.0, s50 = 32.0, q50 = 28,
                c100 = 42.0, s100 = 60.0, q100 = 24,
                c200 = 82.0, s200 = 114.0, q200 = 12
            )
        ),
        SeedSpiceConfig(
            nameEn = "Chhole Masala",
            nameHi = "छोले मसाला",
            category = SpiceCategory.BLENDED,
            sortOrder = 12,
            packs = standardPacks(
                c10 = 4.5, s10 = 7.0, q10 = 30,
                c50 = 20.0, s50 = 29.0, q50 = 20,
                c100 = 38.0, s100 = 54.0, q100 = 18,
                c200 = 74.0, s200 = 104.0, q200 = 12
            )
        ),
        SeedSpiceConfig(
            nameEn = "Custom / Extra Masala",
            nameHi = "अन्य मसाला",
            category = SpiceCategory.OTHER,
            sortOrder = 13,
            packs = standardPacks(
                c10 = 4.0, s10 = 6.0, q10 = 20,
                c50 = 18.0, s50 = 26.0, q50 = 15,
                c100 = 34.0, s100 = 48.0, q100 = 15,
                c200 = 66.0, s200 = 92.0, q200 = 10
            )
        )
    )

    private fun standardPacks(
        c10: Double, s10: Double, q10: Int,
        c50: Double, s50: Double, q50: Int,
        c100: Double, s100: Double, q100: Int,
        c200: Double, s200: Double, q200: Int
    ): List<SeedPackConfig> = listOf(
        SeedPackConfig(PackSize.G10, q10, c10, s10),
        SeedPackConfig(PackSize.G50, q50, c50, s50),
        SeedPackConfig(PackSize.G100, q100, c100, s100),
        SeedPackConfig(PackSize.G200, q200, c200, s200)
    )
}
