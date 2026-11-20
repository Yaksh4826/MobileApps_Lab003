package com.example.productapp.data

import java.time.LocalDate

val productCategories = listOf("Electronics", "Appliances", "Cell Phone", "Media")

// Pure validation is shared by the add and edit actions and can be unit tested.
fun validateProductInput(id: String, name: String, price: String, quantity: String,
                         date: String, category: String, requireFutureDate: Boolean = true,
                         today: LocalDate = LocalDate.now()): List<String> {
    val errors = mutableListOf<String>()
    val numericId = id.toIntOrNull()
    if (!Regex("[0-9]{3}").matches(id) || numericId == null || numericId !in 101..999)
        errors += "Product ID must be three digits from 101 to 999."
    if (name.isBlank()) errors += "Enter a product name."
    val amount = price.toDoubleOrNull()
    if (amount == null || !amount.isFinite() || amount <= 0) errors += "Price must be a positive number."
    val count = quantity.toIntOrNull()
    if (!Regex("[0-9]+").matches(quantity) || count == null || count <= 0)
        errors += "Quantity must be a whole number greater than 0."
    val parsed = try { LocalDate.parse(date) } catch (_: Exception) { null }
    if (parsed == null) errors += "Choose a delivery date."
    else if (requireFutureDate && parsed.isBefore(today)) errors += "Delivery date cannot be before today."
    if (category !in productCategories) errors += "Select a category."
    return errors
}

// Empty search shows all records. Nonempty searches match the whole product ID.
fun matchesProductId(productId: Int, query: String): Boolean =
    query.trim().isEmpty() || query.trim() == productId.toString()
