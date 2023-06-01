package com.liss.smartsos

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.os.postDelayed
import java.util.*

class AutoclickerService : AccessibilityService() {

    private var isPanicPressed = false
    private val handler = Handler()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isPanicPressed && event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d("Autoclicker","En la pantalla principal de 911")
            handler.postDelayed({
                rootInActiveWindow?.let { rootNode ->
                    val targetNode = findNodeByText(rootNode, "Pánico")
                    targetNode?.let { node ->
                        performClick(node)
                        node.recycle()
                        isPanicPressed = true
                        Log.d("Autoclicker", "Boton con texto de Panico encontrado")
                    }
                }
            }, 3000)
        }
        if(isPanicPressed && event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d("Autoclicker","En el boton de panico")
            handler.postDelayed({
                rootInActiveWindow?.let { rootNode ->
                    Log.d("123", findNodeByText(rootNode, "Tijuana").toString())
                    val rButtonTij = findNodeByText(rootNode, "Tijuana").toString()
                    if (rButtonTij != null) {
                        val targetNode = findNodeByText(rootNode, "Tijuana")
                        targetNode?.let { node ->
                            performSelect(node)
                            node.recycle()
                            Log.d("Autoclicker", "Click")
                        }
                    }
                    handler.postDelayed({
                        Log.d("321", findFirstImageView(rootNode).toString())
                        val bPanico = findFirstImageView(rootNode)
                        if(bPanico != null) {
                            disableSelf()
                        }
                        //disableSelf()
                    },2000)
                }
            }, 1000)
        }
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, targetText: String): AccessibilityNodeInfo? {
        if (node.text?.toString() == targetText) {
            return node
        }
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            val targetNode = findNodeByText(childNode, targetText)
            if (targetNode != null) {
                return targetNode
            }
        }
        node.recycle()
        return null
    }

    private fun performClick(node: AccessibilityNodeInfo) {
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            performClick(childNode)
        }
        node.recycle()
    }

    //Codificacion para el dialogo de alerta si se muestra
    private fun performSelect(node: AccessibilityNodeInfo){
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }
        node.recycle()
    }

    //Codificacion para la pantalla de boton de panico
    private fun findFirstImageView(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className == "android.widget.ImageView") {
            return node
        }
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                val targetNode = findFirstImageView(childNode)
                if (targetNode != null) {
                    return targetNode
                }
            }
        }
        node.recycle()
        return null
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        disableSelf()
    }
}
