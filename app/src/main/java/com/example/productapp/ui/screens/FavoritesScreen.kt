package com.example.productapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import com.example.productapp.data.Product
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.productapp.ui.viewmodel.ProductViewModel

// Displays list of favorite products
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel(),
    windowSizeClass: WindowSizeClass
) {
    var pendingDelete by remember { mutableStateOf<Product?>(null) }
    val operationError by viewModel.operationError.collectAsState()
    val favoriteProducts by viewModel.favoriteProducts.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Products") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (favoriteProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No favorites yet!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(favoriteProducts) { product ->
                    ProductItem(
                        product = product,
                        onEdit = { navController.navigate("edit/${product.id}") },
                        onDelete = { pendingDelete = product },
                        onToggleFavorite = { viewModel.toggleFavorite(product) }
                    )
                }
            }
        }
    }
    operationError?.let { message ->
        AlertDialog(onDismissRequest = viewModel::clearOperationError,
            title = { Text("Product action") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearOperationError) { Text("OK") } })
    }
    pendingDelete?.let { product ->
        DeleteProductDialog(product, onConfirm = {
            pendingDelete = null
            viewModel.delete(product)
        }, onDismiss = { pendingDelete = null })
    }

}