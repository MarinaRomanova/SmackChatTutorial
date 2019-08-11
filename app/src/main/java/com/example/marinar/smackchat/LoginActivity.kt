package com.example.marinar.smackchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onLoginLoginClicked(view: View){

    }

    fun onLoginRegisterClicked(view: View){
        startActivity(Intent(this, CreateUserActivity::class.java))
    }
}

