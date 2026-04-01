package com.tubes.nimons360.websocket.model

import com.google.gson.JsonObject

// Outgoing
data class WsMessage<T>(val type: String, val payload: T, val timestamp: String)

data class UpdatePresencePayload(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
    val metadata: Map<String, Any> = emptyMap()
)

// Incoming
data class WsIncoming(val type: String, val payload: JsonObject?, val timestamp: String)

data class MemberPresencePayload(
    val userId: Int,
    val email: String,
    val fullName: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
    val metadata: JsonObject?
)