package com.controlsphere.tvremote.data.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.View
import com.controlsphere.tvremote.data.connection.TVReceiverManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ControlSphereAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var tvReceiverManager: TVReceiverManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        var instance: ControlSphereAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("TVAccessibility", "ControlSphere Accessibility Service Connected")
        
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo = info
        
        // Start TV Receiver in background automatically when accessibility is enabled
        serviceScope.launch {
            tvReceiverManager.startReceiver()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not specifically tracking events, just using service for injection
    }

    override fun onInterrupt() {
        Log.d("TVAccessibility", "ControlSphere Accessibility Service Interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.launch {
            tvReceiverManager.stopReceiver()
        }
    }

    // --- Action Injection API for TVReceiverManager ---
    
    fun injectDpadUp(): Boolean = navigateFocus(View.FOCUS_UP)
    fun injectDpadDown(): Boolean = navigateFocus(View.FOCUS_DOWN)
    fun injectDpadLeft(): Boolean = navigateFocus(View.FOCUS_LEFT)
    fun injectDpadRight(): Boolean = navigateFocus(View.FOCUS_RIGHT)
    
    fun injectGlobalHome(): Boolean = performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    fun injectGlobalBack(): Boolean = performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    fun injectGlobalRecents(): Boolean = performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    
    fun injectDpadCenter(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) 
                          ?: rootNode.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
        
        return focusedNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }
    
    fun injectText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        
        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    private fun navigateFocus(direction: Int): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        // Find currently focused node
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) 
                          ?: rootNode.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
                          
        if (focusedNode != null) {
            // Find next node in direction
            val nextNode = focusedNode.focusSearch(direction)
            if (nextNode != null) {
                return nextNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            }
        } else {
            // If no focus, try to focus the first focusable element
            return focusFirstElement(rootNode)
        }
        return false
    }
    
    private fun focusFirstElement(node: AccessibilityNodeInfo): Boolean {
        if (node.isFocusable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && focusFirstElement(child)) {
                return true
            }
        }
        return false
    }
}
