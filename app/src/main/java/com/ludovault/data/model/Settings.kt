package com.ludovault.data.model

/**
 * App settings persisted across sessions.
 *
 * @param upiId User's own UPI ID for recharge QR generation.
 * @param themeMode 0=System, 1=Light, 2=Dark.
 * @param soundEnabled Whether game sounds are enabled.
 */
data class Settings(
    val upiId: String = "",
    val themeMode: Int = 0,
    val soundEnabled: Boolean = true
)
