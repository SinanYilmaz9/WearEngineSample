package dev.sinanyilmaz.wearenginesample

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.sensor.Sensor
import com.huawei.wearengine.sensor.SensorClient
import com.huawei.wearengine.sensor.SensorReadCallback
import com.huawei.wearengine.sensor.SensorStopCallback
import dev.sinanyilmaz.wearenginesample.databinding.ActivitySensorBinding
import dev.sinanyilmaz.wearenginesample.util.printOperationResult
import java.util.Arrays


class SensorActivity : AppCompatActivity() {

    lateinit var binding: ActivitySensorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySensorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mSensorClient = HiWear.getSensorClient(this)
        mCurrentReadDevice = intent.getParcelableExtra("currentDevice");

        if (mCurrentReadDevice == null) {
            printOperationResult("mCurrentReadDevice is null.");
            return
        }

        if (mCurrentReadDevice?.name != null) binding.textViewDeviceName.text = mCurrentReadDevice?.name

        addListener()
    }

    private fun addListener() {
        binding.apply {
            resultShows.movementMethod = ScrollingMovementMethod.getInstance()
            sensorRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId >= 0 && checkedId < mSensorList.size) mSensor = mSensorList[checkedId]
                else printOperationResult("checked radioButton item is error.")
            }
        }
    }

    /**
     * Obtaining the sensor list.
     *
     * @param view UI object
     */
    fun getSensorList(view: View?) {
        if (mCurrentReadDevice == null || !mCurrentReadDevice!!.isConnected) {
            printOperationResult("getSensorList: The mCurrentReadDevice status is error.")
            return
        }
        mSensorClient?.getSensorList(mCurrentReadDevice)
            ?.addOnSuccessListener(OnSuccessListener<List<Sensor>?> { sensors ->
                if (sensors == null || sensors.isEmpty()) {
                    printOperationResult("getSensorList list is null or list size is 0.")
                    return@OnSuccessListener
                }
                mSensorList.clear()
                for (item in sensors) {
                    if (Sensor.NAME_ACC == item.name || Sensor.NAME_PPG == item.name) {
                        printOperationResult("sensor name:" + item.name)
                        printOperationResult("sensor id:" + item.id)
                        mSensorList.add(item)
                    }
                }
                updateSensorListBox()
            })
            ?.addOnFailureListener { printOperationResult("getSensorList Exception.") }
    }

    /**
     * Update the sensor display box.
     */
    private fun updateSensorListBox() {
        binding.sensorRadioGroup.removeAllViews()

        for (index in 0 until mSensorList.size) {
            val sensor = mSensorList[index]
            if (index == 0) mSensor = sensor

            printOperationResult(sensor.name)
            val deviceRadioButton = RadioButton(this)
            setRadioButton(deviceRadioButton, sensor.name, index)
            binding.sensorRadioGroup.addView(deviceRadioButton)
        }
    }

    /**
     * Set the device list.
     */
    private fun setRadioButton(radioButton: RadioButton, text: String, id: Int) {
        radioButton.isChecked = id == 0
        radioButton.id = id
        radioButton.text = text
    }

    /**
     * Asynchronous reading of the sensor data.
     *
     * @param view UI object
     */
    fun asyncReadSensorData(view: View?) {
        if (checkParams()) {
            printOperationResult("asyncReadSensorData: The device or sensor object is empty, or the device is disconnected.")
            return
        }

        val sensorReadCallback = SensorReadCallback { errorCode, dataResult ->
                printOperationResult("asyncReadSensorData errorCode:$errorCode")

            if (dataResult != null && dataResult.sensor != null) {
                    printOperationResult("sensor name:" + dataResult.sensor.name)
                    var data: FloatArray?

                    when (dataResult.sensor.type) {
                        Sensor.TYPE_PPG -> {
                            val dataResults = dataResult.asList()
                            for (item in dataResults) {
                                data = item.asFloats()
                                printOperationResult("PPG data:" + Arrays.toString(data))
                            }
                        }

                        Sensor.TYPE_ACC -> {
                            data = dataResult.asFloats()
                            printOperationResult("ACC data:" + Arrays.toString(data))
                        }

                        else -> {
                            data = FloatArray(0)
                            printOperationResult("data:" + Arrays.toString(data))
                        }
                    }
                }
            }

        mSensorClient?.asyncRead(mCurrentReadDevice, mSensor, sensorReadCallback)
            ?.addOnSuccessListener { printOperationResult("asyncRead task submission success sensor name is:" + mSensor?.name) }
            ?.addOnFailureListener { printOperationResult("asyncRead task error. sensor name is:" + mSensor?.name) }
    }


    /**
     * Stop the asynchronous reading of the sensor data.
     *
     * @param view UI object
     */
    fun stopReadSensorData(view: View?) {
        if (checkParams()) {
            printOperationResult("stopReadSensorData: The device or sensor object is empty, or the device is disconnected.")
            return
        }

        val sensorStopCallback = SensorStopCallback { errorCode ->
                val result = "stopAsyncRead result:$errorCode"
                printOperationResult(result)
            }

        mSensorClient?.stopAsyncRead(mCurrentReadDevice, mSensor, sensorStopCallback)
            ?.addOnSuccessListener { printOperationResult("stopAsyncRead task submission success sensor name is:" + mSensor?.name) }
            ?.addOnFailureListener { printOperationResult("stopAsyncRead task error. sensor name is:" + mSensor?.name) }
    }

    /**
     * Check whether there are available devices or sensors for connection.
     *
     * @return true:Not available，false:available
     */
    private fun checkParams(): Boolean {
        return mSensor == null || mCurrentReadDevice == null || !mCurrentReadDevice?.isConnected!!
    }

    companion object {
        private var mSensorClient: SensorClient? = null
        private var mCurrentReadDevice: Device? = null
        private var mSensor: Sensor? = null
        private val mSensorList: MutableList<Sensor> = ArrayList()
    }
}