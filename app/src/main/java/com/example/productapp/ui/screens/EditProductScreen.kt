package com.example.productapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.productapp.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(navController: NavController, productId: Int?,
                      viewModel: ProductViewModel = viewModel()) {
    val products by viewModel.allProducts.observeAsState() // null means the first Room result is still loading.
    val product = products?.find { it.id == productId }
    val error by viewModel.operationError.collectAsState()
    val saving by viewModel.editSaving.collectAsState()
    var confirmDelete by remember { mutableStateOf(false) }
    // Exercise 1: always go to Main, including when Edit was opened from Favorites.
    val returnHome: () -> Unit = { navController.popBackStack("home", inclusive = false); Unit }
    BackHandler(enabled = saving) { /* Wait for the current database write. */ }

    Scaffold(topBar = { TopAppBar(title = { Text("Edit Product") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (products == null) { CircularProgressIndicator(); return@Column }
            if (product == null) {
                Text("Product not found.")
                OutlinedButton(onClick = returnHome) { Text("Cancel") }
                return@Column
            }
            var name by rememberSaveable(product.id) { mutableStateOf(product.name) }
            var price by rememberSaveable(product.id) { mutableStateOf(product.price.toString()) }
            var quantity by rememberSaveable(product.id) { mutableStateOf(product.quantity.toString()) }
            var category by rememberSaveable(product.id) { mutableStateOf(product.category) }
            var favorite by rememberSaveable(product.id) { mutableStateOf(product.isFavorite) }
            Text("Product ID: ${product.id}")
            Text("Delivery date: ${product.deliveryDate}")
            OutlinedTextField(name, { name = it; viewModel.clearOperationError() },
                label = { Text("Product Name") }, enabled = !saving, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(price, { price = it; viewModel.clearOperationError() },
                label = { Text("Price") }, enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(quantity, { quantity = it; viewModel.clearOperationError() },
                label = { Text("Quantity (> 0)") }, enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            if (!saving) CategoryDropdown(category) { category = it }
            else Text("Category: $category")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Favorite")
                Switch(favorite, { favorite = it }, enabled = !saving)
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { viewModel.saveEdit(product, name, price, quantity, category, favorite, returnHome) },
                enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Saving..." else "Save") }
            OutlinedButton(onClick = returnHome, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            TextButton(onClick = { confirmDelete = true }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
            if (confirmDelete) DeleteProductDialog(product, onConfirm = {
                confirmDelete = false
                viewModel.delete(product, returnHome)
            }, onDismiss = { confirmDelete = false })
        }
    }
}
