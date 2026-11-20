package com.example.productapp.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.productapp.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Student: Mohammed Safique Hossain | 301485194
// Adapted from the supplied class ProductApp: Room -> repository -> ViewModel -> UI.
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(ProductDatabase.getDatabase(application).productDao())
    val allProducts: LiveData<List<Product>> = repository.products.asLiveData()
    val favoriteProducts: LiveData<List<Product>> = repository.favoriteProducts.asLiveData()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val searchResults = combine(repository.products, _searchQuery) { products, query ->
        products.filter { matchesProductId(it.id, query) }
    }.asLiveData()
    fun updateSearch(value: String) { _searchQuery.value = value }

    data class AddProductState(
        val id: String = "", val name: String = "", val price: String = "",
        val quantity: String = "", val deliveryDate: String = "",
        val category: String = "", val isFavorite: Boolean = false,
        val errors: List<String> = emptyList(), val saving: Boolean = false
    )
    private val _addProductState = MutableStateFlow(AddProductState())
    val addProductState = _addProductState.asStateFlow()
    private val _addProductSuccess = MutableStateFlow(false)
    val addProductSuccess = _addProductSuccess.asStateFlow()
    private val _operationError = MutableStateFlow<String?>(null)
    val operationError = _operationError.asStateFlow()
    private val _editSaving = MutableStateFlow(false)
    val editSaving = _editSaving.asStateFlow()
    fun clearOperationError() { _operationError.value = null }

    fun updateFormState(id: String? = null, name: String? = null, price: String? = null,
                        quantity: String? = null, deliveryDate: String? = null,
                        category: String? = null, isFavorite: Boolean? = null) {
        _addProductState.update { it.copy(id = id ?: it.id, name = name ?: it.name,
            price = price ?: it.price, quantity = quantity ?: it.quantity,
            deliveryDate = deliveryDate ?: it.deliveryDate, category = category ?: it.category,
            isFavorite = isFavorite ?: it.isFavorite, errors = emptyList()) }
    }

    fun validateAndAddProduct() {
        val state = _addProductState.value
        if (state.saving) return
        val errors = validateProductInput(state.id.trim(), state.name, state.price.trim(),
            state.quantity.trim(), state.deliveryDate, state.category)
        if (errors.isNotEmpty()) {
            _addProductState.update { it.copy(errors = errors) }; return
        }
        _addProductState.update { it.copy(saving = true, errors = emptyList()) }
        viewModelScope.launch {
            try {
                repository.addProduct(Product(state.id.trim().toInt(), state.name.trim(),
                    state.price.trim().toDouble(), state.deliveryDate, state.category,
                    state.isFavorite, state.quantity.trim().toInt()))
                _addProductSuccess.value = true // Navigate only AFTER the Room insert completes.
            } catch (error: CancellationException) { throw error
            } catch (_: SQLiteConstraintException) {
                _addProductState.update { it.copy(errors = listOf("This product ID already exists. Use another ID.")) }
            } catch (_: Exception) {
                _addProductState.update { it.copy(errors = listOf("Could not save the product. Please try again.")) }
            } finally { _addProductState.update { it.copy(saving = false) } }
        }
    }
    fun resetSuccessState() { _addProductSuccess.value = false }

    fun saveEdit(original: Product, name: String, price: String, quantity: String,
                 category: String, favorite: Boolean, onSaved: () -> Unit) {
        if (_editSaving.value) return
        val errors = validateProductInput(original.id.toString(), name, price.trim(),
            quantity.trim(), original.deliveryDate, category, requireFutureDate = false)
        if (errors.isNotEmpty()) { _operationError.value = errors.joinToString("\n"); return }
        _operationError.value = null
        _editSaving.value = true
        viewModelScope.launch {
            try {
                repository.updateProduct(original.copy(name = name.trim(), price = price.trim().toDouble(),
                    quantity = quantity.trim().toInt(), category = category, isFavorite = favorite))
                onSaved()
            } catch (error: CancellationException) { throw error
            } catch (_: Exception) { _operationError.value = "Could not update the product. Please try again."
            } finally { _editSaving.value = false }
        }
    }
    fun toggleFavorite(product: Product) = viewModelScope.launch {
        try { repository.updateProduct(product.copy(isFavorite = !product.isFavorite))
        } catch (error: CancellationException) { throw error
        } catch (_: Exception) { _operationError.value = "Could not change favorite status." }
    }
    fun delete(product: Product, onDeleted: () -> Unit = {}) = viewModelScope.launch {
        try { repository.deleteProduct(product); onDeleted()
        } catch (error: CancellationException) { throw error
        } catch (_: Exception) { _operationError.value = "Could not delete the product. Please try again." }
    }
}
