package com.sformica.benchmark.ui.activity

import android.app.NotificationManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.sformica.benchmark.R


class ShowErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_error)
    }

    override fun onResume() {
        super.onResume()

        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mBuilder = NotificationCompat.Builder(applicationContext)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(getString(R.string.error_detected))
            .setSound(soundUri) //This sets the sound to play
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, mBuilder.build())

        val animation = AnimationUtils.loadAnimation(this@ShowErrorActivity, R.anim.blink)
        animation.repeatCount = 99

        findViewById<ImageView>(R.id.imageViewError).apply {
            startAnimation(animation)
        }

        findViewById<TextView>(R.id.textViewError).apply {
            startAnimation(animation)
        }
    }
}