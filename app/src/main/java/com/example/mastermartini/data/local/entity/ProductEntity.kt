package com.example.mastermartini.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mastermartini.model.ProductCategory

/** Catalog product. Ids are fixed by the seed so they stay stable across installs. */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val family: String,
    val category: ProductCategory,
    val description: String,
    val presentation: String,
)
