package com.example.mastermartini.data.repository

import com.example.mastermartini.data.local.entity.ProductEntity
import com.example.mastermartini.data.local.entity.QuoteItemWithProduct
import com.example.mastermartini.data.local.entity.UserEntity
import com.example.mastermartini.model.Product
import com.example.mastermartini.model.QuoteItem
import com.example.mastermartini.model.User

internal fun UserEntity.toModel() = User(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    photoUri = photoUri,
)

internal fun ProductEntity.toModel() = Product(
    id = id,
    name = name,
    family = family,
    category = category,
    description = description,
    presentation = presentation,
)

internal fun QuoteItemWithProduct.toModel() = QuoteItem(
    product = product.toModel(),
    quantity = quantity,
)
