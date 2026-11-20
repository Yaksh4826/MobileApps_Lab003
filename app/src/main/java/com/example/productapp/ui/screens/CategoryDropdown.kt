package com.example.productapp.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.productapp.data.productCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(value = selectedCategory, onValueChange = {}, readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            productCategories.forEach { category ->
                DropdownMenuItem(text = { Text(category) }, onClick = {
                    onCategorySelected(category); expanded = false
                })
            }
        }
    }
}
