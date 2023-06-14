package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationManager: NotificationManager

    private var downloadID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Setup notification channel
        createChannel()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.contentMain.customButton.setOnClickListener {
            download(binding.contentMain.radioUrl.checkedRadioButtonId)
        }
    }

    /**
     * Send notification on download complete
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Filter known download id
            if (id == downloadID) {
                // Post notification on status bar when download is completed
                notificationManager = getSystemService(
                    NotificationManager::class.java
                )
                notificationManager.sendNotification(context!!, downloadID)
            }

            // Stop animation when download completed
            binding.contentMain.customButton.buttonState = ButtonState.Completed
        }
    }

    /**
     * Begin download target file base on selected radio button
     */
    private fun download(position: Int) {

        if (position == -1) {
            // Show information toast to make user select at least one file to download
            Toast.makeText(this, R.string.notification_no_select, Toast.LENGTH_LONG).show()
            // Stop animation
            binding.contentMain.customButton.buttonState = ButtonState.Completed
            return
        }

        val url = when (position) {
            R.id.radio_glide -> URL_GLIDE
            R.id.radio_loadapp -> URL_LOADAPP
            R.id.radio_retrofit -> URL_RETROFIT
            else -> return
        }

        val description = when (position) {
            R.id.radio_glide -> getString(R.string.radio_glide)
            R.id.radio_loadapp -> getString(R.string.radio_loadapp)
            R.id.radio_retrofit -> getString(R.string.radio_retrofit)
            else -> return
        }

        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(description)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    /**
     * Create notification channel for our app
     * Currently only show title on status bar, disabled app badge icon
     */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                //  Disable badge on app icon
                setShowBadge(false)
            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.colorAccent
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.app_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL_GLIDE = "https://github.com/bumptech/glide"
        private const val URL_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL_RETROFIT = "https://github.com/square/retrofit"
    }
}
