package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    // Initialize variable
    private var downloadStatus = DownloadManager.STATUS_FAILED
    private var downloadDescription = ""

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Clear all previously posted notifications
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.cancelAll()

        // Initialize binding
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contentDetail.buttonOk.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Get download result from @DownloadManager
        if (intent != null) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            getDownloadResult(downloadId)
        }
    }

    override fun onResume() {
        super.onResume()

        // Download target file name
        binding.contentDetail.textViewFilename.text = downloadDescription
        // Download status
        binding.contentDetail.textViewStatus.text = when (downloadStatus) {
            DownloadManager.STATUS_FAILED -> getString(R.string.download_status_failed)
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.download_status_success)
            else -> ""
        }
    }

    @SuppressLint("Range")
    private fun getDownloadResult(downloadId: Long) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            if (cursor.count > 0) {
                // Download status
                downloadStatus =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                // Download target file name
                downloadDescription =
                    cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION))
            }
        }
        cursor.close()
    }
}