package com.example.chatapp.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatapp.domain.MenuItem
import com.example.chatapp.presentation.DrawerBody
import com.example.chatapp.presentation.DrawerHeader
import com.example.chatapp.presentation.MainActivity
import com.example.chatapp.presentation.viewmodel.ChannelListViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelListActivity: ComponentActivity() {

    private val viewModel: ChannelListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeToEvents()

        setContent {
            ChatTheme {
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()
                val dialogState = remember { mutableStateOf(false) }

                Scaffold(
                    scaffoldState = scaffoldState,
                    drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
                    drawerContent = {
                        DrawerHeader()
                        Spacer(modifier = Modifier.height(8.dp))
                        DrawerBody(
                            items = listOf(
                                MenuItem(
                                    id = "direct message",
                                    title = "New Direct Message",
                                    contentDescription = "See available channels",
                                    icon = Icons.Default.Edit
                                ),
                                MenuItem(
                                    id = "groups",
                                    title = "Groups",
                                    contentDescription = "See your groups",
                                    icon = Icons.Default.Groups
                                ),
                                MenuItem(
                                    id = "settings",
                                    title = "Settings",
                                    contentDescription = "Go to settings screen",
                                    icon = Icons.Default.Settings
                                ),
                                MenuItem(
                                    id = "logout",
                                    title = "Logout",
                                    contentDescription = "log out",
                                    icon = Icons.Default.Logout
                                ),

                                ),
                            onItemClick = {

                                when(it.id){
                                    "logout" -> {
                                        logout()
                                    }
                                }

                                println("Clicked on ${it.title}")
                            }
                        )
                    }
                ) {it

                    if (dialogState.value) {
                        CreateChannelDialog(
                            dismiss = { channelName ->
                                viewModel.createChannel(channelName)
                                dialogState.value = false
                            }
                        )
                    }
                    ChannelsScreen(
                        filters = Filters.`in`(
                            fieldName = "type",
                            values = listOf("gaming", "messaging", "commerce", "team", "livestream")
                        ),
                        isShowingSearch = true,
                        onItemClick = { channel ->
                            startActivity(
                                MessagesActivity.getIntent(
                                    this,
                                    channelId = channel.cid
                                )
                            )
                        },
                        onBackPressed = { finish() },
                        onHeaderActionClick = {
                                 dialogState.value = true
                        },
                        onHeaderAvatarClick = {
                            scope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun CreateChannelDialog(dismiss: (String) -> Unit) {

        var channelName by remember {
            mutableStateOf("")
        }

        Dialog(
            onDismissRequest = { dismiss(channelName) },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ){
                Box(
                    contentAlignment = Alignment.Center
                ){
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = "Create New Channel",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = channelName,
                            onValueChange = { channelName = it },
                            label = { Text("Channel Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                dismiss(channelName)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(text = "Create")
                        }
                    }
                }
            }


        }
    }

    private fun subscribeToEvents(){

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){

                viewModel.createChannelEvent.collect {event ->

                    when (event) {

                        is ChannelListViewModel.CreateChannelEvent.Error -> {
                            val errorMessage = event.error
                            showToast(errorMessage)
                        }

                        is ChannelListViewModel.CreateChannelEvent.Success -> {
                            showToast("Channel Created")
                        }
                    }
                }
            }
        }

    }

    private fun logout() {

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun MyPreview(){
    ChannelListActivity()
}