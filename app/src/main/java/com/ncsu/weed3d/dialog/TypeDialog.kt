package com.ncsu.weed3d.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ncsu.weed3d.HomeActivity
import com.ncsu.weed3d.R
import com.ncsu.weed3d.data.DatabaseInstance
import com.ncsu.weed3d.databinding.DialogTypeBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class TypeDialog(context: Context, private val farmName: String, private val farmDate: String, private val delegate: TypeDialogProtocol): Dialog(context, R.style.TransparentDialog) {

    private lateinit var binding: DialogTypeBinding
    private var videosInTypeB1 = 0
    private var videosInTypeB2 = 0
    private var videosInTypeCC1 = 0
    private var videosInTypeCC2 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.b1Container.setOnClickListener {
            if(videosInTypeB1 != 2) {
                delegate.startType(farmName, "B1", farmDate)
                dismiss()
            }
        }
        binding.b2Container.setOnClickListener {
            if(videosInTypeB2 != 2) {
                delegate.startType(farmName, "B2", farmDate)
                dismiss()
            }
        }
        binding.cc1Container.setOnClickListener {
            if(videosInTypeCC1 != 2) {
                delegate.startType(farmName, "CC1", farmDate)
                dismiss()
            }
        }
        binding.cc2Container.setOnClickListener {
            if(videosInTypeCC2 != 2) {
                delegate.startType(farmName, "CC2", farmDate)
                dismiss()
            }
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        (context as? HomeActivity)?.toggleLoader(true, "")
        doAsync {
            val db = DatabaseInstance.getInstance(context)
            videosInTypeB1 = db.databaseDao().getVideoOfType(farmName, "B1", farmDate).count()
            videosInTypeB2 = db.databaseDao().getVideoOfType(farmName, "B2", farmDate).count()
            videosInTypeCC1 = db.databaseDao().getVideoOfType(farmName, "CC1", farmDate).count()
            videosInTypeCC2 = db.databaseDao().getVideoOfType(farmName, "CC2", farmDate).count()

            uiThread {
                binding.b1TextView.text = videosInTypeB1.toString().plus(" de 2")
                binding.b2TextView.text = videosInTypeB2.toString().plus(" de 2")
                binding.cc1TextView.text = videosInTypeCC1.toString().plus(" de 2")
                binding.cc2TextView.text = videosInTypeCC2.toString().plus(" de 2")
                if(videosInTypeB1 == 2) binding.b1Container.alpha = 0.5f
                if(videosInTypeB2 == 2) binding.b2Container.alpha = 0.5f
                if(videosInTypeCC1 == 2) binding.cc1Container.alpha = 0.5f
                if(videosInTypeCC2 == 2) binding.cc2Container.alpha = 0.5f
                (context as? HomeActivity)?.toggleLoader(false, "")
            }
        }
    }
}

interface TypeDialogProtocol {
    fun startType(farmName: String, farmType: String, farmDate: String)
}