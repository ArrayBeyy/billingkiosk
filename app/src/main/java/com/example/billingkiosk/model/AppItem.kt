package com.example.billingkiosk.model

data class AppItem(
    val label: String,
    val packageName: String,
    var isAllowed: Boolean = false
)
