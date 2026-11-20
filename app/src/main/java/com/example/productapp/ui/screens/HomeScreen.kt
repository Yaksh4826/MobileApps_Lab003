package com.example.productapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.productapp.data.Product
import com.example.productapp.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, windowSizeClass: WindowSizeClass,
               viewModel: ProductViewModel = viewModel()) {
    val products by viewModel.searchResults.observeAsState(emptyList())
    val query by viewModel.searchQuery.collectAsState()
    val error by viewModel.operationError.collectAsState()
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingDelete by remember { mutableStateOf<Product?>(null) }
    val expanded = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val selectedProduct = products.find { it.id == selectedId }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Product List") }, actions = {
            IconButton(onClick = { navController.navigate("favorites") }) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorite products")
            }
            IconButton(onClick = { navController.navigate("add") }) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Exercise 1: exact ID search; clearing it restores the complete list.
            OutlinedTextField(value = query, onValueChange = viewModel::updateSearch,
                label = { Text("Search by Product ID") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { viewModel.updateSearch("") }) { Text("Clear") } },
                supportingText = { Text("Enter a complete ID, such as 101. Clear to show all products.") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            error?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            if (products.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (query.isBlank()) "No products yet. Tap + to add one." else "No product found for ID ${query.trim()}.")
                }
            } else Row(Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(Modifier.weight(1f).fillMaxHeight(), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(products, key = { it.id }) { product ->
                        ProductItem(product, onEdit = { navController.navigate("edit/${product.id}") },
                            onDelete = { pendingDelete = product },
                            onToggleFavorite = { viewModel.toggleFavorite(product) },
                            modifier = Modifier.clickable { selectedId = product.id })
                    }
                }
                if (expanded) Box(Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
                    selectedProduct?.let { product ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(product.name, style = MaterialTheme.typography.headlineMedium)
                            Text("ID: ${product.id}")
                            Text("Price: $${product.price}")
                            Text("Quantity: ${product.quantity}")
                            Text("Category: ${product.category}")
                            Text("Delivery: ${product.deliveryDate}")
                        }
                    } ?: Text("Select a product to view its details.")
                }
            }
        }
    }
    pendingDelete?.let { product ->
        DeleteProductDialog(product, onConfirm = {
            pendingDelete = null
            viewModel.delete(product)
        }, onDismiss = { pendingDelete = null })
    }
}
