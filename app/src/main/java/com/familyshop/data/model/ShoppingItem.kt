package com.familyshop.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ShoppingItem(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val status: ItemStatus = ItemStatus.PENDING,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null,
    // Автоматично изтриване: Firestore TTL поле (epoch секунди)
    val deleteAfter: Timestamp? = null
)

enum class ItemStatus {
    PENDING,    // Заявено
    DONE        // Изпълнено
}

// Помощни extension функции
fun ShoppingItem.isExpired(): Boolean {
    val completed = completedAt ?: return false
    val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
    return System.currentTimeMillis() - completed.toDate().time > sevenDaysMs
}
