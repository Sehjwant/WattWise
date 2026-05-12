package com.example.wattwise

enum class MessageType {
    SENT,
    RECEIVED,
    ALERT
}

data class HouseholdMessage(
    val id: Int,
    val senderName: String,
    val body: String,
    val timestamp: String,
    val type: MessageType
)