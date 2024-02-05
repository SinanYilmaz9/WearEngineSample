package dev.sinanyilmaz.wearenginesample

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.device.DeviceClient
import com.huawei.wearengine.monitor.MonitorClient
import com.huawei.wearengine.monitor.MonitorItem
import com.huawei.wearengine.monitor.MonitorListener
import com.huawei.wearengine.p2p.Message
import com.huawei.wearengine.p2p.P2pClient
import com.huawei.wearengine.p2p.Receiver
import com.huawei.wearengine.p2p.SendCallback
import dev.sinanyilmaz.wearenginesample.databinding.ActivityMainBinding
import dev.sinanyilmaz.wearenginesample.util.SelectFileManager
import dev.sinanyilmaz.wearenginesample.util.WearEngineTextWatcher
import dev.sinanyilmaz.wearenginesample.util.checkPackageName
import dev.sinanyilmaz.wearenginesample.util.printOperationResult
import dev.sinanyilmaz.wearenginesample.util.verifyStoragePermissions
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var p2pClient: P2pClient? = null
    private var monitorClient: MonitorClient? = null
    private var deviceClient: DeviceClient? = null

    private var selectedDevice: Device? = null
    private val deviceList: MutableList<Device> = ArrayList()
    var peerPkgName: String? = null
    private val deviceMap: MutableMap<String, Device> = HashMap()
    private var sendMessage: Message? = null
    private val monitorItemType = MonitorItem.MONITOR_ITEM_CONNECTION

    private val getTime = Calendar.getInstance().time.toString()

    private var index = 0

    private val receiver = Receiver { message ->
        message?.let {
            val data = it.data.toString()
            printOperationResult("ReceiveMessage is:$data")
        } ?: kotlin.run {
            printOperationResult("Receiver Message is null")
        }
    }

    private var monitorListener = MonitorListener { resultCode, monitorItem, monitorData ->
            if (monitorData != null && monitorItem != null) {
                printOperationResult(
                    ("MonitorListener result is: resultCode:" + resultCode + "string data[" + monitorData.asString()
                            + MONITOR_INT_DATA + monitorData.asInt() + MONITOR_BOOLEAN_DATA + monitorData.asBool()) + " ]"
                )
            }
            else printOperationResult("monitorItem is null or monitorData is null!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        addViewListener()
        verifyStoragePermissions()
    }

    /**
     * Initialization: Obtain the authorization.
     */
    private fun initData() {
        val authCallback: AuthCallback = object : AuthCallback {
            override fun onOk(permissions: Array<Permission>) {
                Log.d(TAG, "getAuthClient onOk")
            }

            override fun onCancel() {
                Log.e(TAG, "getAuthClient onCancel")
            }
        }

        HiWear.getAuthClient(this)
            .requestPermission(authCallback, Permission.DEVICE_MANAGER, Permission.NOTIFY, Permission.SENSOR)
            .addOnSuccessListener { Log.d(TAG, "getAuthClient onSuccess") }
            .addOnFailureListener { Log.d(TAG, "getAuthClient onFailure") }

        p2pClient = HiWear.getP2pClient(this)
        deviceClient = HiWear.getDeviceClient(this)
        monitorClient = HiWear.getMonitorClient(this)
    }

    /**
     * Add a view listener.
     */
    private fun addViewListener() {
        binding.apply {
            logOutputTextView.movementMethod = ScrollingMovementMethod.getInstance()
            deviceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                Log.d(TAG, "onCheckedChanged:$checkedId")
                selectedDevice = deviceList[checkedId]
            }

            peerPkgNameEditText.addTextChangedListener(object : WearEngineTextWatcher() {
                override fun afterTextChanged(editable: Editable?) {
                    Log.d(TAG, "After package text changed$editable")
                    editable?.let { setPeerPkgName(it) }
                }
            })
        }
    }

    /**
     * Set the name and fingerprint of the device app package specified by the third-party app for communication.
     *
     */
    private fun setPeerPkgName(editable: Editable) {
        peerPkgName = editable.toString().trim { it <= ' ' }
        p2pClient!!.setPeerPkgName(peerPkgName)

        // You need to set the fingerprint information of the target application.
        p2pClient?.setPeerFingerPrint("")
    }

    /**
     * Check whether the permission is granted.
     *
     * @param view UI object
     */
    fun checkPermission(view: View?) {
        HiWear.getAuthClient(this)
            .checkPermission(Permission.DEVICE_MANAGER).addOnSuccessListener { isResult ->
                printOperationResult("checkPermission task onSuccess! device_manager permission is = $isResult")
            }.addOnFailureListener { printOperationResult("checkPermission task submission error") }
    }

    fun checkPermissions(view: View?) {
        val permissions: Array<Permission> = arrayOf(Permission.DEVICE_MANAGER)

        HiWear.getAuthClient(this)
            .checkPermissions(permissions).addOnSuccessListener { booleans ->
                printOperationResult("checkPermission task onSuccess!")
                if (booleans.isNotEmpty())
                    printOperationResult("device_manager permission is = " + booleans[0])
            }
            .addOnFailureListener { printOperationResult("checkPermissions task submission error") }
    }

    /**
     * Ping the paired device.
     *
     * @param view UI object
     */
    fun pingBoundDevices(view: View?) {
        if (!checkDevice() || !checkPackageName()) {
            return
        }
        p2pClient!!.ping(selectedDevice) { result ->
            printOperationResult(
                ((getTime + STRING_PING) + selectedDevice!!.name + DEVICE_NAME_OF)
                        + peerPkgName + STRING_RESULT + result
            )
        }
            .addOnSuccessListener { printOperationResult(STRING_PING + selectedDevice!!.name + DEVICE_NAME_OF + peerPkgName + SUCCESS) }
            .addOnFailureListener { printOperationResult(STRING_PING + selectedDevice!!.name + DEVICE_NAME_OF + peerPkgName + FAILURE) }
    }


    /**
     * Obtain the paired device list.
     *
     * @param view UI object
     */
    fun getBoundDevices(view: View?) {
        deviceClient?.bondedDevices?.addOnSuccessListener(OnSuccessListener<List<Device?>?> { devices ->
            if (devices == null || devices.isEmpty()) {
                printOperationResult("getBondedDevices list is null or list size is 0")
                return@OnSuccessListener
            }
            printOperationResult("getBondedDevices onSuccess! devices list size = " + devices.size)
            updateDeviceList(devices)
        })?.addOnFailureListener { printOperationResult("getBondedDevices task submission error") }
    }

    /**
     * Send a message to the app with the specified package name on the specified device.
     *
     * @param view UI object
     */
    fun sendMessage(view: View?) {
        if (!checkDevice() || !checkPackageName()) return

        // Build the request param message
        val sendMessageStr: String = binding.messageEditText.text.toString()

        if (sendMessageStr.isNotEmpty()) {
            val builder: Message.Builder = Message.Builder()

            try {
                builder.setPayload(sendMessageStr.toByteArray(charset("UTF-8")))
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "set sendMessageStr UnsupportedEncodingException")
            }
            sendMessage = builder.build()
        }
        if (sendMessage == null || sendMessage?.data?.size == 0) {
            printOperationResult("please input message for send!")
            return
        }
        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(resultCode: Int) {
                printOperationResult(
                    ((getTime + SEND_MESSAGE_TO) + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + STRING_RESULT + resultCode
                )
            }

            override fun onSendProgress(progress: Long) {
                printOperationResult(
                    (((getTime + SEND_MESSAGE_TO) + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + " progress:" + progress)
                )
            }
        }
        p2pClient?.send(selectedDevice, sendMessage, sendCallback)
            ?.addOnSuccessListener {
                printOperationResult((SEND_MESSAGE_TO + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + SUCCESS)
            }?.addOnFailureListener {
                printOperationResult((SEND_MESSAGE_TO + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + FAILURE)
            }
    }

    /**
     * Select a file to send.
     *
     * @param view UI object
     */
    fun selectFileAndSend(view: View?) {
        if (!checkDevice() || !checkPackageName()) return

        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, SELECT_FILE_CODE)
        } catch (e: ActivityNotFoundException) {
            binding.logOutputTextView.append("ActivityNotFoundException" + System.lineSeparator())
        }
    }

    /**
     * Send the file.
     *
     * @param sendFilePath File path
     */
    private fun sendFile(sendFilePath: String?) {
        val sendFile = File(sendFilePath)
        val builder = Message.Builder()
        builder.setPayload(sendFile)
        val fileMessage = builder.build()
        p2pClient!!.send(selectedDevice, fileMessage, object : SendCallback {
            override fun onSendResult(resultCode: Int) {
                printOperationResult(
                    ((getTime + SEND_MESSAGE_TO) + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + STRING_RESULT + resultCode
                )
            }

            override fun onSendProgress(progress: Long) {
                printOperationResult(
                    (((getTime + SEND_MESSAGE_TO) + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + " progress:" + progress)
                )
            }
        }).addOnSuccessListener {
            printOperationResult(
                (SEND_MESSAGE_TO + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + SUCCESS
            )
        }.addOnFailureListener {
            printOperationResult(
                (SEND_MESSAGE_TO + selectedDevice!!.name + DEVICE_NAME_OF) + peerPkgName + FAILURE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK) {
            if (data == null) {
                Log.e(TAG, "Invalid file data")
                return
            }
            val selectFileUri = data.data
            val selectFilePath = SelectFileManager.getFilePath(this, selectFileUri)
            sendFile(selectFilePath)
        }
    }

    /**
     * Register a specified app on a specified device.
     *
     */
    fun receiveMessage(view: View?) {
        if (!checkDevice() || !checkPackageName()) {
            return
        }

        val receiverPid = Process.myPid()
        val receiverHashCode = System.identityHashCode(receiver)
        Log.d(TAG, "receiveMessageButtonOnClick receiver pid is:$receiverPid$HASH_CODE$receiverHashCode")

        p2pClient?.registerReceiver(selectedDevice, receiver)?.addOnSuccessListener {
            printOperationResult("register receiver listener$SUCCESS")
        }?.addOnFailureListener { printOperationResult("register receiver listener$FAILURE") }
    }

    /**
     * Deregister a registered app.
     *
     * @param view UI object
     */
    fun cancelReceiveMessage(view: View?) {
        val receiverPid = Process.myPid()
        val receiverHashCode = System.identityHashCode(receiver)
        Log.d(
            TAG,
            "cancelReceiveMessage receiver pid is:$receiverPid$HASH_CODE$receiverHashCode"
        )
        p2pClient?.unregisterReceiver(receiver)
            ?.addOnSuccessListener { printOperationResult("cancel receive message$SUCCESS") }
            ?.addOnFailureListener { printOperationResult("cancel receive message$FAILURE") }
    }

    /**
     * Register a listening object on the specified device to listen for the Bluetooth connection status.
     *
     * @param view UI object
     */
    fun registerEventStatus(view: View?) {
        if (!checkDevice()) {
            return
        }
        monitorListener = MonitorListener { _, _, monitorData ->
                val result = ("ReceiveMonitorMessage is: string data[" + monitorData.asString() + MONITOR_INT_DATA
                            + monitorData.asInt() + MONITOR_BOOLEAN_DATA + monitorData.asBool()) + "]"
                printOperationResult(result)
            }

        val receiverPid = Process.myPid()
        val receiverHashCode = System.identityHashCode(monitorListener)
        Log.d(TAG, "registerEventStatus receiver pid is:$receiverPid$HASH_CODE$receiverHashCode")

        monitorClient?.register(selectedDevice, monitorItemType, monitorListener)
            ?.addOnSuccessListener { printOperationResult("register status event listener $SUCCESS") }
            ?.addOnFailureListener { printOperationResult("register status event listener $FAILURE") }
    }

    /**
     * Deregister a registered app.
     *
     * @param view UI object
     */
    fun unRegisterEventStatus(view: View?) {
        val receiverPid = Process.myPid()
        val receiverHashCode = System.identityHashCode(monitorListener)
        Log.d(TAG, "cancelReceiveMessage receiver pid is:$receiverPid$HASH_CODE$receiverHashCode")

        monitorClient?.unregister(monitorListener)
            ?.addOnSuccessListener { printOperationResult("cancel register status event listener $SUCCESS") }
            ?.addOnFailureListener { printOperationResult("cancel register status event listener $FAILURE") }
    }


    /**
     * Update the device display box.
     *
     * @param devices Device list
     */
    private fun updateDeviceList(devices: List<Device?>?) {
        for (device in devices!!) {
            printOperationResult("device Name: " + device?.name)
            printOperationResult("device connect status:" + device?.isConnected)
            if (deviceMap.containsKey(device?.uuid)) {
                continue
            }
            deviceList.add(device!!)
            deviceMap[device.uuid] = device
            val deviceRadioButton = RadioButton(this)
            setRadioButton(deviceRadioButton, device.name, index)
            binding.deviceRadioGroup.addView(deviceRadioButton)
            index++
        }
    }

    /**
     * Set the device list.
     */
    private fun setRadioButton(radioButton: RadioButton, text: String, id: Int) {
        radioButton.isChecked = false
        radioButton.id = id
        radioButton.text = text
    }

    /**
     * Jump to the sensor page.
     *
     * @param view UI object
     */
    fun startSensor(view: View?) {
        if (isDeviceNotConnected()) {
            printOperationResult("No available device or the device has not been connected.")
            return
        }
        val intent = Intent(this, SensorActivity::class.java)
        intent.putExtra("currentDevice", selectedDevice)
        startActivity(intent)
    }

    /**
     * Jump to the notification page.
     *
     * @param view UI object
     */
    fun startNotify(view: View?) {
        if (isDeviceNotConnected()) {
            printOperationResult("No available device or the device has not been connected.")
            return
        }
        val intent = Intent(this, NotificationActivity::class.java)
        intent.putExtra("currentDevice", selectedDevice)
        intent.putExtra("packageName", peerPkgName)
        startActivity(intent)
    }

    /**
     * Check whether the device is connected.
     *
     * @return true:not connected,false:connected
     */
    private fun isDeviceNotConnected(): Boolean {
        val isConnected = selectedDevice != null && selectedDevice!!.isConnected
        return !isConnected
    }

    /**
     * Check whether the target device is selected.
     *
     * @return true/false Return whether the target device is selected.
     */
    private fun checkDevice(): Boolean {
        if (selectedDevice == null) {
            printOperationResult("please select the target device!")
            return false
        }
        return true
    }


    companion object {
        private const val TAG = "WearEngine_MainActivity"
        private const val DEVICE_NAME_OF = "'s "
        private const val SEND_MESSAGE_TO = "Send message to "
        private const val FAILURE = " task failure"
        private const val SUCCESS = " task success"
        private const val STRING_RESULT = " result:"
        private const val STRING_PING = " Ping "
        private const val MONITOR_INT_DATA = "], int data["
        private const val MONITOR_BOOLEAN_DATA = "], boolean data["
        private const val HASH_CODE = " , hashcode is: "

        private const val SELECT_FILE_CODE = 1

        const val REQUEST_EXTERNAL_STORAGE = 1
        val PERMISSIONS_STORAGE = arrayOf(READ_EXTERNAL_STORAGE)
    }
}