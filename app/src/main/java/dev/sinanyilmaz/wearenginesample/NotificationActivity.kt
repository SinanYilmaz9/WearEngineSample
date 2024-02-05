package dev.sinanyilmaz.wearenginesample

import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.notify.Action
import com.huawei.wearengine.notify.Notification
import com.huawei.wearengine.notify.NotificationConstants
import com.huawei.wearengine.notify.NotificationTemplate
import com.huawei.wearengine.notify.NotifyClient
import dev.sinanyilmaz.wearenginesample.databinding.ActivityNotificationBinding
import dev.sinanyilmaz.wearenginesample.util.printOperationResult


class NotificationActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationBinding

    private var checkedDevice: Device? = null
    private var mNotifyClient: NotifyClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
    }

    private fun initData() {
        checkedDevice = intent.getParcelableExtra<Parcelable>("currentDevice") as Device?
        val packageName = intent.getStringExtra("packageName")
        if (packageName != null) binding.editNotifyPackageName.setText(packageName)

        if (checkedDevice != null && checkedDevice?.name != null) binding.textNotifyDevice.text = checkedDevice?.name
        else {
            finish()
            return
        }
        mNotifyClient = HiWear.getNotifyClient(this@NotificationActivity)
    }

    /**
     * Send notification.
     *
     * @param view UI object
     */
    fun sendNotification(view: View?) {
        val builder: Notification.Builder = Notification.Builder()

        val packageName: String = binding.editNotifyPackageName.text.toString().trim()
        if (TextUtils.isEmpty(packageName)) printOperationResult("please input packageName for send!")
        builder.setPackageName(packageName)

        val title: String = binding.editNotifyTitle.text.toString().trim()
        if (TextUtils.isEmpty(title)) printOperationResult("please input title for send!")
        builder.setTitle(title)

        val text: String = binding.editNotifyContent.text.toString().trim()
        if (TextUtils.isEmpty(text)) printOperationResult("please input notification content for send!")
        builder.setText(text)

        setButtonContentAndTemplate(builder)

        val action: Action = getNotificationAction()
        builder.setAction(action)

        val notification: Notification = builder.build()
        mNotifyClient?.notify(checkedDevice, notification)
            ?.addOnSuccessListener {
                printOperationResult("send notification success.")
            }
            ?.addOnFailureListener {
                printOperationResult("send notification fail, exception.")
            }
    }

    /**
     * Create the Action object of the Notification.
     *
     * @return Action Object
     */
    private fun getNotificationAction(): Action {
        return object : Action {
            override fun onResult(notification: Notification, feedback: Int) {
                val result = "getNotificationAction feedback:" + feedback + ",notification title:" + notification.title
                printOperationResult(result)
            }

            override fun onError(notification: Notification, errorCode: Int, errorMsg: String) {
                val result = "getNotificationAction onError errorCode:$errorCode,errorMsg:$errorMsg"
                printOperationResult(result)
            }
        }
    }

    /**
     * Set the number and content of notification buttons.
     *
     * @param builder Construct the Builder object of the Notification object.
     */
    private fun setButtonContentAndTemplate(builder: Notification.Builder) {
        val buttonContents = HashMap<Int, String>()

        val templateId: String = binding.editNotifyTemplate.text.toString().trim()
        val button1Text: String = binding.editNotifyButtonOne.text.toString().trim()
        val button2Text: String = binding.editNotifyButtonTwo.text.toString().trim()
        val button3Text: String = binding.editNotifyButtonThree.text.toString().trim()

        when (templateId) {
            NOTIFY_TEMPLATE_NO_BUTTON -> builder.setTemplateId(NotificationTemplate.NOTIFICATION_TEMPLATE_NO_BUTTON)
            NOTIFY_TEMPLATE_ONE_BUTTON -> {
                if (TextUtils.isEmpty(button1Text))
                    printOperationResult("please input button1Text for send!")

                builder.setTemplateId(NotificationTemplate.NOTIFICATION_TEMPLATE_ONE_BUTTON)
                buttonContents[NotificationConstants.BUTTON_ONE_CONTENT_KEY] = button1Text
            }

            NOTIFY_TEMPLATE_TWO_BUTTON -> {
                if (TextUtils.isEmpty(button1Text) || TextUtils.isEmpty(button2Text))
                    printOperationResult("please input button1Text and button2Text for send!")

                builder.setTemplateId(NotificationTemplate.NOTIFICATION_TEMPLATE_TWO_BUTTONS)
                buttonContents[NotificationConstants.BUTTON_ONE_CONTENT_KEY] = button1Text
                buttonContents[NotificationConstants.BUTTON_TWO_CONTENT_KEY] = button2Text
            }

            NOTIFY_TEMPLATE_THREE_BUTTON -> {
                if (TextUtils.isEmpty(button1Text) || TextUtils.isEmpty(button2Text) || TextUtils.isEmpty(button3Text))
                    printOperationResult("please input button1Text and button2Text and button3Text for send!")

                builder.setTemplateId(NotificationTemplate.NOTIFICATION_TEMPLATE_THREE_BUTTONS)
                buttonContents[NotificationConstants.BUTTON_ONE_CONTENT_KEY] = button1Text
                buttonContents[NotificationConstants.BUTTON_TWO_CONTENT_KEY] = button2Text
                buttonContents[NotificationConstants.BUTTON_THREE_CONTENT_KEY] = button3Text
            }

            else -> builder.setTemplateId(NotificationTemplate.NOTIFICATION_TEMPLATE_NO_BUTTON)
        }

        if (buttonContents.isNotEmpty()) builder.setButtonContents(buttonContents)
    }

    companion object {
        private const val NOTIFY_TEMPLATE_NO_BUTTON = "50"
        private const val NOTIFY_TEMPLATE_ONE_BUTTON = "51"
        private const val NOTIFY_TEMPLATE_TWO_BUTTON = "52"
        private const val NOTIFY_TEMPLATE_THREE_BUTTON = "53"
    }
}