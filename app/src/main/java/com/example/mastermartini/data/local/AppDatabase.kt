package com.example.mastermartini.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mastermartini.data.local.dao.FavoriteDao
import com.example.mastermartini.data.local.dao.ProductDao
import com.example.mastermartini.data.local.dao.QuoteDao
import com.example.mastermartini.data.local.dao.UserDao
import com.example.mastermartini.data.local.entity.FavoriteEntity
import com.example.mastermartini.data.local.entity.ProductEntity
import com.example.mastermartini.data.local.entity.QuoteItemEntity
import com.example.mastermartini.data.local.entity.QuoteRequestEntity
import com.example.mastermartini.data.local.entity.QuoteRequestItemEntity
import com.example.mastermartini.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        FavoriteEntity::class,
        QuoteItemEntity::class,
        QuoteRequestEntity::class,
        QuoteRequestItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        private const val DATABASE_NAME = "master_martini.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(SeedCallback)
                .build()
    }

    /**
     * Inserts the catalog the first time the database file is created. It runs inside
     * Room's open transaction, so the products exist before the first query returns.
     */
    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            SeedData.products.forEach { product ->
                db.insert("products", SQLiteDatabase.CONFLICT_IGNORE, product.toContentValues())
            }
        }

        private fun ProductEntity.toContentValues() = ContentValues().apply {
            put("id", id)
            put("name", name)
            put("family", family)
            put("category", category.name)
            put("description", description)
            put("presentation", presentation)
        }
    }
}
