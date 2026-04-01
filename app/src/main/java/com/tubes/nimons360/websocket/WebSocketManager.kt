package com.tubes.nimons360.websocket

import android.util.Log
import com.google.gson.Gson
import com.tubes.nimons360.core.network.TokenManager
import com.tubes.nimons360.websocket.model.MemberPresencePayload
import com.tubes.nimons360.websocket.model.UpdatePresencePayload
import com.tubes.nimons360.websocket.model.WsIncoming
import com.tubes.nimons360.websocket.model.WsMessage
import kotlinx.coroutines.*
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WebSocketManager(
    private val tokenManager: TokenManager,
    private val gson: Gson = Gson()
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS).build()
    private var webSocket: WebSocket? = null

    var onPresenceUpdated: ((MemberPresencePayload) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    private val wsUrl = "wss://mad.labpro.hmif.dev/ws/live"

    fun connect() {
        val token = tokenManager.getToken() ?: return
        val request = Request.Builder().url(wsUrl)
            .addHeader("Authorization", "Bearer $token").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                onConnectionChange?.invoke(true)
                startPingLoop()
            }
            override fun onMessage(ws: WebSocket, text: String) {
                handleIncoming(text)
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                onConnectionChange?.invoke(false)
                Log.e("WebSocket", "Failure: ${t.message}")
            }
            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                onConnectionChange?.invoke(false)
            }
        })
    }

    private fun handleIncoming(text: String) {
        try {
            val msg = gson.fromJson(text, WsIncoming::class.java)
            when (msg.type) {
                "member_presence_updated" -> {
                    val payload = gson.fromJson(msg.payload, MemberPresencePayload::class.java)
                    onPresenceUpdated?.invoke(payload)
                }
                "pong" -> { /* heartbeat received */ }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Parse error: ${e.message}")
        }
    }

    private var pingJob: Job? = null

    private fun startPingLoop() {
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(4000)
                sendPing()
            }
        }
    }

    private fun sendPing() {
        val msg = WsMessage("ping", emptyMap<String, Any>(),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        webSocket?.send(gson.toJson(msg))
    }

    fun sendPresence(payload: UpdatePresencePayload) {
        val msg = WsMessage("update_presence", payload,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        webSocket?.send(gson.toJson(msg))
    }

    fun disconnect() {
        pingJob?.cancel()
        webSocket?.close(1000, "User left map")
    }
}