package com.controlsphere.tvremote.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 5556,
    val isConnected: Boolean = false,
    val isAuthorized: Boolean = false,
    val lastConnected: Long = 0L
) : Parcelable

@Parcelize
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: ByteArray? = null,
    val isFavorite: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AppInfo
        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}

enum class KeyEvent(val code: Int, val label: String) {
    DPAD_UP(19, "Up"),
    DPAD_DOWN(20, "Down"),
    DPAD_LEFT(21, "Left"),
    DPAD_RIGHT(22, "Right"),
    DPAD_CENTER(23, "OK"),
    BACK(4, "Back"),
    HOME(3, "Home"),
    APP_SWITCH(187, "Apps"),
    POWER(26, "Power"),
    VOLUME_UP(24, "Volume Up"),
    VOLUME_DOWN(25, "Volume Down"),
    MUTE(164, "Mute"),
    PLAY_PAUSE(85, "Play/Pause"),
    MEDIA_NEXT(87, "Next"),
    MEDIA_PREVIOUS(88, "Previous"),
    FAST_FORWARD(90, "Fast Forward"),
    REWIND(89, "Rewind"),
    ENTER(66, "Enter"),
    MENU(82, "Menu"),
    SETTINGS_SEARCH(84, "Search")
}

data class AdbCommand(
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConnectionStatus(
    val isConnected: Boolean,
    val isAuthorized: Boolean,
    val errorMessage: String? = null
)
