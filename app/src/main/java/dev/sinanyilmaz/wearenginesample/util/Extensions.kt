package dev.sinanyilmaz.wearenginesample.util

import android.Manifest
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import dev.sinanyilmaz.wearenginesample.MainActivity
import dev.sinanyilmaz.wearenginesample.MainActivity.Companion.PERMISSIONS_STORAGE
import dev.sinanyilmaz.wearenginesample.MainActivity.Companion.REQUEST_EXTERNAL_STORAGE
import dev.sinanyilmaz.wearenginesample.NotificationActivity
import dev.sinanyilmaz.wearenginesample.SensorActivity


private const val SCROLL_HIGH = 50


fun MainActivity.verifyStoragePermissions() {
    val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    if (permission != PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
}

fun MainActivity.printOperationResult(string: String) {
    Log.i("PrintOperationResultTAG", string)

    runOnUiThread {
        binding.apply {
            logOutputTextView.append(string + System.lineSeparator())
            val offset: Int = logOutputTextView.lineCount * logOutputTextView.lineHeight
            if (offset > logOutputTextView.height) {
                logOutputTextView.scrollTo(
                    0,
                    offset - logOutputTextView.height + SCROLL_HIGH
                )
            }
        }
    }
}

/**
 * Clear the result printing box.
 *
 * @param view UI object
 */
fun MainActivity.clearOutputTextView(view: View?) {
    binding.logOutputTextView.text = ""
    binding.logOutputTextView.scrollTo(0, 0)
}

/**
 * Check whether the target app package name is set.
 *
 * @return true/false Return whether the target app package name is set
 */
fun MainActivity.checkPackageName(): Boolean {
    if (TextUtils.isEmpty(peerPkgName)) {
        printOperationResult("please input target app packageName!")
        return false
    }
    return true
}

fun SensorActivity.printOperationResult(string: String) {
    Log.i("PrintOperationResultTAG", string)

    runOnUiThread {
        binding.apply {
            resultShows.append(string + System.lineSeparator())
            val offset: Int = resultShows.lineCount * resultShows.lineHeight
            if (offset > resultShows.height) {
                resultShows.scrollTo(0, offset - resultShows.height + SCROLL_HIGH)
            }
        }
    }
}

fun NotificationActivity.printOperationResult(string: String) {
    Log.i("PrintOperationResultTAG", string)

    runOnUiThread {
        binding.apply {
            resultShows.append(string + System.lineSeparator())
            val offset: Int = resultShows.lineCount * resultShows.lineHeight
            if (offset > resultShows.height) {
                resultShows.scrollTo(0, offset - resultShows.height + SCROLL_HIGH)
            }
        }
    }
}