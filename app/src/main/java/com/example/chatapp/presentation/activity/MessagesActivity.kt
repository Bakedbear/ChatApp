package com.example.chatapp.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamShapes

class MessagesActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelId = intent.getStringExtra(KEY_CHANNEL_ID)

        if (channelId == null) {
            finish()
            return
        }

        setContent {
            ChatTheme(
                shapes = StreamShapes.defaultShapes().copy(
                    attachment = RoundedCornerShape(16.dp),
                    myMessageBubble = RoundedCornerShape(16.dp),
                    otherMessageBubble = RoundedCornerShape(16.dp),
                )
            ){
                MessagesScreen(
                    channelId = channelId,
                    messageLimit = 30,
                    onBackPressed = { finish() }

                )
            }
        }
    }
    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        fun getIntent(context: Context, channelId: String ): Intent {
            return Intent(context, MessagesActivity::class.java ).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}