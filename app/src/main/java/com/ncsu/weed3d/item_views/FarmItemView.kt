package com.ncsu.weed3d.item_views

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.core.view.isVisible
import com.ncsu.weed3d.R
import com.ncsu.weed3d.data.FarmData
import com.ncsu.weed3d.data.VideoData
import com.ncsu.weed3d.databinding.ItemFarmBinding
import com.ncsu.weed3d.databinding.ItemNoSyncBinding
import com.ncsu.weed3d.util.OnSingleClickListener
import com.xwray.groupie.viewbinding.BindableItem

class FarmItemView(
    private val model: FarmData,
    private val delegate: FarmItemViewProtocol,
    private val context: Context
) : BindableItem<ItemFarmBinding>() {
    override fun bind(binding: ItemFarmBinding, position: Int) {
        binding.nameTextView.text = model.farmName
        binding.playButton.setOnClickListener {
            delegate.startVideo(model)
        }
        binding.infoButton.setOnClickListener {
            delegate.showInfo(model)
        }
    }

    override fun getLayout() = R.layout.item_farm

    override fun initializeViewBinding(view: View): ItemFarmBinding {
        return ItemFarmBinding.bind(view)
    }

}

interface FarmItemViewProtocol {
    fun showInfo(item: FarmData)
    fun startVideo(item: FarmData)
}