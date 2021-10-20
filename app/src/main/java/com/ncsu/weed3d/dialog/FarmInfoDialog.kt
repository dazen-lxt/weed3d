package com.ncsu.weed3d.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncsu.weed3d.data.AppDatabase
import com.ncsu.weed3d.data.DatabaseInstance
import com.ncsu.weed3d.databinding.DialogFarmInfoBinding
import com.ncsu.weed3d.item_views.FarmInfoItemView
import com.xwray.groupie.GroupieAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class FarmInfoDialog(context: Context, val farmName: String): Dialog(context) {

    private lateinit var binding: DialogFarmInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogFarmInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        doAsync {
            val db = DatabaseInstance.getInstance(context)
            val videos = db.databaseDao().getVideoOfFarm(farmName)
            uiThread {
                val groups = videos.groupBy { it.date }.toList()
                val adapter = GroupieAdapter()
                val items = groups.mapIndexed { index, group ->
                    FarmInfoItemView(index, group.first, group.second, context)
                }
                binding.infoRecyclerView.adapter = adapter
                binding.infoRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.noEntriesTextView.isVisible = items.isEmpty()
                binding.infoRecyclerView.isVisible = items.isNotEmpty()
                adapter.addAll(items)
            }
        }
    }
}