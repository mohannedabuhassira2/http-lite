package com.example.httplite

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.text.Charsets.UTF_8

object Http {
    enum class Method {
        GET,
        POST,
        DELETE,
        PUT;

        override fun toString(): String = name
    }

    class Request(
        val method: Method
    ) {
        val header: MutableMap<String, String> = HashMap()
        var url: String? = null
        var body: ByteArray? = null
        private var jsonObjReqListener: JSONObjectListener? = null

        fun url(url: String?): Request {
            this.url = url
            return this
        }

        fun body(bodyJson: JSONObject?): Request {
            val textBody = bodyJson?.toString()
            body = textBody?.toByteArray(UTF_8)
            this.header["Content-Type"] = "application/json"
            return this
        }

        fun header(header: Map<String, String>?): Request {
            if (header.isNullOrEmpty()) {
                return this
            }

            this.header.putAll(header)
            return this
        }

        fun makeRequest(jsonObjectListener: JSONObjectListener?): Request {
            this.jsonObjReqListener = jsonObjectListener

            CoroutineScope(Dispatchers.IO).launch {
                val requestTask = RequestTask(this@Request)
                requestTask.run()
            }

            return this
        }

        internal fun sendResponse(resp: Response?, e: Exception?) {
            if (jsonObjReqListener != null) {
                if (e != null) jsonObjReqListener?.onFailure(e)
                else jsonObjReqListener?.onResponse(resp?.asJSONObject())
            }
        }
    }
}