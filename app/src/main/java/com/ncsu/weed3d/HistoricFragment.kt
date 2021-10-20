package com.ncsu.weed3d

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.ncsu.weed3d.data.AppDatabase
import com.ncsu.weed3d.data.DatabaseInstance
import com.ncsu.weed3d.data.VideoData
import com.ncsu.weed3d.databinding.FragmentHistoricBinding
import com.ncsu.weed3d.gopro.RetrofitInstance
import com.ncsu.weed3d.item_views.ItemTypeEnum
import com.ncsu.weed3d.item_views.NoSynchItemView
import com.ncsu.weed3d.item_views.NoSynchItemViewProtocol
import com.ncsu.weed3d.storage.GoogleDrive
import com.xwray.groupie.GroupieAdapter
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class HistoricFragment : Fragment(), NoSynchItemViewProtocol {

    private var _binding: FragmentHistoricBinding? = null

    private val binding get() = _binding!!


    private lateinit var db: AppDatabase
    private val noSyncAdapter = GroupieAdapter()
    private val localSyncedAdapter = GroupieAdapter()
    private val remoteSyncedAdapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoricBinding.inflate(inflater, container, false)
        binding.noSyncRecyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.localSyncedRecyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.remoteSyncedRecyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.noSyncRecyclerView.adapter = noSyncAdapter
        binding.localSyncedRecyclerView.adapter = localSyncedAdapter
        binding.remoteSyncedRecyclerView.adapter = remoteSyncedAdapter
        db = DatabaseInstance.getInstance(requireContext())
        loadData()
        return binding.root

    }

    private fun loadData() {
        val that = this
        doAsync {
            val data = db.databaseDao().getAllVideo()
            uiThread {
                noSyncAdapter.replaceAll(data.filter { it.deviceUri.isEmpty() && it.remoteUrl.isEmpty() }.map { NoSynchItemView(it, that, requireContext(), ItemTypeEnum.NO_SYNC) })
                localSyncedAdapter.replaceAll(data.filter { it.deviceUri.isNotEmpty() && it.remoteUrl.isEmpty() }.map { NoSynchItemView(it, that, requireContext(), ItemTypeEnum.LOCAL) })
                remoteSyncedAdapter.replaceAll(data.filter { it.remoteUrl.isNotEmpty() }.map { NoSynchItemView(it, that, requireContext(), ItemTypeEnum.REMOTE) })

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun uploadFile(item: VideoData) {
        (requireActivity() as? HomeActivity)?.toggleLoader(true, "")
        GoogleDrive.uploadImagesByDrive(item, requireContext()) {
            updateData(resources.getString(R.string.video_upload_success))
        }
    }

    override fun downloadFile(item: VideoData) {
        (requireActivity() as? HomeActivity)?.toggleLoader(true, "")
        val goProUri = item.goProUri.replace("http://10.5.5.9:8080/", "")
        RetrofitInstance.getMediaInstance().downloadFileByUrl(goProUri).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                response.body()?.let {
                    saveToDisk(it, item)
                } ?: run {
                    (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                    (requireActivity() as? HomeActivity)?.showSuccessMessage(resources.getString(R.string.video_download_fail), false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                (requireActivity() as? HomeActivity)?.showSuccessMessage(resources.getString(R.string.video_download_fail), false)
            }

        })
    }

    private fun saveToDisk(body: ResponseBody, videoData: VideoData) {
        doAsync {
            try {
                val fileUriSplit = videoData.goProUri.split("/")
                val folder = "/videos/" + fileUriSplit[fileUriSplit.size - 2]
                val fileName = folder + "/" + fileUriSplit[fileUriSplit.size - 1]
                File(requireContext().getExternalFilesDir("").toString() + "/videos").mkdir()
                File(requireContext().getExternalFilesDir("").toString()+ folder).mkdir()
                val destinationFile =
                    File(requireContext().getExternalFilesDir("").toString() + fileName)
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(destinationFile)
                    val data = ByteArray(4096)
                    var count: Int
                    var progress = 0
                    while (inputStream.read(data).also { count = it } != -1) {
                        outputStream.write(data, 0, count)
                        progress += count
                        uiThread {
                            (requireActivity() as? HomeActivity)?.toggleLoader(true, "Progress: " + 100 * progress / body.contentLength() + "% (" + body.contentLength() + ")")
                        }
                    }
                    outputStream.flush()
                    videoData.deviceUri = fileName
                    db.databaseDao().updateVideo(videoData)
                    downloadThumbnail(videoData)
                } catch (e: IOException) {
                    e.printStackTrace()
                    uiThread {
                        (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                        (requireActivity() as? HomeActivity)?.showSuccessMessage(
                            "Failed to save the file: " + e.message,
                            false
                        )
                    }
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                uiThread {
                    (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                    (requireActivity() as? HomeActivity)?.showSuccessMessage(
                        "Failed to save the file :" + e.message,
                        false
                    )
                }
            }
        }
    }

    private fun downloadThumbnail(videoData: VideoData) {
        val thumbnailUri = videoData.goProUri.replace("videos/DCIM/", "http://10.5.5.9/gp/gpMediaMetadata?p=")
        RetrofitInstance.getMediaInstance().downloadFileByUrl(thumbnailUri).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                response.body()?.let {
                    saveThumbnailToDisk(it, videoData)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                updateData("File saved successfully, but without Thumbnail")
            }

        })
    }

    private fun saveThumbnailToDisk(body: ResponseBody, videoData: VideoData) {
        doAsync {
            try {
                val fileUriSplit = videoData.goProUri.replace("MP4", "JPG").split("/")
                val folder = "/thumbnail/" + fileUriSplit[fileUriSplit.size - 2]
                val fileName = folder + "/" + fileUriSplit[fileUriSplit.size - 1]
                File(requireContext().getExternalFilesDir("").toString() + "/thumbnail").mkdir()
                File(requireContext().getExternalFilesDir("").toString()+ folder).mkdir()
                val destinationFile =
                    File(requireContext().getExternalFilesDir("").toString() + fileName)
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(destinationFile)
                    val data = ByteArray(4096)
                    var count: Int
                    var progress = 0
                    while (inputStream.read(data).also { count = it } != -1) {
                        outputStream.write(data, 0, count)
                        progress += count
                        uiThread {
                            (requireActivity() as? HomeActivity)?.toggleLoader(true, "Thumbnail Progress: " + 100 * progress / body.contentLength() + "% (" + body.contentLength() + ")")
                        }
                    }
                    outputStream.flush()
                    videoData.thumbnailUri = fileName
                    db.databaseDao().updateVideo(videoData)
                    uiThread {
                        updateData("File saved successfully!")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    uiThread {
                        updateData("File saved successfully, but without Thumbnail")
                    }
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                uiThread {
                    updateData("File saved successfully, but without Thumbnail")
                }
            }
        }
    }

    private fun updateData(message: String) {
        loadData()
        (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
        (requireActivity() as? HomeActivity)?.showSuccessMessage(message, true)
    }
}