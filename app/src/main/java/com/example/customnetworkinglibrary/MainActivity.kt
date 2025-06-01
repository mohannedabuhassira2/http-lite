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
import com.example.customnetworkinglibrary.networking.model.Todo
import com.example.httplite.client.NetworkClient
import com.example.httplite.response.ApiResult

// TODO: Refactor to MVVM
class MainActivity : ComponentActivity() {
    private val networkClient = NetworkClient(
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
                fetchTodo(networkClient)
            } catch (e: Exception) {
                "${e.message}"
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = httpResult)
            }
        }
    }

    suspend fun fetchTodo(networkManager: NetworkClient): String {
        val apiResult = networkManager.get<Todo>(
            queryPath = "/todos/1",
            responseClass = Todo::class.java
        )

        return when (apiResult) {
            is ApiResult.Response<Todo> -> {
                val statusCode = apiResult.statusCode
                val data = apiResult.data
                "Status Code: $statusCode, \nData: ${data}"
            }
            is ApiResult.SerializationFailed -> apiResult.exception.message.toString()
            is ApiResult.NetworkFailed -> apiResult.exception.message.toString()
        }
    }
}
