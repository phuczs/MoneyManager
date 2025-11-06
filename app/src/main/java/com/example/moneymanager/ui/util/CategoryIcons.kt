package com.example.moneymanager.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    // Define a map of icon names to actual icons
    val iconMap = mapOf(
        "default" to Icons.Default.Category,
        "food" to Icons.Default.Restaurant,
        "transport" to Icons.Default.DirectionsCar,
        "shopping" to Icons.Default.ShoppingCart,
        "bills" to Icons.Default.Receipt,
        "entertainment" to Icons.Default.Movie,
        "healthcare" to Icons.Default.LocalHospital,
        "salary" to Icons.Default.AccountBalance,
        "freelance" to Icons.Default.Work,
        "investment" to Icons.Default.TrendingUp,
        "gift" to Icons.Default.CardGiftcard,
        "education" to Icons.Default.School,
        "travel" to Icons.Default.Flight,
        "utilities" to Icons.Default.Home,
        "other" to Icons.Default.MoreHoriz
    )
    
    // Get icon by name, fallback to default if not found
    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Default.Category
    }
    
    // List of available icon names for user selection
    val availableIcons = iconMap.keys.toList()
}