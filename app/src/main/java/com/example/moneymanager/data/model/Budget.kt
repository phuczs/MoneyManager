package com.example.moneymanager.data.model

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    var spentAmount: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0
)
