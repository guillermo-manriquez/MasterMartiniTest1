package com.example.mastermartini.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** A submitted quote request ("Solicitud de cotización registrada"). Stored locally only. */
@Entity(
    tableName = "quote_requests",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class QuoteRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val businessName: String,
    val rut: String,
    val deliveryDate: LocalDate,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long,
)
