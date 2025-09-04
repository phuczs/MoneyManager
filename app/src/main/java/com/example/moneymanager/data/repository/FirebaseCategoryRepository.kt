package com.example.moneymanager.data.repository

import com.example.moneymanager.data.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CategoryRepository {

    private val categoriesCollection = firestore.collection("categories")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    override fun getAllCategories(): Flow<List<Category>> = callbackFlow {
        val userId = getCurrentUserId()
        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.toObjects(Category::class.java)
                    trySend(categories)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getCategoriesByType(type: String): Flow<List<Category>> = callbackFlow {
        val userId = getCurrentUserId()
        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.toObjects(Category::class.java)
                    trySend(categories)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun addCategory(category: Category): Result<Category> {
        return try {
            val userId = getCurrentUserId()
            val newCategory = category.copy(userId = userId)
            val documentRef = categoriesCollection.document()
            val categoryWithId = newCategory.copy(id = documentRef.id)
            documentRef.set(categoryWithId).await()
            Result.success(categoryWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}