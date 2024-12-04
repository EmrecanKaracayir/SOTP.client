package com.karacayir.sotpclient.data.pair

data class PairRequest(
  val accountId: Int,
  val pairUsername: String,
  val documentContent: String
)