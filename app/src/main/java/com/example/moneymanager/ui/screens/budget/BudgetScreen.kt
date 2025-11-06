package com.example.moneymanager.ui.screens.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Budget
import com.example.moneymanager.ui.viewmodel.BudgetViewModel
import com.example.moneymanager.ui.viewmodel.CategoryViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    LaunchedEffect(Unit) {
        budgetViewModel.loadBudgetsForCurrentMonth()
        categoryViewModel.loadCategoriesByType("expense")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budgets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedBudget = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        val budgetsState by budgetViewModel.budgetsState.collectAsState()
        val categoriesState by categoryViewModel.categoriesState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = budgetsState) {
                is BudgetViewModel.BudgetsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BudgetViewModel.BudgetsState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
                is BudgetViewModel.BudgetsState.Success -> {
                    val budgets = state.budgets
                    if (budgets.isEmpty()) {
                        Text("No budgets set for this month. Tap '+' to add one.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(budgets) { budget ->
                                BudgetListItem(
                                    budget = budget,
                                    onEdit = {
                                        selectedBudget = budget
                                        showDialog = true
                                    },
                                    onDelete = {
                                        budgetToDelete = budget
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddEditBudgetDialog(
                budget = selectedBudget,
                categoriesState = categoriesState,
                onDismiss = { showDialog = false },
                onConfirm = {
                    if (selectedBudget == null) {
                        budgetViewModel.addBudget(it)
                    } else {
                        budgetViewModel.updateBudget(it)
                    }
                    showDialog = false
                },
                onDelete = {
                    selectedBudget?.id?.let { budgetViewModel.deleteBudget(it) }
                    showDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Budget") },
                text = { Text("Are you sure you want to delete the budget for ${budgetToDelete?.category}? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            budgetToDelete?.id?.let { budgetViewModel.deleteBudget(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BudgetListItem(budget: Budget, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(budget.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    Text(
                        text = "${NumberFormat.getCurrencyInstance(Locale.getDefault()).format(budget.spentAmount)} / ${NumberFormat.getCurrencyInstance(Locale.getDefault()).format(budget.amount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (budget.spentAmount / budget.amount).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    budget: Budget?,
    categoriesState: CategoryViewModel.CategoriesState,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit,
    onDelete: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(budget?.category ?: "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (budget == null) "Add Budget" else "Edit Budget") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        enabled = budget == null
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false }
                    ) {
                        if (categoriesState is CategoryViewModel.CategoriesState.Success) {
                            categoriesState.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = { 
                                        selectedCategory = category.name
                                        isCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (selectedCategory.isNotBlank() && amountValue != null && amountValue > 0) {
                        val calendar = Calendar.getInstance()
                        val newBudget = budget?.copy(
                            amount = amountValue
                        ) ?: Budget(
                            category = selectedCategory,
                            amount = amountValue,
                            month = calendar.get(Calendar.MONTH) + 1,
                            year = calendar.get(Calendar.YEAR)
                        )
                        onConfirm(newBudget)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = if (budget != null) {
            Modifier.padding(bottom = 56.dp) 
        } else Modifier
    )

    if (budget != null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}