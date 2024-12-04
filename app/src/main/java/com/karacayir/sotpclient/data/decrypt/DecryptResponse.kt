package com.karacayir.sotpclient.data.decrypt

import com.karacayir.sotpclient.data.ClientError
import com.karacayir.sotpclient.data.HttpStatus
import com.karacayir.sotpclient.data.ServerError

data class DecryptData(
  val documentContent: String
)

data class DecryptResponse(
  val httpStatus: HttpStatus,
  val serverError: ServerError?,
  val clientErrors: List<ClientError>,
  val data: DecryptData?
)