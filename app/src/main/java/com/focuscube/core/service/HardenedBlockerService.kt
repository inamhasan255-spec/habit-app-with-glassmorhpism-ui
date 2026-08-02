package com.focuscube.core.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focuscube.core.datastore.BlockPreferences
import com.focuscube.data.local.FocusCubeDatabase
import com.focuscube.data.model.TimeCushionCalculator
import com.focuscube.data.model.UserSchedule
import com.focuscube.data.model.TimeCushionEngine
import com.focuscube.data.model.BlockState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

import com.focuscube.core.intercept.TokenManager
import com.focuscube.core.intercept.FlashcardInterceptActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

class HardenedBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var blockPreferences: BlockPreferences
    private lateinit var database: FocusCubeDatabase
    private lateinit var tokenManager: TokenManager

    private var isSessionLocked = false
    private var restrictedPackages = emptySet<String>()
    private var hasCushionDeficit = false
    private var currentEngineState = BlockState.NORMAL
    
    private var activeExpulsionJob: Job? = null
    private var currentRestrictedPackageAllowed: String? = null
    
    private val ejectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.focuscube.ACTION_EJECT") {
                performGlobalAction(GLOBAL_ACTION_BACK)
                
                // Send back to home screen to be sure
                val fallbackHome = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(fallbackHome)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        blockPreferences = BlockPreferences(applicationContext)
        database = FocusCubeDatabase.getDatabase(applicationContext)
        tokenManager = TokenManager.getInstance(applicationContext)
        
        androidx.core.content.ContextCompat.registerReceiver(
            applicationContext,
            ejectionReceiver,
            IntentFilter("com.focuscube.ACTION_EJECT"),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Monitor preferences and assignments to check for locks and deficits
        serviceScope.launch {
            combine(
                blockPreferences.isSessionLocked,
                blockPreferences.restrictedPackages,
                database.assignmentDao().getAllAssignmentsFlow(),
                TimeCushionEngine.blockStateFlow
            ) { locked, packages, assignments, engineState ->
                val cushions = TimeCushionCalculator.calculateTimeCushions(
                    assignments = assignments,
                    schedule = UserSchedule(),
                    now = Date()
                )
                val activeDeficit = cushions.any { it < 0.0 }
                // Re-trigger recalculation in case assignments or schedules changed
                val latestDiskState = TimeCushionEngine.readBlockStateFromDisk(applicationContext)
                val isStrictLocked = (engineState == BlockState.STRICT_LOCK || latestDiskState == BlockState.STRICT_LOCK)
                Quadruple(locked, packages, activeDeficit, isStrictLocked)
            }.collect { (locked, packages, deficit, strictLocked) ->
                isSessionLocked = locked
                restrictedPackages = packages
                hasCushionDeficit = deficit || strictLocked
                currentEngineState = if (strictLocked) BlockState.STRICT_LOCK else BlockState.NORMAL
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 50
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val activePackage = event.packageName?.toString() ?: return

        // Use cached state instead of disk read to ensure < 15ms latency
        val isStrictLockActive = currentEngineState == BlockState.STRICT_LOCK

        // Intercept when locked OR when there is an active Time Cushion deficit / strict lock
        if (isSessionLocked || hasCushionDeficit || isStrictLockActive) {

            
            // 1. Settings Bypass prevention (Uninstall / Force Stop / Device Admin block)
            if (activePackage == "com.android.settings") {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    if (containsBypassOrUninstallTerms(rootNode)) {
                        preventBypass()
                        rootNode.recycle()
                        return
                    }
                    rootNode.recycle()
                }

                // Check active interactive windows (anti-PiP and anti-split-screen bypasses)
                val activeWindows = windows
                for (window in activeWindows) {
                    val windowRoot = window.root
                    if (windowRoot != null) {
                        if (containsBypassOrUninstallTerms(windowRoot)) {
                            preventBypass()
                            windowRoot.recycle()
                            break
                        }
                        windowRoot.recycle()
                    }
                }
            }

            // 2. Restricted Packages block (e.g. Social apps during focus sessions or deficit blocks)
            if (restrictedPackages.contains(activePackage)) {
                handleRestrictedAppIntercept(activePackage)
                return
            }

            // Dual validation: Evaluate windows collection to check if a restricted app is in split screen or PiP
            val activeWindows = windows
            for (window in activeWindows) {
                val windowRoot = window.root
                if (windowRoot != null) {
                    val rootPackage = windowRoot.packageName?.toString() ?: ""
                    if (restrictedPackages.contains(rootPackage)) {
                        handleRestrictedAppIntercept(rootPackage)
                        windowRoot.recycle()
                        break
                    }
                    windowRoot.recycle()
                }
            }
        }
    }

    private fun containsBypassOrUninstallTerms(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""

        // Strict validation terms for system settings / app detail sub-bypasses
        if (text.contains("focus cube") || 
            text.contains("force stop") || 
            text.contains("uninstall") || 
            text.contains("device admin") ||
            text.contains("deactivate") ||
            text.contains("clear data") ||
            desc.contains("force stop") || 
            desc.contains("uninstall") ||
            desc.contains("deactivate") ||
            desc.contains("clear data")) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (containsBypassOrUninstallTerms(child)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    private fun preventBypass() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun handleRestrictedAppIntercept(targetPackage: String) {
        if (tokenManager.hasValidToken()) {
            if (currentRestrictedPackageAllowed != targetPackage || activeExpulsionJob?.isActive != true) {
                currentRestrictedPackageAllowed = targetPackage
                startExpulsionDaemon()
            }
            return
        }

        // Token invalid or absent: trigger Flashcard Intercept
        val interceptIntent = Intent(this, FlashcardInterceptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("target_package", targetPackage)
        }
        startActivity(interceptIntent)
    }

    private fun startExpulsionDaemon() {
        activeExpulsionJob?.cancel()
        activeExpulsionJob = serviceScope.launch {
            while (tokenManager.hasValidToken()) {
                delay(1000L) // check every second
            }
            // Token expired! Eject!
            currentRestrictedPackageAllowed = null
            performGlobalAction(GLOBAL_ACTION_BACK)
            
            // Send back to home screen to be sure
            val fallbackHome = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(fallbackHome)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        unregisterReceiver(ejectionReceiver)
    }
}
