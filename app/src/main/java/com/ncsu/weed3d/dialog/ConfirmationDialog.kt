package com.ncsu.weed3d.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import com.ncsu.weed3d.databinding.DialogConfirmationBinding

class ConfirmationDialog(context: Context, @StringRes var messageString: Int, @StringRes var cancelString: Int, @StringRes var continueString: Int): Dialog(context) {

    private lateinit var binding: DialogConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogConfirmationBinding.inflate(layoutInflater)
        binding.messageTextView.setText(messageString)
        binding.cancelButton.setText(cancelString)
        binding.continueButton.setText(continueString)
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.continueButton.setOnClickListener {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            dismiss()
        }
        setContentView(binding.root)
    }
}