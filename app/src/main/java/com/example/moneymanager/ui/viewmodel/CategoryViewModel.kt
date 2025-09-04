package com.example.moneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<CategoriesState>(CategoriesState.Loading)
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()

    init {
        loadAllCategories()
    }

    fun loadAllCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoriesState.Loading
            categoryRepository.getAllCategories()
                .catch { e ->
                    _categoriesState.value = CategoriesState.Error(e.message ?: "Failed to load categories")
                }
                .collectLatest { categories ->
                    _categoriesState.value = CategoriesState.Success(categories)
                }
        }
    }

    fun loadCategoriesByType(type: String) {
        viewModelScope.launch {
            _categoriesState.value = CategoriesState.Loading
            categoryRepository.getCategoriesByType(type)
                .catch { e ->
                    _categoriesState.value = CategoriesState.Error(e.message ?: "Failed to load categories")
                }
                .collectLatest { categories ->
                    _categoriesState.value = CategoriesState.Success(categories)
                }
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            _categoriesState.value = CategoriesState.Loading
            categoryRepository.addCategory(category)
                .fold(
                    onSuccess = { 
                        loadAllCategories()
                    },
                    onFailure = { error ->
                        _categoriesState.value = CategoriesState.Error(
                            error.message ?: "Failed to add category"
                        )
                    }
                )
        }
    }
    
    fun createDefaultCategories() {
        viewModelScope.launch {
            val defaultCategories = listOf(
                Category(name = "Food & Drinks", type = "expense"),
                Category(name = "Transportation", type = "expense"),
                Category(name = "Shopping", type = "expense"),
                Category(name = "Bills & Utilities", type = "expense"),
                Category(name = "Entertainment", type = "expense"),
                Category(name = "Healthcare", type = "expense"),
                Category(name = "Salary", type = "income"),
                Category(name = "Freelance", type = "income"),
                Category(name = "Investment", type = "income"),
                Category(name = "Other Income", type = "income")
            )
            
            defaultCategories.forEach { category ->
                categoryRepository.addCategory(category)
            }
            
            loadAllCategories()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId)
                .fold(
                    onSuccess = { loadAllCategories() },
                    onFailure = { /* Handle error */ }
                )
        }
    }

    sealed class CategoriesState {
        object Loading : CategoriesState()
        data class Success(val categories: List<Category>) : CategoriesState()
        data class Error(val message: String) : CategoriesState()
    }
}