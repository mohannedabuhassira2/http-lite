package com.example.customnetworkinglibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.httplite.client.NetworkClient
import com.example.httplite.response.ApiResult
import org.json.JSONObject

// TODO: Refactor to MVVM
class MainActivity : ComponentActivity() {
    private val networkManager = NetworkClient(
        baseUrl = "https://jsonplaceholder.typicode.com"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyScreen()
        }
    }

    @Composable
    fun MyScreen() {
        var httpResult by remember { mutableStateOf("Loading...") }

        LaunchedEffect(Unit) {
            httpResult = try {
                fetchTodo(networkManager)
            } catch (e: Exception) {
                "${e.message}"
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = httpResult)
            }
        }
    }

    suspend fun fetchTodo(networkManager: NetworkClient): String {
        val apiResult = networkManager.get<JSONObject>("https://jsonplaceholder.typicode.com/todos/1")
        return when (apiResult) {
            is ApiResult.Response<JSONObject> -> {
                val statusCode = apiResult.statusCode
                val data = apiResult.data
                "Status Code: $statusCode, Data: $data"
            }
            is ApiResult.SerializationFailed -> apiResult.exception.message.toString()
            is ApiResult.NetworkFailed -> apiResult.exception.message.toString()
        }
    }
}
