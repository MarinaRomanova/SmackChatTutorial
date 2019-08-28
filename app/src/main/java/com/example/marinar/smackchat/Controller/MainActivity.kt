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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.marinar.smackchat.Model.Channel
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
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelsAdapter: ArrayAdapter<Channel>

    private fun setUpAdapter() {
        channelsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list_view.adapter = channelsAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        socket.connect()
        socket.on(getString(R.string.socket_channel_created_event_flag), onNewChannel)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setUpAdapter()

        if(App.sharedPreferences.isLoggedIn){
            AuthService.findUserByEmail(this){}
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
                MessageService.getChannels(context){ complete ->
                    if (complete) {
                        channelsAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangedReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGED))
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

                        val channeleName = channelNameTextField.text.toString()
                        val channelDescription = channelDescriptionTextField.text.toString()

                        //Create channel with the channel name and description
                        socket.emit(getString(R.string.socket_add_channel_flag), channeleName, channelDescription)

                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialogInterface, i ->
                        //Cancel and close the dialog

                    }
                    .show()
        }

    }

    fun onSendMessageClicked(view: View) {
        hideKeyBoard()
    }

    fun hideKeyBoard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        runOnUiThread {
            MessageService.channels.add(Channel(args[0] as String, args[1] as String, args[2] as String))
            channelsAdapter.notifyDataSetChanged()
        }
    }
}
