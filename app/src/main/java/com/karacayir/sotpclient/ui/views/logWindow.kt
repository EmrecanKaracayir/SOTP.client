package com.karacayir.sotpclient.ui.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karacayir.sotpclient.utils.Log
import com.karacayir.sotpclient.utils.LogLevel
import com.karacayir.sotpclient.MainViewModel
import kotlinx.coroutines.flow.StateFlow

data class CustomScrollState(var value: Int, var toEnd: Boolean, var active: Boolean)

var lwScrollState = CustomScrollState(0, toEnd = false, active = false)

@Composable
fun LogWindow(mainViewModel: MainViewModel)
{
  var showLogWindow by remember { mutableStateOf(true) }
  val logWindowSize by animateDpAsState(
    if (showLogWindow) LocalConfiguration.current.screenHeightDp.dp * 0.2F else 0.dp,
    label = ""
  )

  Box(
    modifier = Modifier
      .wrapContentHeight()
      .fillMaxWidth()
      .background(Color.Black)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    )
    {
      Text(
        style = MaterialTheme.typography.titleMedium,
        text = "Logs",
        color = Color.White,
        modifier = Modifier
          .weight(1F)
          .padding(PaddingValues(start = 44.dp)),
        textAlign = TextAlign.Center,
      )
      IconButton(
        modifier = Modifier,
        colors = IconButtonColors(
          containerColor = Color.Transparent,
          contentColor = Color.White,
          disabledContainerColor = Color.Transparent,
          disabledContentColor = Color.Gray
        ),
        onClick = {
          showLogWindow = !showLogWindow
          lwScrollState.active = !lwScrollState.active
        }
      ) {
        Icon(
          imageVector = if (showLogWindow) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
          contentDescription = "Toggle Log Window"
        )
      }
    }
  }
  Box(
    modifier = Modifier.height(logWindowSize)
  ) {
    LogPanel(mainViewModel.logs, modifier = Modifier.fillMaxSize())
  }
}

@Composable
fun LogPanel(
  logs: StateFlow<List<Log>>,
  modifier: Modifier = Modifier,
) {
  val logList by logs.collectAsState()
  val scrollState = rememberScrollState()

  LaunchedEffect(logList.size) {
    if (lwScrollState.toEnd) {
      scrollState.animateScrollTo(scrollState.maxValue)
    } else if (lwScrollState.value != scrollState.value) {
      scrollState.animateScrollTo(lwScrollState.value)
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color.Black)
      .verticalScrollBar(scrollState)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.Top
  ) {
    if (lwScrollState.active) {
      lwScrollState.value = scrollState.value
      lwScrollState.toEnd = scrollState.value == scrollState.maxValue
    }

    for (log in logList) {
      Text(
        modifier = Modifier.padding(4.dp),
        text = "${if (log.logLevel != LogLevel.TITLE) "[${log.logTime}] " else "// "}${log.message}",
        color = when (log.logLevel) {
          LogLevel.TITLE -> Color.White
          LogLevel.SUCCESS -> Color.Green
          LogLevel.INFO -> Color.Cyan
          LogLevel.WARNING -> Color.Yellow
          LogLevel.ERROR -> Color.Red
        },
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = if (log.logLevel != LogLevel.TITLE) FontWeight.Normal else FontWeight.Bold
      )
    }
  }
}