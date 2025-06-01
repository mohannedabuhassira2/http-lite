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
import com.example.httplite.NetworkManager

// TODO: Refactor to MVVM
class MainActivity : ComponentActivity() {
    private val networkManager = NetworkManager(
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

    suspend fun fetchTodo(networkManager: NetworkManager): String {
        val response = networkManager.get("https://jsonplaceholder.typicode.com/todos/1")
        return response.jsonBody
    }
}
