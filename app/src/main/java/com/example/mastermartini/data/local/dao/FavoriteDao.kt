package com.example.mastermartini.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mastermartini.data.local.entity.FavoriteEntity
import com.example.mastermartini.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun delete(userId: Long, productId: Long)

    @Query("SELECT productId FROM favorites WHERE userId = :userId")
    fun observeFavoriteIds(userId: Long): Flow<List<Long>>

    @Query(
        """
        SELECT p.* FROM products p
        INNER JOIN favorites f ON f.productId = p.id
        WHERE f.userId = :userId
        ORDER BY p.name
        """
    )
    fun observeFavoriteProducts(userId: Long): Flow<List<ProductEntity>>
}
