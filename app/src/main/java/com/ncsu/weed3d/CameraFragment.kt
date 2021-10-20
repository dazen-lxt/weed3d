package com.ncsu.weed3d

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ncsu.weed3d.data.*
import com.ncsu.weed3d.databinding.FragmentCameraBinding
import com.ncsu.weed3d.dialog.ConditionsDialog
import com.ncsu.weed3d.dialog.ConditionsDialogProtocol
import com.ncsu.weed3d.dialog.TypeDialog
import com.ncsu.weed3d.dialog.TypeDialogProtocol
import com.ncsu.weed3d.gopro.MediaResponse
import com.ncsu.weed3d.gopro.RetrofitInstance
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.media.ToneGenerator

import android.media.AudioManager


class CameraFragment(val farmName: String, var farmType: String, val farmDate: String) : Fragment(), SensorEventListener, TypeDialogProtocol, ConditionsDialogProtocol {

    private var _binding: FragmentCameraBinding? = null

    private val mHandler = Handler()
    lateinit var db: AppDatabase
    private var metronomeRunning = false

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseInstance.getInstance(requireContext())
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mandatorySensors = listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE, Sensor.TYPE_PROXIMITY)
        val sensorTypes = mutableListOf<Int>()
        for(sensor in mandatorySensors) {
            if(sensorManager.getDefaultSensor(sensor) != null) {
                sensorTypes.add(sensor)
            }
        }
        sensorValues = mutableListOf()
        for(sensorType in sensorTypes) {
            sensorManager.getDefaultSensor(sensorType)?.let {
                sensorValues.add(SensorValue(it, true))
            }
        }
        binding.playImageView.setOnClickListener {
            startVideo()
        }

        binding.stopButton.setOnClickListener {
            stopVideo()
        }
        binding.editImageView.setOnClickListener {
            val comments = if(binding.commentsTextView.text.toString() == resources.getString(R.string.no_comments)) "" else binding.commentsTextView.text.toString()
            val dialog =  ConditionsDialog(requireContext(), comments, this)
            dialog.setCancelable(true)
            dialog.show()
        }
    }

    private fun stopVideo() {
        (requireActivity() as? HomeActivity)?.toggleLoader(true, "")
        RetrofitInstance.getInstance().triggerShutter(0).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                stopListeners()
                metronomeRunning = false
                binding.playGroup.isVisible = true
                binding.stopGroup.isVisible = false
                binding.hourTextView.stop()
                saveVideo()
            }

            override fun onFailure(call: Call<Void>?, t: Throwable?) {
                (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                (requireActivity() as HomeActivity).showSuccessMessage(resources.getString(R.string.gopro_disconnected), false)
            }
        })
    }

    private fun setupUI() {
        binding.farmTextView.text = farmName
        binding.farmTypeView.text = farmType
        binding.heightTextView.text = ConditionInfo.height.toString().plus(" in")
        binding.weatherImageView.setImageResource(when(ConditionInfo.weatherType) {
            WeatherType.SUNNY -> R.drawable.ic_sun
            WeatherType.CLOUDY -> R.drawable.ic_cloud
            else -> R.drawable.ic_clouds_and_sun
        })

    }

    private fun startVideo() {
        (requireActivity() as? HomeActivity)?.toggleLoader(true, "")
        val that = this
        doAsync {
            val videosInType = db.databaseDao().getVideoOfType(farmName, farmType, farmDate)
            if(videosInType.count() >= 2) {
                uiThread {
                    (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                    val dialog =  TypeDialog(requireContext(), farmName, farmDate, that)
                    dialog.setCancelable(true)
                    dialog.show()
                }
            } else {
                uiThread {
                    binding.numberVideoTextView.text = resources.getString(if(videosInType.size == 1)  R.string.second_video else R.string.first_video)
                }
                RetrofitInstance.getInstance().triggerShutter(1).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                        (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                        startListeners()
                        metronomeRunning = true
                        playMetronome()
                        binding.playGroup.isVisible = false
                        binding.stopGroup.isVisible = true
                        binding.hourTextView.base = SystemClock.elapsedRealtime();
                        binding.hourTextView.start()
                    }

                    override fun onFailure(call: Call<Void>?, t: Throwable?) {
                        (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                        (requireActivity() as HomeActivity).showSuccessMessage(resources.getString(R.string.gopro_disconnected), false)
                    }
                })
            }
        }
    }

    private fun saveVideo() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                RetrofitInstance.getMediaInstance().getMedia().enqueue(object : Callback<MediaResponse> {
                    override fun onResponse(call: Call<MediaResponse>?, response: Response<MediaResponse>?) {
                        response?.body()?.let { it ->
                            val localUri = "videos/DCIM/${it.media.first().d}/${it.media.first().fs.last().n}"
                            val videoData = VideoData(
                                farmName,
                                farmType,
                                "",
                                ConditionInfo.weatherType.name,
                                ConditionInfo.height,
                                farmDate,
                                binding.commentsTextView.text.toString(),
                                localUri,
                                "",
                                ""
                            )
                            doAsync {
                                val videoId = db.databaseDao().insertVideo(videoData)
                                saveSensorData(videoId)
                            }
                            return
                        }
                        saveVideo()
                    }

                    override fun onFailure(call: Call<MediaResponse>?, t: Throwable?) {
                        saveVideo()
                    }
                })
            },2000
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
        _binding = null
    }

    private fun saveSensorData(videoId: Long) {
        doAsync {
            sensorValues.forEach { sensor ->
                val sensorTimeData = sensor.values.map { sensorValue ->
                    SensorTimeData(videoId, sensor.type, sensorValue.time, sensorValue.values.fold("", {
                        acc, fl ->  acc.plus(fl.toString()).plus(",")
                    }))
                }
                db.databaseDao().insertSensorTime(sensorTimeData)
            }
            uiThread {
                (requireActivity() as? HomeActivity)?.toggleLoader(false, "")
                binding.numberVideoTextView.text = if(binding.numberVideoTextView.text == resources.getString(R.string.first_video)) {
                    resources.getString(R.string.second_video)
                } else {
                    ""
                }
                binding.farmTypeView.text = ""
            }
        }
    }

    override fun updateComments(comment: String) {
        binding.commentsTextView.text = if(comment.isEmpty()) resources.getString(R.string.no_comments) else comment
        setupUI()
    }

    private fun playMetronome() {
        mHandler.postDelayed(Runnable {
            if(metronomeRunning) {
                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                playMetronome()
            }
        }, 1000)
    }

    //Sensors
    private var initialTime: Long = 0
    private lateinit var sensorManager: SensorManager

    private var sensorValues: MutableList<SensorValue> = mutableListOf()

    private fun startListeners() {
        initialTime = System.currentTimeMillis()
        sensorValues.filter { it.isActive }.forEach {
            it.values = mutableListOf()
            sensorManager.registerListener(this, it.sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopListeners() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        sensorValues.find { it.sensor == event.sensor }?.values?.add(SensorValueTime(System.currentTimeMillis() - initialTime, event.values.asList()))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun startType(farmName: String, farmType: String, farmDate: String) {
        this.farmType = farmType
        setupUI()
    }
}