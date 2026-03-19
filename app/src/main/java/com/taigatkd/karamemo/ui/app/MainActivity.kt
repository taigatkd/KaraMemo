package com.taigatkd.karamemo.ui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.taigatkd.karamemo.KaraMemoApplication
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KaraMemoViewModel by viewModels {
        KaraMemoViewModelFactory((application as KaraMemoApplication).appContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaraMemoTheme {
                KaraMemoApp(viewModel = viewModel)
            }
        }
    }
}

