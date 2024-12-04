package com.karacayir.sotpclient.data

data class HttpStatus(
  val code: Int,
  val message: String
)

data class ServerError(
  val name: String,
  val message: String,
  val stackTrace: String?
)

data class ClientError(
  val code: Int,
  val message: String
)