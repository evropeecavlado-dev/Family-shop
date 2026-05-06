package com.familyshop.ui.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familyshop.R
import com.familyshop.databinding.ActivityShoppingListBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingListBinding
    private val viewModel: ShoppingListViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var pendingAdapter: ShoppingItemAdapter
    private lateinit var doneAdapter: ShoppingItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupAdapters()
        setupInput()
        observeState()
    }

    private fun setupAdapters() {
        pendingAdapter = ShoppingItemAdapter(
            onToggle = { viewModel.toggleItem(it) },
            onDelete = { showDeleteDialog(it) }
        )
        doneAdapter = ShoppingItemAdapter(
            onToggle = { viewModel.toggleItem(it) },
            onDelete = { showDeleteDialog(it) }
        )

        binding.rvPending.apply {
            adapter = pendingAdapter
            layoutManager = LinearLayoutManager(this@ShoppingListActivity)
        }
        binding.rvDone.apply {
            adapter = doneAdapter
            layoutManager = LinearLayoutManager(this@ShoppingListActivity)
        }

        // Свайп за изтриване
        setupSwipeToDelete(binding.rvPending)
        setupSwipeToDelete(binding.rvDone)
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter as ShoppingItemAdapter
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.currentList[viewHolder.adapterPosition]
                showDeleteDialog(item.id)
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun setupInput() {
        binding.btnAdd.setOnClickListener {
            val name = binding.etNewItem.text.toString()
            viewModel.addItem(name)
            binding.etNewItem.text?.clear()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading

                    pendingAdapter.submitList(state.pendingItems)
                    doneAdapter.submitList(state.doneItems)

                    // Брой заявени / изпълнени
                    binding.tvPendingCount.text = "Заявени (${state.pendingItems.size})"
                    binding.tvDoneCount.text = "Изпълнени (${state.doneItems.size})"
                    binding.sectionDone.isVisible = state.doneItems.isNotEmpty()

                    state.error?.let {
                        Toast.makeText(this@ShoppingListActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(itemId: String) {
        AlertDialog.Builder(this)
            .setTitle("Изтриване")
            .setMessage("Сигурни ли сте, че искате да изтриете този продукт?")
            .setPositiveButton("Изтрий") { _, _ -> viewModel.deleteItem(itemId) }
            .setNegativeButton("Отказ", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                auth.signOut()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
