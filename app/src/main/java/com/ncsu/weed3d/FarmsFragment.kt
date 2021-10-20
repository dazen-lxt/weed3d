package com.ncsu.weed3d

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncsu.weed3d.data.AppDatabase
import com.ncsu.weed3d.data.DatabaseInstance
import com.ncsu.weed3d.data.FarmData
import com.ncsu.weed3d.databinding.FragmentFarmsBinding
import com.ncsu.weed3d.dialog.*
import com.ncsu.weed3d.item_views.FarmItemView
import com.ncsu.weed3d.item_views.FarmItemViewProtocol
import com.xwray.groupie.GroupieAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

class FarmsFragment : Fragment(), NewFarmDialogProtocol, FarmItemViewProtocol, TypeDialogProtocol {

    private var _binding: FragmentFarmsBinding? = null

    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val farmsAdapter = GroupieAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmsBinding.inflate(inflater, container, false)
        binding.newButton.setOnClickListener {
            val dialog = NewFarmDialog(requireContext(), this)
            dialog.setCancelable(true)
            dialog.show()
        }
        binding.farmsRecyclerView.adapter = farmsAdapter
        binding.farmsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        db = DatabaseInstance.getInstance(requireContext())
        loadData()
        return binding.root
    }

    override fun createNewFarm(name: String) {
        doAsync {
            db.databaseDao().insertFarm(FarmData(name))
            uiThread {
                loadData()
            }
        }
    }

    private fun loadData() {
        val that = this
        doAsync {
            val data = db.databaseDao().getAllFarms()
            uiThread {
                farmsAdapter.replaceAll(data.map { FarmItemView(it, that, requireContext()) })
            }
        }
    }

    override fun showInfo(item: FarmData) {
        val dialog =  FarmInfoDialog(requireContext(), item.farmName)
        dialog.setCancelable(true)
        dialog.show()
    }

    override fun startVideo(item: FarmData) {
        val format = SimpleDateFormat("dd/MM/yyyy")
        val date = format.format(Date())
        val dialog =  TypeDialog(requireContext(), item.farmName, date, this)
        dialog.setCancelable(true)
        dialog.show()
    }

    override fun startType(farmName: String, farmType: String, farmDate: String) {
        (requireActivity() as HomeActivity).goToCamera(farmName, farmType, farmDate)
    }
}