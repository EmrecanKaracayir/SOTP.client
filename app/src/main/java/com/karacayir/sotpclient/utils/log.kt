package com.karacayir.sotpclient.utils

data class Log(
  val message: String,
  val logLevel: LogLevel,
  val logTime: String
)

enum class LogLevel {
  TITLE,
  SUCCESS,
  INFO,
  WARNING,
  ERROR
}