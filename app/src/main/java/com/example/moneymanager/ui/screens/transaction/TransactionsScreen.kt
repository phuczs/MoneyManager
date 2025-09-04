package com.example.moneymanager.ui.screens.transaction

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.ui.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onTransactionClick: (String) -> Unit,
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val transactionsState by transactionViewModel.transactionsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Filter states
    var selectedTypeFilter by remember { mutableStateOf("all") } // all, income, expense
    var isMonthFilterExpanded by remember { mutableStateOf(false) }
    var selectedMonthFilter by remember { mutableStateOf("All Time") }
    
    val months = listOf(
        "All Time",
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    // Load transactions on initial composition
    LaunchedEffect(Unit) {
        transactionViewModel.loadAllTransactions()
    }
    
    // Apply filters when they change
    LaunchedEffect(selectedTypeFilter, selectedMonthFilter) {
        when {
            selectedTypeFilter != "all" && selectedMonthFilter != "All Time" -> {
                // For now, just use the type filter since we don't have a combined method
                transactionViewModel.loadTransactionsByType(selectedTypeFilter)
            }
            selectedTypeFilter != "all" -> {
                transactionViewModel.loadTransactionsByType(selectedTypeFilter)
            }
            selectedMonthFilter != "All Time" -> {
                val monthIndex = months.indexOf(selectedMonthFilter) - 1 // -1 because "All Time" is at index 0
                val currentYear = java.time.Year.now().value
                val yearMonth = java.time.YearMonth.of(currentYear, monthIndex + 1) // +1 because months are 1-based
                transactionViewModel.loadTransactionsByMonth(yearMonth)
            }
            else -> {
                transactionViewModel.loadAllTransactions()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                // Type filter chips
                FilterChip(
                    selected = selectedTypeFilter == "all",
                    onClick = { selectedTypeFilter = "all" },
                    label = { Text("All") }
                )
                
                FilterChip(
                    selected = selectedTypeFilter == "income",
                    onClick = { selectedTypeFilter = "income" },
                    label = { Text("Income") }
                )
                
                FilterChip(
                    selected = selectedTypeFilter == "expense",
                    onClick = { selectedTypeFilter = "expense" },
                    label = { Text("Expense") }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Month filter dropdown
                ExposedDropdownMenuBox(
                    expanded = isMonthFilterExpanded,
                    onExpandedChange = { isMonthFilterExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMonthFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMonthFilterExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .width(150.dp),
                        singleLine = true
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isMonthFilterExpanded,
                        onDismissRequest = { isMonthFilterExpanded = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    selectedMonthFilter = month
                                    isMonthFilterExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Transactions list
            when (transactionsState) {
                is TransactionViewModel.TransactionsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is TransactionViewModel.TransactionsState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = (transactionsState as TransactionViewModel.TransactionsState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is TransactionViewModel.TransactionsState.Success -> {
                    val transactions = (transactionsState as TransactionViewModel.TransactionsState.Success).transactions
                    
                    if (transactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "No transactions found",
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transactions) { transaction ->
                                TransactionListItem(transaction, onTransactionClick)
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionListItem(transaction: Transaction, onTransactionClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { transaction.id?.let { onTransactionClick(it) } },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == "income") Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else Color(0xFFF44336).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "income") Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = transaction.type,
                    tint = if (transaction.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category ?: "Uncategorized",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (!transaction.description.isNullOrEmpty()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                transaction.date?.let { date ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = dateFormat.format(date.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            Text(
                text = formatter.format(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}