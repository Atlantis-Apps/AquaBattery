package com.atlantis.aquabattery

import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutActivity : AppCompatActivity() {

    private lateinit var appIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"

        // App icon
        appIcon = findViewById(R.id.appIcon)

        // Entrance animation (fade + scale)
        appIcon.scaleX = 0.85f
        appIcon.scaleY = 0.85f
        appIcon.alpha = 0f

        appIcon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(650)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                startPulse()
            }
            .start()

        // Version text
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        val versionName = packageManager
            .getPackageInfo(packageName, 0)
            .versionName

        tvVersion.text = "Version $versionName"

        // Clickable version (toast for now)
        tvVersion.setOnClickListener {
            Toast.makeText(
                this,
                "AquaBattery v$versionName",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gentle pulsing animation
    private fun startPulse() {
        appIcon.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                appIcon.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(1200)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        startPulse()
                    }
                    .start()
            }
            .start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStop() {
        super.onStop()
        // Stop animations to avoid leaks
        appIcon.animate().cancel()
    }
}