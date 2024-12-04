package com.karacayir.sotpclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karacayir.sotpclient.data.RetrofitClient
import com.karacayir.sotpclient.data.decrypt.DecryptRequest
import com.karacayir.sotpclient.data.decrypt.DecryptResponse
import com.karacayir.sotpclient.data.delete.DeleteRequest
import com.karacayir.sotpclient.data.delete.DeleteResponse
import com.karacayir.sotpclient.data.login.LoginRequest
import com.karacayir.sotpclient.data.login.LoginResponse
import com.karacayir.sotpclient.data.pair.PairRequest
import com.karacayir.sotpclient.data.pair.PairResponse
import com.karacayir.sotpclient.ui.theme.SOTPClientTheme
import com.karacayir.sotpclient.ui.views.LogWindow
import com.karacayir.sotpclient.utils.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response


class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  // /login request data
  private var username by mutableStateOf("")
  private var password by mutableStateOf("")

  // /pair request data
  private var documentContent by mutableStateOf("")
  private var pairUsername by mutableStateOf("")

  // /document request data
  private var pairSOTP by mutableStateOf("")

  // Booleans
  private var blockedForNetwork by mutableStateOf(false)
  private var loggedIn by mutableStateOf(false)
  private var doesHavePair by mutableStateOf(false)
  private var decrypted by mutableStateOf(false)

  // Time related
  private var remainingTime by mutableIntStateOf(300)

  // User Info
  private var accountId by mutableIntStateOf(-1)
  private var sharedOtp by mutableStateOf("")


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SOTPClientTheme {
        MainScreen(viewModel)
      }
    }
  }

  @Composable
  fun MainScreen(viewModel: MainViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier.fillMaxSize()
        ) {
          Text(
            text = "SOTP",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .padding(top = 64.dp, start = 16.dp, end = 16.dp)
          )
          Box(
            modifier = Modifier
              .weight(1F)
              .fillMaxWidth()
          ) {
            if (!loggedIn) {
              LoginUI()
            }
            if (loggedIn && !doesHavePair) {
              remainingTime = 300
              NoPairUI()
            }
            if (loggedIn && doesHavePair) {
              remainingTime = 300
              PairUI()
            }
          }
          LogWindow(viewModel)
        }

        if (blockedForNetwork) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
          ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            Text(
              text = "Please wait...",
              modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp)
            )
          }
        }
      }
    }
  }

  @Composable
  fun LoginUI() {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        label = { Text("Username") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = password,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onValueChange = { password = it },
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = { makeLoginRequest(false) }) {
        Text("Login")
      }
    }
  }

  @Composable
  fun NoPairUI() {
    // Function to start the countdown timer
    fun startTimer() {
      CoroutineScope(Dispatchers.Main).launch {
        while (true) {
          delay(100)
          remainingTime--
          if (remainingTime <= 0)
          {
            makeLoginRequest(true)
            remainingTime = 300
          }
        }
      }
    }

    // Start the timer immediately when the UI is visible
    LaunchedEffect(Unit) {
      startTimer()
    }

    Column(modifier = Modifier.padding(16.dp)) {
      // First Row: Shared OTP
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
              // Copy OTP to clipboard
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = sharedOtp,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.wrapContentWidth(),
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            CircularProgressIndicator(
              progress = {
                remainingTime / 300f
              },
              modifier = Modifier.size(24.dp),
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))

      // Second Row: Subtitle and multi-line TextField
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1F)
      ) {
        Text(
          text = "Document",
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = documentContent,
          onValueChange = {
            documentContent = it
          },
          placeholder = { Text("Enter document content here...") },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1F)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))

      // Third Row: Pair Username Label and TextField
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Pair Username",
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = pairUsername,
          onValueChange = {
            pairUsername = it
          },
          placeholder = { Text("Enter username here...") },
          modifier = Modifier.fillMaxWidth()
        )
      }
      Spacer(modifier = Modifier.height(16.dp))

      // Fourth Row: Logout Button
      Row(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = {
          loggedIn = false
        }, modifier = Modifier.weight(1f)) {
          Text("Logout")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            makePairRequest()
          }, modifier = Modifier.weight(1f)
        ) {
          Text("Pair")
        }
      }
    }
  }

  @Composable
  fun PairUI() {
    // Function to start the countdown timer
    fun startTimer() {
      CoroutineScope(Dispatchers.Main).launch {
        while (true) {
          delay(100)
          remainingTime--
          if (remainingTime <= 0)
          {
            makeLoginRequest(true)
            remainingTime = 300
          }
        }
      }
    }

    // Start the timer immediately when the UI is visible
    LaunchedEffect(Unit) {
      startTimer()
    }

    Column(modifier = Modifier.padding(16.dp)) {
      // First Row: Shared OTP
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
              // Copy OTP to clipboard
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = sharedOtp,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.wrapContentWidth(),
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(8.dp))
            CircularProgressIndicator(
              progress = {
                remainingTime / 300f
              },
              modifier = Modifier.size(24.dp),
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))

      // Second Row: Subtitle and multi-line TextField
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1F)
      ) {
        Text(
          text = "Document",
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = documentContent,
          onValueChange = {
            // nothing
          },
          enabled = false,
          colors = TextFieldDefaults.colors(disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1F)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))

      // Third Row: Pair Username Label and TextField
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Pair Username",
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
          value = pairUsername,
          onValueChange = {
            // nothing
          },
          enabled = false,
          colors = TextFieldDefaults.colors(disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor),
          modifier = Modifier.fillMaxWidth()
        )
      }
      Spacer(modifier = Modifier.height(16.dp))

      if (!decrypted) {
        // Fourth Row: SOTP
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Pair SOTP",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
          )
          Spacer(modifier = Modifier.height(8.dp))
          TextField(
            value = pairSOTP,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
              pairSOTP = it
            },
            placeholder = { Text("Enter your pair's SOTP here...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Fifth Row: Buttons
      Row(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = {
          loggedIn = false
        }, modifier = Modifier.weight(1f)) {
          Text("Logout")
        }
        if (!decrypted) {
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              makeDecryptRequest()
            }, modifier = Modifier.weight(1f)
          ) {
            Text("Decrypt")
          }
        } else {
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              makeDeleteRequest()
            }, modifier = Modifier.weight(1f)
          ) {
            Text("Delete")
          }
        }
      }
    }
  }

  private fun makeLoginRequest(byTime: Boolean) {
    // Set State
    if (!byTime) decrypted = false

    // Print Title
    if (byTime)
      viewModel.log("Network Request: /refresh", LogLevel.TITLE)
    else
      viewModel.log("Network Request: /login", LogLevel.TITLE)

    blockedForNetwork = true
    RetrofitClient.apiService.login(
      LoginRequest(
        username,
        password,
      )
    )
      .enqueue(object : retrofit2.Callback<LoginResponse> {
        override fun onResponse(
          call: Call<LoginResponse>,
          response: Response<LoginResponse>
        ) {
          if (response.isSuccessful) {
            if (byTime)
            viewModel.log("Refresh success.", LogLevel.SUCCESS)
            else
              viewModel.log("Login success.", LogLevel.SUCCESS)
            loggedIn = true
            doesHavePair = response.body()!!.data!!.pairDoc != null
            accountId = response.body()!!.data!!.accountId
            sharedOtp = response.body()!!.data!!.sharedOtp
            if (doesHavePair) {
              pairUsername = response.body()!!.data!!.pairDoc!!.pairUsername
              documentContent = response.body()!!.data!!.pairDoc!!.content
            }
            if (byTime && decrypted)
              makeDecryptRequest()
          } else {
            val gson = Gson()
            val type = object : TypeToken<LoginResponse>() {}.type
            val errorResponse: LoginResponse =
              gson.fromJson(response.errorBody()!!.charStream(), type)
            if (response.code() in 500..599) {
              viewModel.log(errorResponse.serverError!!.name, LogLevel.ERROR)
            } else if (response.code() in 400..499) {
              for (clientError in errorResponse.clientErrors) {
                viewModel.log(clientError.message, LogLevel.ERROR)
              }
            }
          }
          blockedForNetwork = false
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
          viewModel.log(t.message!!, LogLevel.ERROR)
          blockedForNetwork = false
        }
      })
  }

  private fun makePairRequest() {
    // Print Title
    viewModel.log("Network Request: /pair", LogLevel.TITLE)

    blockedForNetwork = true
    RetrofitClient.apiService.pair(
      PairRequest(
        accountId,
        pairUsername,
        documentContent,
      )
    )
      .enqueue(object : retrofit2.Callback<PairResponse> {
        override fun onResponse(
          call: Call<PairResponse>,
          response: Response<PairResponse>
        ) {
          if (response.isSuccessful) {
            viewModel.log("Pair success.", LogLevel.SUCCESS)
            loggedIn = false
            makeLoginRequest(false)
          } else {
            val gson = Gson()
            val type = object : TypeToken<LoginResponse>() {}.type
            val errorResponse: LoginResponse =
              gson.fromJson(response.errorBody()!!.charStream(), type)
            if (response.code() in 500..599) {
              viewModel.log(errorResponse.serverError!!.name, LogLevel.ERROR)
            } else if (response.code() in 400..499) {
              for (clientError in errorResponse.clientErrors) {
                viewModel.log(clientError.message, LogLevel.ERROR)
              }
            }
          }
          blockedForNetwork = false
        }

        override fun onFailure(call: Call<PairResponse>, t: Throwable) {
          viewModel.log(t.message!!, LogLevel.ERROR)
          blockedForNetwork = false
        }
      })
  }

  private fun makeDecryptRequest() {
    // Print Title
    viewModel.log("Network Request: /decrypt", LogLevel.TITLE)

    blockedForNetwork = true
    RetrofitClient.apiService.decrypt(
      DecryptRequest(
        accountId,
        pairUsername,
        pairSOTP,
      )
    )
      .enqueue(object : retrofit2.Callback<DecryptResponse> {
        override fun onResponse(
          call: Call<DecryptResponse>,
          response: Response<DecryptResponse>
        ) {
          if (response.isSuccessful) {
            viewModel.log("Decrypt success.", LogLevel.SUCCESS)
            documentContent = response.body()!!.data!!.documentContent
            decrypted = true
          } else {
            val gson = Gson()
            val type = object : TypeToken<LoginResponse>() {}.type
            val errorResponse: LoginResponse =
              gson.fromJson(response.errorBody()!!.charStream(), type)
            if (response.code() in 500..599) {
              viewModel.log(errorResponse.serverError!!.name, LogLevel.ERROR)
            } else if (response.code() in 400..499) {
              for (clientError in errorResponse.clientErrors) {
                viewModel.log(clientError.message, LogLevel.ERROR)
              }
            }
            decrypted = false
          }
          blockedForNetwork = false
        }

        override fun onFailure(call: Call<DecryptResponse>, t: Throwable) {
          viewModel.log(t.message!!, LogLevel.ERROR)
          decrypted = false
          blockedForNetwork = false
        }
      })
  }

  private fun makeDeleteRequest() {
    // Print Title
    viewModel.log("Network Request: /delete", LogLevel.TITLE)

    blockedForNetwork = true
    RetrofitClient.apiService.delete(
      DeleteRequest(
        accountId,
      )
    )
      .enqueue(object : retrofit2.Callback<DeleteResponse> {
        override fun onResponse(
          call: Call<DeleteResponse>,
          response: Response<DeleteResponse>
        ) {
          if (response.isSuccessful) {
            viewModel.log("Delete success.", LogLevel.SUCCESS)
            documentContent = ""
            pairUsername = ""
            pairSOTP = ""
            loggedIn = false
          } else {
            val gson = Gson()
            val type = object : TypeToken<LoginResponse>() {}.type
            val errorResponse: LoginResponse =
              gson.fromJson(response.errorBody()!!.charStream(), type)
            if (response.code() in 500..599) {
              viewModel.log(errorResponse.serverError!!.name, LogLevel.ERROR)
            } else if (response.code() in 400..499) {
              for (clientError in errorResponse.clientErrors) {
                viewModel.log(clientError.message, LogLevel.ERROR)
              }
            }
          }
          blockedForNetwork = false
        }

        override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
          viewModel.log(t.message!!, LogLevel.ERROR)
          blockedForNetwork = false
        }
      })
  }
}
