package com.example.marinar.smackchat.Services

import android.graphics.Color
import com.example.marinar.smackchat.Controller.App
import java.util.*

object UserDataService {

    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun returnAvatarColor(rgb: String) : Int {
        val colors = rgb.replace("[", "")
                .replace("]", "")
                .replace(",", "")
        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(colors)
        if(scanner.hasNext()){
            r = (scanner.nextDouble()* 255).toInt()
            g = (scanner.nextDouble()* 255).toInt()
            b = (scanner.nextDouble()* 255).toInt()
        }
        return  Color.rgb(r,g,b)
    }

    fun logout(){
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""

//        AuthService.authToken = ""
//        AuthService.userEmail = ""
//        AuthService.isLoggedIn = false
        App.sharedPreferences.authToken = ""
        App.sharedPreferences.userEmail = ""
        App.sharedPreferences.isLoggedIn = false
    }
}