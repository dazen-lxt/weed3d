package com.ncsu.weed3d.item_views

import android.content.Context
import android.view.View
import com.ncsu.weed3d.R
import com.ncsu.weed3d.data.VideoData
import com.ncsu.weed3d.databinding.ItemFarmInfoBinding
import com.xwray.groupie.viewbinding.BindableItem


class FarmInfoItemView(
    private val index: Int,
    private val farmDate: String,
    private val videos: List<VideoData>,
    private val context: Context
) : BindableItem<ItemFarmInfoBinding>() {
    override fun bind(binding: ItemFarmInfoBinding, position: Int) {
        binding.entryTextView.text = context.resources.getString(R.string.entry_num).plus(index + 1)
        binding.dateTextView.text = farmDate
        binding.infoTextView.text = context.resources.getString(R.string.video_info,
            videos.filter {
                it.farmType == "B1"
            }.size,
            videos.filter {
                it.farmType == "B2"
            }.size,
            videos.filter {
                it.farmType == "CC1"
            }.size,
            videos.filter {
                it.farmType == "CC2"
            }.size)
    }

    override fun getLayout() = R.layout.item_farm_info

    override fun initializeViewBinding(view: View): ItemFarmInfoBinding {
        return ItemFarmInfoBinding.bind(view)
    }

}