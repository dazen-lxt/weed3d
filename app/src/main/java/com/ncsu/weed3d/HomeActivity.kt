package com.ncsu.weed3d

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import retrofit2.Callback
import com.leinardi.android.speeddial.SpeedDialView
import com.ncsu.weed3d.databinding.ActivityHomeBinding
import com.ncsu.weed3d.dialog.ConfirmationDialog
import com.ncsu.weed3d.gopro.RetrofitInstance
import retrofit2.Call
import retrofit2.Response
import android.os.Looper


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDial()
        goToFarms()
    }

    private fun setupDial() {
        binding.speedDial.inflate(R.menu.fab_menu)
        binding.speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.connectionMenu -> {
                    RetrofitInstance.getInstance().locate(1).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                            RetrofitInstance.getInstance().locate(0).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                                    showSuccessMessage(resources.getString(R.string.gopro_connected), true)
                                }

                                override fun onFailure(call: Call<Void>?, t: Throwable?) {
                                    val dialog = ConfirmationDialog(this@HomeActivity, R.string.go_pro_gopro_disconnected, R.string.to_continue, R.string.settings)
                                    dialog.setCancelable(true)
                                    dialog.show()
                                }
                            })
                        }

                        override fun onFailure(call: Call<Void>?, t: Throwable?) {
                            val dialog = ConfirmationDialog(this@HomeActivity, R.string.go_pro_gopro_disconnected, R.string.to_continue, R.string.settings)
                            dialog.setCancelable(true)
                            dialog.show()
                        }
                    })
                    return@OnActionSelectedListener true // false will close it without animation
                }
                R.id.historicMenu -> {
                    goToHistoric()
                }
                R.id.farmsMenu -> {
                    goToFarms()
                }
            }
            false
        })
    }

    fun goToCamera(farmName: String, farmType: String, farmDate: String) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, CameraFragment(farmName, farmType, farmDate))
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun goToFarms() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, FarmsFragment())
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun goToHistoric() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, HistoricFragment())
        ft.addToBackStack(null)
        ft.commit()
    }

    fun showSuccessMessage(text: String, isSuccess: Boolean) {
        binding.successCardView.setCardBackgroundColor(resources.getColor(if(isSuccess) R.color.success_bg else R.color.main_red, null))
        binding.successTextView.text = text
        binding.successCardView.isVisible = true
        Handler(Looper.getMainLooper()).postDelayed(
            {
                binding.successCardView.isVisible = false
            },3000
        )
    }

    fun toggleLoader(isVisible: Boolean, message: String) {
        binding.loadingContainer.isVisible = isVisible
        binding.loadingMessage.text = message
    }

    fun didCloseSuccessClick(view: View) {
        binding.successCardView.isVisible = false
    }


}