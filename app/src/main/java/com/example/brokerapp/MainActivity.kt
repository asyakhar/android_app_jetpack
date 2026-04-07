package com.example.brokerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.brokerapp.navigation.BrokerAppContent
import com.example.brokerapp.ui.theme.BrokerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrokerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrokerAppContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}