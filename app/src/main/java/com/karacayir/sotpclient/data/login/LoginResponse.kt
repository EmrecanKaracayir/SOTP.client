package com.karacayir.sotpclient.data.login

import com.karacayir.sotpclient.data.ClientError
import com.karacayir.sotpclient.data.HttpStatus
import com.karacayir.sotpclient.data.ServerError

data class PairDocData(
  val pairUsername: String,
  val documentId: Int,
  val content: String
)

data class LoginData(
  val accountId: Int,
  val sharedOtp: String,
  val pairDoc: PairDocData?
)

data class LoginResponse(
  val httpStatus: HttpStatus,
  val serverError: ServerError?,
  val clientErrors: List<ClientError>,
  val data: LoginData?
)