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
import com.example.httplite.Http
import com.example.httplite.JSONObjectListener
import org.json.JSONObject

// TODO: Refactor to MVVM
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyScreen()
        }
    }

    @Composable
    fun MyScreen() {
        var title by remember { mutableStateOf("Loading...") }

        LaunchedEffect(Unit) {
            fetchTodo { result ->
                title = result
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title)
            }
        }
    }

    fun fetchTodo(onResult: (String) -> Unit) {
        Http.Request(Http.Method.GET)
            .url("https://jsonplaceholder.typicode.com/todos/1")
            .makeRequest(object : JSONObjectListener {
                override fun onResponse(res: JSONObject?) {
                    onResult(res?.toString(4) ?: "No title")
                }

                override fun onFailure(e: Exception?) {
                    onResult("Failed: ${e?.message ?: "Unknown error"}")
                }
            })
    }
}
