package com.example.moneymanager.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Transaction(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: String = "", // "income" or "expense"
    val category: String = "",
    val description: String = "",
    val date: Timestamp = Timestamp.now()
)