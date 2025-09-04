package com.example.moneymanager.di

import com.example.moneymanager.data.repository.AuthRepository
import com.example.moneymanager.data.repository.CategoryRepository
import com.example.moneymanager.data.repository.FirebaseAuthRepository
import com.example.moneymanager.data.repository.FirebaseCategoryRepository
import com.example.moneymanager.data.repository.FirebaseTransactionRepository
import com.example.moneymanager.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return FirebaseAuthRepository(auth)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): TransactionRepository {
        return FirebaseTransactionRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CategoryRepository {
        return FirebaseCategoryRepository(firestore, auth)
    }
}