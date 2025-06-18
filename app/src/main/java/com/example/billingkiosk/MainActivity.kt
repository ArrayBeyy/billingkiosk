package com.example.billingkiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding

class MainActivity : Activity() {

    private lateinit var appListContainer: LinearLayout
    private lateinit var buttonSave: Button
    private lateinit var buttonLaunch: Button
    private val selectedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        // Inisialisasi DevicePolicyManager dan ComponentName
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Aktifkan LockTask Mode (Kiosk Mode) jika app adalah Device Owner
        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(compName, arrayOf(packageName, "com.whatsapp", "com.youtube.kids"))
            startLockTask()
        } else {
            Toast.makeText(this, "Aplikasi bukan Device Owner", Toast.LENGTH_LONG).show()
        }

        appListContainer = findViewById(R.id.rvApps)
        buttonSave = findViewById(R.id.btnSave)
        buttonLaunch = findViewById(R.id.btnLaunch)

        loadApps()

        buttonSave.setOnClickListener {
            val sharedPrefs = getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putStringSet("allowedApps", selectedApps).apply()
            Toast.makeText(this, "Aplikasi berhasil disimpan", Toast.LENGTH_SHORT).show()
        }

        buttonLaunch.setOnClickListener {
            val sharedPrefs = getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)
            val allowedApps = sharedPrefs.getStringSet("allowedApps", emptySet()) ?: emptySet()
            allowedApps.forEach { pkg ->
                launchAppInKiosk(pkg)
            }
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)
        val sharedPrefs = getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)
        val savedApps = sharedPrefs.getStringSet("allowedApps", emptySet())
        selectedApps.clear()
        savedApps?.let { selectedApps.addAll(it) }

        for (resolveInfo in apps) {
            val label = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName

            if (packageName == applicationContext.packageName) continue

            val checkBox = CheckBox(this).apply {
                text = label
                isChecked = savedApps?.contains(packageName) == true
                setPadding(16)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedApps.add(packageName)
                    } else {
                        selectedApps.remove(packageName)
                    }
                }
            }

            appListContainer.addView(checkBox)
        }
    }

    private fun launchAppInKiosk(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
            startLockTask()
        } else {
            Toast.makeText(this, "Gagal membuka aplikasi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Tombol kembali dinonaktifkan", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()

        // Jika aplikasi ke background, coba kembalikan ke foreground
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningTasks = activityManager.getRunningTasks(1)

        if (runningTasks.isNotEmpty()) {
            val topActivity = runningTasks[0].topActivity
            if (topActivity?.packageName != packageName) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onUserLeaveHint() {
        // Cegah recent apps
        moveTaskToFront()
    }

    private fun moveTaskToFront() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        am.moveTaskToFront(taskId, 0)
    }
}
