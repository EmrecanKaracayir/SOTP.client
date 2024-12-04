package com.karacayir.sotpclient.data.decrypt

data class DecryptRequest(
  val accountId: Int,
  val pairUsername: String,
  val pairSharedOtp: String
)