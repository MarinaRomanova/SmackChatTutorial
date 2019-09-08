package com.example.marinar.smackchat.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.marinar.smackchat.Adapters.MessageAdapter
import com.example.marinar.smackchat.Model.Channel
import com.example.marinar.smackchat.Model.Message
import com.example.marinar.smackchat.R
import com.example.marinar.smackchat.Services.AuthService
import com.example.marinar.smackchat.Services.MessageService
import com.example.marinar.smackchat.Services.UserDataService
import com.example.marinar.smackchat.Utils.BROADCAST_USER_DATA_CHANGED
import com.example.marinar.smackchat.Utils.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelsAdapter: ArrayAdapter<Channel>
    lateinit var messagesAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    private fun setUpAdapters() {
        channelsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list_view.adapter = channelsAdapter

        messagesAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messagesAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        socket.connect()
        socket.on(getString(R.string.socket_channel_created_event_flag), onNewChannel)
        socket.on(getString(R.string.socket_message_created_flag), onNewMessage)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setUpAdapters()
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangedReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGED))
        channel_list_view.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.sharedPreferences.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }
    }

    private val userDataChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.sharedPreferences.isLoggedIn) {
                userName_nav.text = UserDataService.name
                userEmail_nav.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageView_nav.setImageResource(resourceId)
                userImageView_nav.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                login_btn.text = getString(R.string.logout)
                MessageService.getChannels(context) { complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelsAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    fun updateWithChannel() {
        mainChannelName_tv.text = "#${selectedChannel?.name}"
        //download messages for channel
        if (selectedChannel != null) {
            MessageService.getMessages(this, selectedChannel!!.id) { complete ->
                if (complete) {
                    messagesAdapter.notifyDataSetChanged()
                    if (messagesAdapter.itemCount > 0) {
                        messageListView.smoothScrollToPosition(messagesAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangedReceiver)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun onLoginClicked(view: View) {
        if (App.sharedPreferences.isLoggedIn) {
            UserDataService.logout()
            mainChannelName_tv.text = getString(R.string.please_log_in)
            channelsAdapter.notifyDataSetChanged()
            messagesAdapter.notifyDataSetChanged()
            userName_nav.text = ""
            userEmail_nav.text = ""
            userImageView_nav.setImageResource(R.drawable.profiledefault)
            userImageView_nav.setBackgroundColor(Color.TRANSPARENT)
            login_btn.text = getString(R.string.login)
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

    }

    fun onAddChannelClicked(view: View) {
        if (App.sharedPreferences.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                    .setPositiveButton(getString(R.string.add)) { dialogInterface, i ->
                        val channelNameTextField = dialogView.findViewById<EditText>(R.id.channel_name_editText)
                        val channelDescriptionTextField = dialogView.findViewById<EditText>(R.id.channel_description_editText)

                        val channelName = channelNameTextField.text.toString()
                        val channelDescription = channelDescriptionTextField.text.toString()

                        //Create channel with the channel name and description
                        socket.emit(getString(R.string.socket_add_channel_flag), channelName, channelDescription)

                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialogInterface, i ->
                        //Cancel and close the dialog

                    }
                    .show()
        }

    }

    fun onSendMessageClicked(view: View) {
        if (App.sharedPreferences.isLoggedIn && messageEditText.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageEditText.text.toString(), userId, channelId, UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageEditText.text.clear()
            hideKeyBoard()
        }
    }

    fun hideKeyBoard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if (App.sharedPreferences.isLoggedIn) {
            runOnUiThread {
                MessageService.channels.add(Channel(args[0] as String, args[1] as String, args[2] as String))
                channelsAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.sharedPreferences.isLoggedIn) {
            runOnUiThread {
                for (arg in args) {
                    println(arg as String)
                }
                val channelId = args[2] as String
                if (channelId == selectedChannel?.id) {
                    val newMessage = Message(args[0] as String, args[3] as String, args[2] as String,
                            args[4] as String, args[5] as String, args[6] as String, args[7] as String)
                    MessageService.messages.add(newMessage)
                    messagesAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messagesAdapter.itemCount - 1)
                }
            }
        }
    }
}
