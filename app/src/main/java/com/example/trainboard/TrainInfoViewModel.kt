package com.example.trainboard

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*

class TrainInfoViewModel : ViewModel() {
    var trainInfo by mutableStateOf<TrainInfo?>(null)
}
