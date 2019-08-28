package com.example.marinar.smackchat.Controller

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.marinar.smackchat.R
import com.example.marinar.smackchat.Services.AuthService
import com.example.marinar.smackchat.Services.UserDataService
import com.example.marinar.smackchat.Utils.BROADCAST_USER_DATA_CHANGED
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profiledefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        create_spinner.visibility = View.INVISIBLE
    }

    fun onAvatarClicked(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatarNumber = random.nextInt(28) //not included bound

        when (color) {
            0 -> userAvatar = "light$avatarNumber"
            1 -> userAvatar = "dark$avatarNumber"
        }
        create_avatar_imageView.setImageResource(resources.getIdentifier(userAvatar, "drawable", packageName))
    }

    fun onGenerateClicked(view: View) {
        val random = Random()
        val rChannel = random.nextInt(255)
        val gChannel = random.nextInt(255)
        val bChannel = random.nextInt(255)
        create_avatar_imageView.setBackgroundColor(Color.rgb(rChannel, gChannel, bChannel))

        val savedR = rChannel.toDouble() / 255
        val savedG = gChannel.toDouble() / 255
        val savedB = bChannel.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB]"
    }

    fun onCreateClicked(view: View) {

        enableSpinner(true)
        val username = create_username_editText.text.toString()
        val email = create_email_editText.text.toString()
        val password = create_password_editText.text.toString()
        hideKeyBoard()
        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
            AuthService.registerUser(this, email, password) { registerSuccess ->
                if (registerSuccess) {
                    Toast.makeText(this, "Create Success", Toast.LENGTH_SHORT).show()
                    AuthService.loginUser(this, email, password) { loginSuccess ->
                        if (loginSuccess) {
                            AuthService.createUser(this, username, email, userAvatar, avatarColor) { createSuccess ->
                                if (createSuccess) {
                                    val userDataChanged = Intent(BROADCAST_USER_DATA_CHANGED)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChanged)
                                    enableSpinner(false)
                                    println(UserDataService.name)
                                    finish()
                                } else {
                                    errorToast()
                                }
                            }
                        } else {
                            errorToast()
                        }
                    }
                } else {
                    errorToast()
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.form_error), Toast.LENGTH_SHORT).show()
            enableSpinner(false)
        }
    }

    fun errorToast(){
        Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            create_spinner.visibility = View.VISIBLE
        } else {
            create_spinner.visibility = View.INVISIBLE
        }
        create_createUser_btn.isEnabled = !enable
        background_color_btn.isEnabled = !enable
        create_avatar_imageView.isEnabled = !enable
    }

    fun hideKeyBoard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
