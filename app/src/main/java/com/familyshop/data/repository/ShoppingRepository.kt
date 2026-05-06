package com.familyshop.data.repository

import com.familyshop.data.model.ItemStatus
import com.familyshop.data.model.ShoppingItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Единна колекция за цялото семейство
    // В бъдеще може да се добави "familyId" за поддръжка на множество семейства
    private val collection = firestore.collection("shopping_items")

    // Слушаме промените в реално време
    fun observeItems(): Flow<List<ShoppingItem>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(ShoppingItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addItem(name: String) {
        val sevenDaysFromNow = Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
        val item = mapOf(
            "name" to name.trim(),
            "status" to ItemStatus.PENDING.name,
            "createdAt" to Timestamp.now(),
            "completedAt" to null,
            // TTL поле — Firebase Functions ще изтриват записи след тази дата
            "deleteAfter" to Timestamp(sevenDaysFromNow)
        )
        collection.add(item).await()
    }

    suspend fun markDone(itemId: String) {
        val now = Timestamp.now()
        val sevenDaysLater = Date(now.toDate().time + 7L * 24 * 60 * 60 * 1000)
        collection.document(itemId).update(
            mapOf(
                "status" to ItemStatus.DONE.name,
                "completedAt" to now,
                "deleteAfter" to Timestamp(sevenDaysLater)
            )
        ).await()
    }

    suspend fun markPending(itemId: String) {
        collection.document(itemId).update(
            mapOf(
                "status" to ItemStatus.PENDING.name,
                "completedAt" to null,
                "deleteAfter" to null
            )
        ).await()
    }

    suspend fun deleteItem(itemId: String) {
        collection.document(itemId).delete().await()
    }
}
