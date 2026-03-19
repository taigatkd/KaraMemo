package com.taigatkd.karamemo.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taigatkd.karamemo.data.repository.AppContainer

class KaraMemoViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KaraMemoViewModel::class.java)) {
            return KaraMemoViewModel(
                songRepository = appContainer.songRepository,
                playlistRepository = appContainer.playlistRepository,
                preferencesRepository = appContainer.preferencesRepository,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

