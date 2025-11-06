package com.example.moneymanager.data.repository

import com.example.moneymanager.data.model.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    private val budgetsCollection
        get() = firestore.collection("budgets")

    suspend fun addBudget(budget: Budget) {
        budgetsCollection.add(budget.copy(userId = userId)).await()
    }

    suspend fun getBudgetsForMonth(month: Int, year: Int): List<Budget> {
        return budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .await()
            .toObjects(Budget::class.java)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetsCollection.document(budget.id).set(budget).await()
    }

    suspend fun deleteBudget(budgetId: String) {
        budgetsCollection.document(budgetId).delete().await()
    }

    suspend fun getBudgetForCategory(category: String, month: Int, year: Int): Budget? {
        return budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("category", category)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .limit(1)
            .get()
            .await()
            .toObjects(Budget::class.java)
            .firstOrNull()
    }
}