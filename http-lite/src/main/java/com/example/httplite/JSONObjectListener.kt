package com.example.httplite

import org.json.JSONObject

interface JSONObjectListener {
    fun onResponse(res: JSONObject?)
    fun onFailure(e: Exception?)
}
