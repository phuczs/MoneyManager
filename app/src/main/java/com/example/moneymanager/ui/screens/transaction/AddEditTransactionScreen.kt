package com.example.moneymanager.ui.screens.transaction

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.ui.util.CategoryIcons
import com.example.moneymanager.ui.viewmodel.CategoryViewModel
import com.example.moneymanager.ui.viewmodel.TransactionViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
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

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("expense") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val transaction by transactionViewModel.currentTransaction.collectAsState()
    val transactionsState by transactionViewModel.transactionsState.collectAsState()

    LaunchedEffect(transactionsState) {
        if (transactionsState is TransactionViewModel.TransactionsState.Error) {
            snackbarHostState.showSnackbar((transactionsState as TransactionViewModel.TransactionsState.Error).message)
        }
    }

    LaunchedEffect(transactionType) {
        categoryViewModel.loadCategoriesByType(transactionType)
    }

    LaunchedEffect(categoriesState) {
        if (categoriesState is CategoryViewModel.CategoriesState.Success) {
            val categories = (categoriesState as CategoryViewModel.CategoriesState.Success).categories
            if (categories.isEmpty()) {
                categoryViewModel.createDefaultCategories()
            }
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            transactionViewModel.getTransactionById(transactionId)
        }
    }

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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TransactionTypeSelector(transactionType) { transactionType = it } }
            item { AmountInput(amount) { amount = it } }
            item { CategorySelector(categoriesState, selectedCategory, isExpanded, { isExpanded = it }) { selectedCategory = it } }
            item { DescriptionInput(description) { description = it } }
            item { DateSelector(selectedDate) { selectedDate = it } }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SaveButton(transactionId, amount, selectedCategory, transactionsState) {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        return@SaveButton
                    }

                    val categoryName = if (selectedCategory.isBlank()) "Other" else selectedCategory

                    val newTransaction = Transaction(
                        id = transactionId ?: "",
                        amount = amountValue,
                        type = transactionType,
                        category = categoryName,
                        description = description.ifBlank { "" },
                        date = Timestamp(selectedDate),
                        userId = ""
                    )

                    if (transactionId == null) {
                        transactionViewModel.addTransaction(newTransaction)
                    } else {
                        transactionViewModel.updateTransaction(newTransaction)
                    }
                    onNavigateBack()
                }
            }
        }
    }
}

@Composable
fun TransactionTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("expense", "income").forEach { type ->
                val isSelected = selectedType == type
                Button(
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    elevation = if (isSelected) ButtonDefaults.buttonElevation(4.dp) else ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(type.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
fun AmountInput(amount: String, onAmountChange: (String) -> Unit) {
    val currencySymbol = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).currency?.symbol ?: "$"
    }
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text("Amount") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        leadingIcon = {
            Text(currencySymbol, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        },
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categoriesState: CategoryViewModel.CategoriesState,
    selectedCategory: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            placeholder = { Text("Select Category") }
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            when (categoriesState) {
                is CategoryViewModel.CategoriesState.Loading -> {
                    DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                }
                is CategoryViewModel.CategoriesState.Error -> {
                    DropdownMenuItem(text = { Text("Error loading categories") }, onClick = {})
                }
                is CategoryViewModel.CategoriesState.Success -> {
                    val categories = categoriesState.categories
                    if (categories.isEmpty()) {
                        DropdownMenuItem(text = { Text("No categories available") }, onClick = {})
                    } else {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = CategoryIcons.getIcon(category.icon),
                                            contentDescription = category.name,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    onCategorySelected(category.name)
                                    onExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DescriptionInput(description: String, onDescriptionChange: (String) -> Unit) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        minLines = 3
    )
}

@Composable
fun DateSelector(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
        onValueChange = {},
        label = { Text("Date") },
        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
        readOnly = true,
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

@Composable
fun SaveButton(
    transactionId: String?,
    amount: String,
    selectedCategory: String,
    transactionsState: TransactionViewModel.TransactionsState,
    onSave: () -> Unit
) {
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = amount.isNotBlank() &&
                amount.toDoubleOrNull() != null && (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                transactionsState !is TransactionViewModel.TransactionsState.Loading,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (transactionsState is TransactionViewModel.TransactionsState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                if (transactionId == null) "Add Transaction" else "Update Transaction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
