package com.example.mastermartini.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mastermartini.data.local.entity.ProductEntity
import com.example.mastermartini.model.ProductCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    /**
     * Catalog list, optionally limited to one category. Text search is done in
     * ProductRepository because SQLite LIKE is not accent-insensitive.
     */
    @Query(
        """
        SELECT * FROM products
        WHERE (:category IS NULL OR category = :category)
        ORDER BY category, name
        """
    )
    fun observeProducts(category: ProductCategory?): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<ProductEntity?>
}
