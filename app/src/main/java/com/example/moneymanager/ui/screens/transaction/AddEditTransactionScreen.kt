package com.example.moneymanager.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.ui.viewmodel.CategoryViewModel
import com.example.moneymanager.ui.viewmodel.TransactionViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: String? = null,
    onNavigateBack: () -> Unit,
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Transaction state
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("expense") } // Default to expense
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    
    // Category dropdown state
    var isExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    
    // Get categories based on transaction type
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val transaction by transactionViewModel.currentTransaction.collectAsState()
    val transactionsState by transactionViewModel.transactionsState.collectAsState()
    
    // Error handling
    LaunchedEffect(transactionsState) {
        when (val state = transactionsState) {
            is TransactionViewModel.TransactionsState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }
    
    // Load categories when transaction type changes
    LaunchedEffect(transactionType) {
        categoryViewModel.loadCategoriesByType(transactionType)
    }
    
    // Check if categories are empty and create defaults
    LaunchedEffect(categoriesState) {
        if (categoriesState is CategoryViewModel.CategoriesState.Success) {
            val categories = (categoriesState as CategoryViewModel.CategoriesState.Success).categories
            if (categories.isEmpty()) {
                categoryViewModel.createDefaultCategories()
            }
        }
    }
    
    // Load transaction if editing
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            transactionViewModel.getTransactionById(transactionId)
        }
    }
    
    // Update fields if editing an existing transaction
    LaunchedEffect(transaction) {
        transaction?.let {
            amount = it.amount.toString()
            description = it.description
            transactionType = it.type
            selectedCategory = it.category
            selectedDate = it.date.toDate()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId == null) "Add Transaction" else "Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selection
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = transactionType == "expense",
                    onClick = { transactionType = "expense" }
                )
                Text("Expense")
                
                Spacer(modifier = Modifier.width(16.dp))
                
                RadioButton(
                    selected = transactionType == "income",
                    onClick = { transactionType = "income" }
                )
                Text("Income")
            }
            
            // Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            
            // Category Dropdown
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium
            )
            
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Select Category") }
                )
                
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    when (categoriesState) {
                        is CategoryViewModel.CategoriesState.Loading -> {
                            DropdownMenuItem(
                                text = { Text("Loading...") },
                                onClick = {},
                                trailingIcon = { CircularProgressIndicator(modifier = Modifier.height(24.dp)) }
                            )
                        }
                        is CategoryViewModel.CategoriesState.Error -> {
                            DropdownMenuItem(
                                text = { Text("Error loading categories") },
                                onClick = {}
                            )
                        }
                        is CategoryViewModel.CategoriesState.Success -> {
                            val categories = (categoriesState as CategoryViewModel.CategoriesState.Success).categories
                            if (categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No categories available") },
                                    onClick = {}
                                )
                                DropdownMenuItem(
                                    text = { Text("Create categories first") },
                                    onClick = { /* Could navigate to categories screen */ }
                                )
                            } else {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category.name
                                            isExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 3
            )
            
            // Date Picker (simplified for now)
            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
                // In a real app, clicking this would open a date picker dialog
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            // Show error for invalid amount
                            return@Button
                        }
                        
                        // Use "Other" as default category if none selected
                        val categoryName = if (selectedCategory.isBlank()) "Other" else selectedCategory
                        
                        val transaction = Transaction(
                            id = transactionId ?: "",
                            amount = amountValue,
                            type = transactionType,
                            category = categoryName,
                            description = description.ifBlank { "" },
                            date = Timestamp(selectedDate),
                            userId = "" // Will be set in repository
                        )
                        
                        if (transactionId == null) {
                            transactionViewModel.addTransaction(transaction)
                        } else {
                            transactionViewModel.updateTransaction(transaction)
                        }
                        
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && 
                         amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                         transactionsState !is TransactionViewModel.TransactionsState.Loading &&
                         (selectedCategory.isNotBlank() || 
                          (categoriesState is CategoryViewModel.CategoriesState.Success && 
                           (categoriesState as CategoryViewModel.CategoriesState.Success).categories.isEmpty()))
            ) {
                if (transactionsState is TransactionViewModel.TransactionsState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp).width(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (transactionId == null) "Add Transaction" else "Update Transaction")
                }
            }
        }
    }
}