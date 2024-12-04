package com.karacayir.sotpclient.data.delete

import com.karacayir.sotpclient.data.ClientError
import com.karacayir.sotpclient.data.HttpStatus
import com.karacayir.sotpclient.data.ServerError

data class DeleteData(
  val success: Boolean
)

data class DeleteResponse(
  val httpStatus: HttpStatus,
  val serverError: ServerError?,
  val clientErrors: List<ClientError>,
  val data: DeleteData?
)