package com.ncsu.weed3d.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.CompoundButton
import com.ncsu.weed3d.R
import com.ncsu.weed3d.data.ConditionInfo
import com.ncsu.weed3d.data.WeatherType
import com.ncsu.weed3d.databinding.DialogConditionsBinding

class ConditionsDialog(context: Context, private val comments: String, private val listener: ConditionsDialogProtocol) : Dialog(context, R.style.TransparentDialog),
    CompoundButton.OnCheckedChangeListener {


    private lateinit var binding: DialogConditionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogConditionsBinding.inflate(layoutInflater)
        binding.heightEditText.setText(ConditionInfo.height.toString())
        binding.commentsEditText.setText(comments)
        when(ConditionInfo.weatherType) {
            WeatherType.SUNNY -> binding.sunnyRadioButton.isChecked = true
            WeatherType.CLOUDY -> binding.cloudyRadioButton.isChecked = true
            else -> binding.partialCloudRadioButton.isChecked = true
        }
        binding.continueButton.setOnClickListener {
            ConditionInfo.height = binding.heightEditText.text.toString().toInt()
            ConditionInfo.weatherType = when {
                binding.sunnyRadioButton.isChecked -> WeatherType.SUNNY
                binding.cloudyRadioButton.isChecked -> WeatherType.CLOUDY
                else -> WeatherType.PARTIAL
            }
            listener.updateComments(binding.commentsEditText.text.toString())
            dismiss()
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.sunnyRadioButton.setOnCheckedChangeListener(this)
        binding.cloudyRadioButton.setOnCheckedChangeListener(this)
        binding.partialCloudRadioButton.setOnCheckedChangeListener(this)

        setContentView(binding.root)
    }

    override fun onCheckedChanged(radioButton: CompoundButton, isChecked: Boolean) {
        if(isChecked) {
            binding.sunnyRadioButton.isChecked = radioButton.id == R.id.sunnyRadioButton
            binding.partialCloudRadioButton.isChecked = radioButton.id == R.id.partialCloudRadioButton
            binding.cloudyRadioButton.isChecked = radioButton.id == R.id.cloudyRadioButton
        }
    }


}

interface ConditionsDialogProtocol {
    fun updateComments(comment: String)
}