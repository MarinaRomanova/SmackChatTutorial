package com.example.marinar.smackchat

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profiledefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
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

    }
}
