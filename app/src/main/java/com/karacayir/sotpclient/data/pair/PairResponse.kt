package com.karacayir.sotpclient.data.pair

import com.karacayir.sotpclient.data.ClientError
import com.karacayir.sotpclient.data.HttpStatus
import com.karacayir.sotpclient.data.ServerError

data class PairData(
  val success: Boolean
)

data class PairResponse(
  val httpStatus: HttpStatus,
  val serverError: ServerError?,
  val clientErrors: List<ClientError>,
  val data: PairData?
)