package com.example.customnetworkinglibrary

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import com.example.customnetworkinglibrary.networking.RetryInterceptor
import com.example.customnetworkinglibrary.networking.model.Todo
import com.example.httplite.client.NetworkClient
import com.google.gson.JsonSyntaxException
import java.io.IOException

// TODO: Refactor to MVVM
class MainActivity : ComponentActivity() {
    private val networkClient = NetworkClient(
        baseUrl = "https://jsonplaceholder.typicode.com",
        requestInterceptors = listOf(
            RetryInterceptor()
        )
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
            httpResult = fetchTodo(networkClient).toString()
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = httpResult
                )
            }
        }
    }

    suspend fun fetchTodo(networkManager: NetworkClient): String? {
        return try {
            val apiResult = networkManager.get<Todo>(
                queryPath = "/todos/1",
                responseClass = Todo::class.java
            )

            val statusCode = apiResult.code
            val data = apiResult.body
            "Status Code: $statusCode, \nData: $data"
        } catch (e: IOException) {
            Log.e("Networking", "Network request failed", e)
            e.message
        } catch (e: JsonSyntaxException) {
            Log.e("Networking", "JSON parsing failed", e)
            e.message
        }
    }
}
