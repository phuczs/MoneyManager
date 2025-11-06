package com.example.moneymanager.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.ui.util.CategoryIcons
import com.example.moneymanager.ui.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Filter state
    var selectedTypeFilter by remember { mutableStateOf("all") } // all, income, expense
    
    // Category dialog state
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    
    // Delete confirmation dialog state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    
    // Load categories on initial composition
    LaunchedEffect(Unit) {
        categoryViewModel.loadAllCategories()
    }
    
    // Apply filter when it changes
    LaunchedEffect(selectedTypeFilter) {
        when (selectedTypeFilter) {
            "all" -> categoryViewModel.loadAllCategories()
            else -> categoryViewModel.loadCategoriesByType(selectedTypeFilter)
        }
    }
    
    // Category Dialog (Add/Edit)
    if (showCategoryDialog) {
        CategoryDialog(
            category = categoryToEdit,
            onDismiss = { 
                showCategoryDialog = false
                categoryToEdit = null
            },
            onConfirm = { category ->
                if (categoryToEdit == null) {
                    // Adding new category
                    categoryViewModel.addCategory(category)
                } else {
                    // Updating existing category
                    categoryViewModel.updateCategory(category)
                }
                showCategoryDialog = false
                categoryToEdit = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                categoryToDelete = null
            },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete '${categoryToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.id?.let { categoryViewModel.deleteCategory(it) }
                        showDeleteConfirmDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    categoryToEdit = null
                    showCategoryDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
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
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Categories list
            when (categoriesState) {
                is CategoryViewModel.CategoriesState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is CategoryViewModel.CategoriesState.Error -> {
                    val errorMessage = (categoriesState as CategoryViewModel.CategoriesState.Error).message
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: $errorMessage",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { categoryViewModel.loadAllCategories() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is CategoryViewModel.CategoriesState.Success -> {
                    val categories = (categoriesState as CategoryViewModel.CategoriesState.Success).categories
                    
                    if (categories.isEmpty()) {
                        EmptyCategoriesScreen()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories) { category ->
                                CategoryItem(
                                    category = category,
                                    onEdit = {
                                        categoryToEdit = category
                                        showCategoryDialog = true
                                    },
                                    onDelete = {
                                        categoryToDelete = category
                                        showDeleteConfirmDialog = true
                                    }
                                )
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

@Composable
fun EmptyCategoriesScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No categories yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first category to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit), // Make the entire card clickable for editing
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (category.type == "income") 
                            Color(0xFF4CAF50).copy(alpha = 0.1f) 
                        else Color(0xFFF44336).copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIcons.getIcon(category.icon),
                    contentDescription = category.name,
                    tint = if (category.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Category type badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (category.type == "income") 
                            Color(0xFF4CAF50).copy(alpha = 0.1f) 
                        else Color(0xFFF44336).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = category.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (category.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (name: String, type: String, icon: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("expense") } // Default to expense
    var selectedIcon by remember { mutableStateOf("default") }
    var showIconSelector by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Icon selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconSelector = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Icon",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.getIcon(selectedIcon),
                            contentDescription = "Selected Icon",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Select Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Category Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = categoryType == "expense",
                        onClick = { categoryType = "expense" }
                    )
                    Text("Expense")
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    RadioButton(
                        selected = categoryType == "income",
                        onClick = { categoryType = "income" }
                    )
                    Text("Income")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onAddCategory(categoryName, categoryType, selectedIcon)
                            }
                        },
                        enabled = categoryName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    
    // Icon selector dialog
    if (showIconSelector) {
        IconSelectorDialog(
            selectedIcon = selectedIcon,
            onIconSelected = { selectedIcon = it },
            onDismiss = { showIconSelector = false }
        )
    }
}

@Composable
fun CategoryDialog(
    category: Category? = null, // null for add, non-null for edit
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var categoryName by remember { mutableStateOf(category?.name ?: "") }
    var categoryType by remember { mutableStateOf(category?.type ?: "expense") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "default") }
    var showIconSelector by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (category == null) "Add New Category" else "Edit Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Icon selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconSelector = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Icon",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.getIcon(selectedIcon),
                            contentDescription = "Selected Icon",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Select Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Category Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = categoryType == "expense",
                        onClick = { categoryType = "expense" }
                    )
                    Text("Expense")
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    RadioButton(
                        selected = categoryType == "income",
                        onClick = { categoryType = "income" }
                    )
                    Text("Income")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                val updatedCategory = if (category == null) {
                                    // Creating new category
                                    Category(
                                        name = categoryName,
                                        type = categoryType,
                                        icon = selectedIcon
                                    )
                                } else {
                                    // Updating existing category
                                    category.copy(
                                        name = categoryName,
                                        type = categoryType,
                                        icon = selectedIcon
                                    )
                                }
                                onConfirm(updatedCategory)
                            }
                        },
                        enabled = categoryName.isNotBlank()
                    ) {
                        Text(if (category == null) "Add" else "Save")
                    }
                }
            }
        }
    }
    
    // Icon selector dialog
    if (showIconSelector) {
        IconSelectorDialog(
            selectedIcon = selectedIcon,
            onIconSelected = { selectedIcon = it },
            onDismiss = { showIconSelector = false }
        )
    }
}

@Composable
fun IconSelectorDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Icon") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoryIcons.availableIcons.chunked(5)) { iconRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        iconRow.forEach { iconName ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (iconName == selectedIcon) 
                                            Color(0xFF6366F1).copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { onIconSelected(iconName) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIcons.getIcon(iconName),
                                    contentDescription = iconName,
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        // Fill remaining spaces if less than 5 items
                        repeat(5 - iconRow.size) {
                            Box(modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
