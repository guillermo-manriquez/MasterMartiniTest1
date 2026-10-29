package com.example.mastermartini.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room type converters. Enums such as ProductCategory are stored by name without one. */
class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
