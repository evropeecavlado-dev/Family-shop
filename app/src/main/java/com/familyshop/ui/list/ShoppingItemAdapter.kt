package com.familyshop.ui.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.familyshop.data.model.ItemStatus
import com.familyshop.data.model.ShoppingItem
import com.familyshop.databinding.ItemShoppingBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ShoppingItemAdapter(
    private val onToggle: (ShoppingItem) -> Unit,
    private val onDelete: (String) -> Unit
) : ListAdapter<ShoppingItem, ShoppingItemAdapter.ViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("bg"))

    inner class ViewHolder(private val binding: ItemShoppingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShoppingItem) {
            val isDone = item.status == ItemStatus.DONE

            // Текст на продукта
            binding.tvItemName.text = item.name
            if (isDone) {
                binding.tvItemName.paintFlags =
                    binding.tvItemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvItemName.paintFlags =
                    binding.tvItemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Чекбокс
            binding.checkbox.isChecked = isDone
            binding.checkbox.setOnClickListener { onToggle(item) }
            binding.root.setOnClickListener { onToggle(item) }

            // Дати
            binding.tvCreatedAt.isVisible = true
            item.createdAt?.let {
                binding.tvCreatedAt.text = "Добавено: ${dateFormat.format(it.toDate())}"
            }

            binding.tvCompletedAt.isVisible = isDone && item.completedAt != null
            item.completedAt?.let {
                binding.tvCompletedAt.text = "Купено: ${dateFormat.format(it.toDate())}"
            }

            // Статус бадж
            binding.tvStatus.text = if (isDone) "Изпълнено" else "Заявено"
            binding.tvStatus.setBackgroundResource(
                if (isDone) com.familyshop.R.drawable.bg_badge_done
                else com.familyshop.R.drawable.bg_badge_pending
            )

            // Изтриване (свайп се добавя в Activity, тук само дълго натискане)
            binding.root.setOnLongClickListener {
                onDelete(item.id)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShoppingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ShoppingItem>() {
        override fun areItemsTheSame(old: ShoppingItem, new: ShoppingItem) = old.id == new.id
        override fun areContentsTheSame(old: ShoppingItem, new: ShoppingItem) = old == new
    }
}
