package com.example.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusShieldService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var monitorJob: Job? = null
    private var overlayManager: SystemBlockOverlayManager? = null

    companion object {
        const val CHANNEL_SHIELD_ID = "focus_shield_service_channel"
        const val NOTIFICATION_SHIELD_ID = 3003
        var isServiceRunning = false

        fun getDiscoverableApps(context: Context): List<DeviceAppInfo> {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val launchableResolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val launchablePackages = launchableResolveInfos.map { it.activityInfo.packageName }.toSet()

            val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            return apps.filter {
                launchablePackages.contains(it.packageName) || it.packageName == context.packageName
            }.map {
                DeviceAppInfo(
                    packageName = it.packageName,
                    appName = pm.getApplicationLabel(it).toString()
                )
            }.distinctBy { it.packageName }
             .sortedBy { it.appName }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        overlayManager = SystemBlockOverlayManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_SHIELD_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_SHIELD_ID, notification)
        }

        // Start checking foreground app
        startMonitoring()

        return START_STICKY
    }

    private fun getDailyUsageTimeMs(context: Context, packageName: String): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        try {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            if (!stats.isNullOrEmpty()) {
                val appStats = stats.find { it.packageName == packageName }
                if (appStats != null) {
                    return appStats.totalTimeInForeground
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch(Dispatchers.Default) {
            val database = FocusDatabase.getDatabase(applicationContext)
            val blockedAppDao = database.blockedAppDao()

            while (isActive) {
                val prefs = applicationContext.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                val isTimerRunning = prefs.getBoolean("is_timer_running_for_service", false)
                val isShieldActive = prefs.getBoolean("is_shield_active_for_service", true)

                var needsOverlayToShow = false
                var blockedAppName = ""
                var blockedPackageName = ""

                if (isShieldActive) {
                    val foregroundPackage = getForegroundPackage(applicationContext)
                    if (!foregroundPackage.isNullOrEmpty() && foregroundPackage != packageName) {
                        val essentialItemDao = database.essentialItemDao()
                        val isEssentialApp = essentialItemDao.getEssentialItemByIdentifier(foregroundPackage) != null
                        
                        // Whitelist browsers if there are any essential websites whitelisted
                        val browsers = listOf(
                            "com.android.chrome", "org.mozilla.firefox", 
                            "com.microsoft.emmx", "com.opera.browser", 
                            "com.sec.android.app.sbrowser"
                        )
                        val isBrowser = browsers.contains(foregroundPackage)
                        val hasWebsitesAllowed = isBrowser && (essentialItemDao.getAnyEssentialWebsite() != null)

                        // Check database for active blocked app config
                        val blockedApps = blockedAppDao.getBlockedAppsByPackageName(foregroundPackage)
                        val activeBlocks = blockedApps.filter { it.isBlocked }
                        val isExplicitlyBlocked = activeBlocks.isNotEmpty()

                        val isEssential = (isEssentialApp || hasWebsitesAllowed) && !isExplicitlyBlocked

                        if (!isEssential && isExplicitlyBlocked) {
                            val shouldBlock = activeBlocks.any { blockedApp ->
                                when {
                                    blockedApp.blockMode == "Always block" -> true
                                    blockedApp.blockMode == "Session only" -> isTimerRunning
                                    blockedApp.blockMode == "Scheduled" -> true
                                    blockedApp.blockMode.startsWith("Limit:") -> {
                                        val limitMinutes = when (blockedApp.blockMode) {
                                            "Limit: 15m/day" -> 15
                                            "Limit: 30m/day" -> 30
                                            "Limit: 1h/day" -> 60
                                            else -> 0
                                        }
                                        if (limitMinutes > 0) {
                                            val dailyTimeMs = getDailyUsageTimeMs(applicationContext, foregroundPackage)
                                            val dailyTimeMins = dailyTimeMs / (1000 * 60)
                                            dailyTimeMins >= limitMinutes
                                        } else {
                                            false
                                        }
                                    }
                                    else -> isTimerRunning
                                }
                            }

                            if (shouldBlock) {
                                needsOverlayToShow = true
                                blockedAppName = activeBlocks.first().appName
                                blockedPackageName = foregroundPackage
                            }
                        }
                    }
                }

                // Apply overlay state on Main dispatcher
                serviceScope.launch(Dispatchers.Main) {
                    if (needsOverlayToShow) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(applicationContext)) {
                            overlayManager?.showOverlay(blockedAppName, blockedPackageName)
                        } else {
                            // Fallback: trigger MainActivity overlay
                            val blockIntent = Intent(applicationContext, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("blocked_app_triggered", true)
                                putExtra("blocked_package_name", blockedPackageName)
                                putExtra("blocked_app_name", blockedAppName)
                            }
                            applicationContext.startActivity(blockIntent)
                        }
                    } else {
                        overlayManager?.removeOverlay()
                    }
                }

                delay(1500) // check every 1.5 seconds
            }
        }
    }

    private fun getForegroundPackage(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // 10 seconds ago
        
        try {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            var foregroundApp: String? = null
            val event = android.app.usage.UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundApp = event.packageName
                }
            }
            if (!foregroundApp.isNullOrEmpty()) {
                return foregroundApp
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to queryUsageStats if queryEvents did not return a result
        try {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            if (!stats.isNullOrEmpty()) {
                val sorted = stats.sortedByDescending { it.lastTimeUsed }
                return sorted.firstOrNull()?.packageName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_SHIELD_ID,
                "Focus Shield Active Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running in background to block restricted apps during sessions."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_SHIELD_ID)
            .setContentTitle("Focus Shield Guard is Active 🛡️")
            .setContentText("Monitoring and protecting your current workspace.")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        overlayManager?.removeOverlay()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
