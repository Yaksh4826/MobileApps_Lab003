package com.example.productapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Exercise 1: quantity is stored with each product, including after app restart.
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: Int,
    val name: String,
    val price: Double,
    val deliveryDate: String,
    val category: String,
    val isFavorite: Boolean,
    @ColumnInfo(defaultValue = "1") val quantity: Int = 1
)
