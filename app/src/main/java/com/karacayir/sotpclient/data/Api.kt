package com.karacayir.sotpclient.data

import com.karacayir.sotpclient.data.decrypt.DecryptRequest
import com.karacayir.sotpclient.data.decrypt.DecryptResponse
import com.karacayir.sotpclient.data.delete.DeleteRequest
import com.karacayir.sotpclient.data.delete.DeleteResponse
import com.karacayir.sotpclient.data.login.LoginRequest
import com.karacayir.sotpclient.data.login.LoginResponse
import com.karacayir.sotpclient.data.pair.PairRequest
import com.karacayir.sotpclient.data.pair.PairResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
  @POST("/api/login")
  fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

  @POST("/api/pair")
  fun pair(@Body pairRequest: PairRequest): Call<PairResponse>

  @POST("/api/decrypt")
  fun decrypt(@Body decryptRequest: DecryptRequest): Call<DecryptResponse>

  @POST("/api/delete")
  fun delete(@Body deleteRequest: DeleteRequest): Call<DeleteResponse>
}

object RetrofitClient {
  private val retrofit = Retrofit.Builder()
    .baseUrl("http://35.159.138.63:3000/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  val apiService: ApiService = retrofit.create(ApiService::class.java)
}