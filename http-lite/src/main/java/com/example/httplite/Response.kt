package com.example.httplite

import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class Response(
    private val data: ByteArray
) {
    @Throws(JSONException::class)
    fun asJSONObject(): JSONObject {
        val str = String(data, StandardCharsets.UTF_8)
        return if (str.isEmpty()) JSONObject() else JSONObject(str)
    }
}
