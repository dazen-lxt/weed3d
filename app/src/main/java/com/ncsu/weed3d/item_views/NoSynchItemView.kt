package com.ncsu.weed3d.item_views

import android.content.Context
import android.net.Uri
import android.view.View
import com.ncsu.weed3d.R
import com.ncsu.weed3d.data.VideoData
import com.ncsu.weed3d.databinding.ItemNoSyncBinding
import com.ncsu.weed3d.util.OnSingleClickListener
import com.xwray.groupie.viewbinding.BindableItem
import android.provider.MediaStore

import android.graphics.Bitmap
import androidx.core.view.isVisible
import com.ncsu.weed3d.HomeActivity
import com.ncsu.weed3d.gopro.RetrofitInstance
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoSynchItemView(
    private val model: VideoData,
    private val delegate: NoSynchItemViewProtocol,
    private val context: Context,
    private val type: ItemTypeEnum
) : BindableItem<ItemNoSyncBinding>() {
    override fun bind(binding: ItemNoSyncBinding, position: Int) {
        binding.uriTextView.text = model.goProUri
        binding.nameTextView.text = model.farmName.plus("-").plus(model.farmType)
        binding.dateTextView.text = model.date
        when(type) {
            ItemTypeEnum.NO_SYNC -> {
                binding.downloadButton.isVisible = true
                binding.downloadButton.text = context.getString(R.string.download)
                binding.downloadButton.setOnClickListener(OnSingleClickListener {
                    delegate.downloadFile(
                        model
                    )
                })
            }
            ItemTypeEnum.LOCAL -> {
                binding.downloadButton.isVisible = true
                binding.downloadButton.text = context.getString(R.string.upload)
                binding.downloadButton.setOnClickListener(OnSingleClickListener {
                    delegate.uploadFile(
                        model
                    )
                })
            }
            ItemTypeEnum.REMOTE -> {
                binding.downloadButton.isVisible = false
            }
        }
        if(model.thumbnailUri.isNotEmpty()) {
            binding.loadUri()
            binding.thumbnailImageView.isVisible = true
        } else {
            binding.thumbnailImageView.isVisible = false
        }
    }

    private fun ItemNoSyncBinding.loadUri() {
        val bitmap =
            MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(java.io.File(context.getExternalFilesDir("").toString() + model.thumbnailUri)))
        thumbnailImageView.setImageBitmap(bitmap)
    }

    override fun getLayout() = R.layout.item_no_sync

    override fun initializeViewBinding(view: View): ItemNoSyncBinding {
        return ItemNoSyncBinding.bind(view)
    }

}

enum class ItemTypeEnum {
    NO_SYNC, LOCAL, REMOTE
}

interface NoSynchItemViewProtocol {
    fun downloadFile(item: VideoData)
    fun uploadFile(item: VideoData)
}