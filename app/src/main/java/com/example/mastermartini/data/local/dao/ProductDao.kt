package com.example.mastermartini.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mastermartini.data.local.entity.ProductEntity
import com.example.mastermartini.model.ProductCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    /**
     * Catalog list filtered by an optional category and a search text that
     * matches the product name or family. An empty [query] matches everything.
     */
    @Query(
        """
        SELECT * FROM products
        WHERE (:category IS NULL OR category = :category)
          AND (name LIKE '%' || :query || '%' OR family LIKE '%' || :query || '%')
        ORDER BY category, name
        """
    )
    fun observeProducts(query: String, category: ProductCategory?): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<ProductEntity?>
}
