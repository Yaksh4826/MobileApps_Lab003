package com.example.productapp.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.productapp.data.Product

// Shared by Home, Favorites and Edit: deletion occurs only after Yes.
@Composable
fun DeleteProductDialog(product: Product, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Delete product?") },
        text = { Text("Delete ${product.name} (ID ${product.id})? This cannot be undone.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Yes") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("No") } })
}
