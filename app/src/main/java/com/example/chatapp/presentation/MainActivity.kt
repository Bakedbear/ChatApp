package com.example.chatapp.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatapp.R
import com.example.chatapp.presentation.activity.ChannelListActivity
import com.example.chatapp.presentation.viewmodel.LoginScreenViewModel
import com.example.chatapp.ui.theme.ChatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: LoginScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // observe login events from viewmodel
        subscribeToEvents()

        setContent {
            ChatAppTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {

        var username by remember { mutableStateOf(TextFieldValue("")) }

        var showProgress: Boolean by remember { mutableStateOf(false) }

        viewModel.loadingState.observe(this) { uiLoadingState ->
            showProgress = when (uiLoadingState) {
                is LoginScreenViewModel.UiLoadingState.Loading -> {
                    true
                }

                is LoginScreenViewModel.UiLoadingState.NotLoading -> {
                    false
                }
            }
        }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 35.dp, end = 35.dp)
        ) {

            val (
                logo, usernameTextField, btnLoginAsUser, btnLoginAsGuest,
                progressBar, textField
            ) = createRefs()

            Image(
                painterResource(R.drawable.chaticon),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
                    .constrainAs(logo) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top, margin = 80.dp)
                    }
            )
            Text(
                text = stringResource(R.string.welcome),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 0.25.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(textField) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(logo.bottom, margin = 28.dp)
                    }
            )

            OutlinedTextField(
                value = username,
                onValueChange = { newValue -> username = newValue },
                label = {
                    Text(
                        text = "Enter your username"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person icon"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(usernameTextField) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(textField.bottom, margin = 28.dp)
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Button(
                onClick = {
                    viewModel.loginUser(
                        username.text, getString(R.string.jwt_token)
                    )
                },
                colors = ButtonDefaults.buttonColors(contentColor = Color.White),
                shape = CutCornerShape(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .constrainAs(btnLoginAsUser) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(usernameTextField.bottom, margin = 58.dp)
                    }
            ) {
                Text(text = "Login as User")
            }

            Button(
                onClick = {
                          viewModel.loginUser(username.text)
                },
                shape = CutCornerShape(5),
                colors = ButtonDefaults.buttonColors(contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .constrainAs(btnLoginAsGuest) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(btnLoginAsUser.bottom, margin = 30.dp)
                    }
            ) {
                Text(text = "Login as Guest")
            }

            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.constrainAs(progressBar) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(btnLoginAsGuest.bottom, margin = 18.dp)
                    }
                )
            }
        }
    }

    private fun subscribeToEvents(){

        lifecycleScope.launch{

            //collect events from viewModel when lifecycle state is started
            repeatOnLifecycle(Lifecycle.State.STARTED){

                viewModel.loginEvent.collect {event ->

                    when(event) {
                        is LoginScreenViewModel.LogInEvent.ErrorInputTooShort -> {
                            showToast("Invalid! Enter more than 3 characters.")
                        }

                        is LoginScreenViewModel.LogInEvent.ErrorLogIn -> {
                            val errorMessage = event.error
                            showToast("Error: $errorMessage")
                        }

                        is LoginScreenViewModel.LogInEvent.Success -> {
                            showToast("Login Successful")
                            startActivity(Intent(this@MainActivity, ChannelListActivity::class.java ))
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

