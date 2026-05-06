package com.familyshop.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyshop.data.model.ItemStatus
import com.familyshop.data.model.ShoppingItem
import com.familyshop.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val pendingItems: List<ShoppingItem> = emptyList(),
    val doneItems: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState

    init {
        observeItems()
    }

    private fun observeItems() {
        viewModelScope.launch {
            repository.observeItems()
                .map { items ->
                    ListUiState(
                        pendingItems = items.filter { it.status == ItemStatus.PENDING },
                        doneItems = items.filter { it.status == ItemStatus.DONE },
                        isLoading = false
                    )
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun addItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.addItem(name) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            runCatching {
                if (item.status == ItemStatus.PENDING) {
                    repository.markDone(item.id)
                } else {
                    repository.markPending(item.id)
                }
            }.onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteItem(itemId) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
