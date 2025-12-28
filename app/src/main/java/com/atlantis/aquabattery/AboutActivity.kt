package com.atlantis.aquabattery

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"

        // Version text
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        tvVersion.text = "Version " + packageManager
        .getPackageInfo(packageName, 0)
        .versionName
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}