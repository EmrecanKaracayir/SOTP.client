package com.karacayir.sotpclient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.karacayir.sotpclient.utils.Log
import com.karacayir.sotpclient.utils.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalTime

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val _logs: MutableStateFlow<List<Log>> =
    MutableStateFlow(listOf(Log("Log Start", LogLevel.TITLE, LocalTime.now().toString())))
  val logs get() = _logs
  fun log(message: String, logLevel: LogLevel) {
    _logs.value += Log(message, logLevel, LocalTime.now().toString())
  }
}