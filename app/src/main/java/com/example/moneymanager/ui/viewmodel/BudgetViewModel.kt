package com.example.moneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.data.repository.BudgetRepository
import com.example.moneymanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _budgetsState = MutableStateFlow<BudgetsState>(BudgetsState.Loading)
    val budgetsState: StateFlow<BudgetsState> = _budgetsState.asStateFlow()

    sealed class BudgetsState {
        object Loading : BudgetsState()
        data class Success(val budgets: List<Budget>) : BudgetsState()
        data class Error(val message: String) : BudgetsState()
    }

    fun loadBudgetsForCurrentMonth() {
        viewModelScope.launch {
            _budgetsState.value = BudgetsState.Loading
            try {
                val calendar = Calendar.getInstance()
                val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
                val year = calendar.get(Calendar.YEAR)

                val budgets = budgetRepository.getBudgetsForMonth(month, year)
                val transactions = transactionRepository.getAllTransactions().first()

                // Calculate spent amount for each budget
                val updatedBudgets = budgets.map { budget ->
                    val spentAmount = transactions
                        .filter { it.category == budget.category && it.type == "expense" }
                        .sumOf { it.amount }
                    budget.copy(spentAmount = spentAmount)
                }

                _budgetsState.value = BudgetsState.Success(updatedBudgets)
            } catch (e: Exception) {
                _budgetsState.value = BudgetsState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.addBudget(budget)
                loadBudgetsForCurrentMonth()
            } catch (e: Exception) {
                _budgetsState.value = BudgetsState.Error(e.message ?: "Failed to add budget")
            }
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.updateBudget(budget)
                loadBudgetsForCurrentMonth()
            } catch (e: Exception) {
                _budgetsState.value = BudgetsState.Error(e.message ?: "Failed to update budget")
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budgetId)
                loadBudgetsForCurrentMonth()
            } catch (e: Exception) {
                _budgetsState.value = BudgetsState.Error(e.message ?: "Failed to delete budget")
            }
        }
    }
}