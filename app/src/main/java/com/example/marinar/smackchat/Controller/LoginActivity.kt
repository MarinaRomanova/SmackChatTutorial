package com.example.marinar.smackchat.Controller

import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.marinar.smackchat.R
import com.example.marinar.smackchat.Services.AuthService
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_spinner.visibility = View.INVISIBLE
    }

    fun onLoginLoginClicked(view: View){
        enableSpinner(true)
        val email = login_email_editText.text.toString()
        val password = login_password_editText.text.toString()
        hideKeyBoard()
        if(email.isNotEmpty() && password.isNotEmpty()){
            AuthService.loginUser(this, email, password){loginSuccess ->
                if(loginSuccess){
                    AuthService.findUserByEmail(this) { findSuccess ->
                        if(findSuccess){
                            enableSpinner(false)
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
            Toast.makeText(this, getString(R.string.form_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun onLoginRegisterClicked(view: View){
        startActivity(Intent(this, CreateUserActivity::class.java))
        finish()
    }
    fun errorToast(){
        Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            login_spinner.visibility = View.VISIBLE
        } else {
            login_spinner.visibility = View.INVISIBLE
        }
        login_login_btn.isEnabled = !enable
        login_register_btn.isEnabled = !enable
    }

    fun hideKeyBoard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}

