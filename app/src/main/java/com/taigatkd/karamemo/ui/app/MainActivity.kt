package com.taigatkd.karamemo.ui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.taigatkd.karamemo.KaraMemoApplication
import com.taigatkd.karamemo.common.AndroidStringResolver
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KaraMemoViewModel by viewModels {
        KaraMemoViewModelFactory(
            appContainer = (application as KaraMemoApplication).appContainer,
            stringResolver = AndroidStringResolver(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaraMemoTheme(useDynamicColor = false) {
                KaraMemoApp(viewModel = viewModel)
            }
        }
    }
}
