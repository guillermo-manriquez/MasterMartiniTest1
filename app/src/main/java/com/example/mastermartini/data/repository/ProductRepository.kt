package com.example.mastermartini.data.repository

import com.example.mastermartini.data.local.dao.FavoriteDao
import com.example.mastermartini.data.local.dao.ProductDao
import com.example.mastermartini.data.local.entity.FavoriteEntity
import com.example.mastermartini.model.Product
import com.example.mastermartini.model.ProductCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Normalizer

interface ProductRepository {
    /** Products in [category] (all if null) whose name or family contains [query]. */
    fun observeProducts(query: String, category: ProductCategory?): Flow<List<Product>>

    fun observeProduct(id: Long): Flow<Product?>

    fun observeFavoriteIds(userId: Long): Flow<Set<Long>>

    fun observeFavorites(userId: Long): Flow<List<Product>>

    suspend fun setFavorite(userId: Long, productId: Long, favorite: Boolean)
}

class RoomProductRepository(
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
) : ProductRepository {

    override fun observeProducts(query: String, category: ProductCategory?): Flow<List<Product>> {
        val needle = query.normalizedForSearch()
        return productDao.observeProducts(category).map { products ->
            products
                .filter { needle.isEmpty() || needle in it.name.normalizedForSearch() || needle in it.family.normalizedForSearch() }
                // Website order (enum order), not the alphabetical order of the stored names
                .sortedWith(compareBy({ it.category.ordinal }, { it.name }))
                .map { it.toModel() }
        }
    }

    override fun observeProduct(id: Long): Flow<Product?> =
        productDao.observeById(id).map { it?.toModel() }

    override fun observeFavoriteIds(userId: Long): Flow<Set<Long>> =
        favoriteDao.observeFavoriteIds(userId).map { it.toSet() }

    override fun observeFavorites(userId: Long): Flow<List<Product>> =
        favoriteDao.observeFavoriteProducts(userId).map { list -> list.map { it.toModel() } }

    override suspend fun setFavorite(userId: Long, productId: Long, favorite: Boolean) {
        if (favorite) {
            favoriteDao.insert(FavoriteEntity(userId, productId))
        } else {
            favoriteDao.delete(userId, productId)
        }
    }
}

private val DIACRITICS = "\\p{Mn}+".toRegex()

/** Lowercase, trimmed and without accents, so "panaderia" matches "Panadería". */
internal fun String.normalizedForSearch(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(DIACRITICS, "")
