package com.example.mastermartini.model

import androidx.annotation.StringRes
import com.example.mastermartini.R

/** The four business sections of mastermartini.cl. Stored by name in Room. */
enum class ProductCategory(@StringRes val labelRes: Int) {
    PANADERIA_PASTELERIA(R.string.category_bakery_pastry),
    GELATERIA(R.string.category_gelato),
    FOOD_SERVICE(R.string.category_food_service),
    RETAIL(R.string.category_retail),
}
