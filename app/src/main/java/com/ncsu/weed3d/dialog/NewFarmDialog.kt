package com.ncsu.weed3d.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import com.ncsu.weed3d.R
import com.ncsu.weed3d.databinding.DialogNewFarmBinding
import com.ncsu.weed3d.util.afterTextChanged

class NewFarmDialog(context: Context, val delegate: NewFarmDialogProtocol): Dialog(context) {

    private lateinit var binding: DialogNewFarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogNewFarmBinding.inflate(layoutInflater)
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        ArrayAdapter.createFromResource(
            context,
            R.array.state_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.stateSpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            context,
            R.array.group_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.groupSpinner.adapter = adapter
        }
        binding.nameEditText.afterTextChanged {
            binding.continueButton.isEnabled = it.isNotEmpty()
        }
        binding.continueButton.setOnClickListener {
            delegate.createNewFarm("${binding.stateSpinner.selectedItem}-${binding.groupSpinner.selectedItem}-${binding.nameEditText.text.toString()}")
            dismiss()
        }
        setContentView(binding.root)
    }
}

interface NewFarmDialogProtocol {
    fun createNewFarm(name: String)
}