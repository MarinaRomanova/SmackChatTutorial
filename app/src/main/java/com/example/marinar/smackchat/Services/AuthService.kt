package com.example.marinar.smackchat.Services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.marinar.smackchat.Controller.App
import com.example.marinar.smackchat.R
import com.example.marinar.smackchat.Utils.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

//    var isLoggedIn = false
//    var userEmail = ""
//    var authToken = ""

    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            println(response)
            complete(true)
        }, Response.ErrorListener { error ->
            Log.e(ERROR_TAG, "Couldn't register user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return context.getString(R.string.content_type_json)
            }

            override fun getBody(): ByteArray {
                return accountLogInToString(context, email, password).toByteArray()
            }
        }

//        Volley.newRequestQueue(context).add(registerRequest)
        App.sharedPreferences.requestQueue.add(registerRequest)
    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit){

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->
            try {
                App.sharedPreferences.userEmail = response.getString(context.getString(R.string.user_json))
                App.sharedPreferences.authToken = response.getString(context.getString(R.string.token))
                App.sharedPreferences.isLoggedIn = true;
                complete(true)
            } catch (error: JSONException){
                Log.e(ERROR_TAG, "JSON Exception: ${error.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener {error ->
            Log.e(ERROR_TAG, "Couldn't login user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return context.getString(R.string.content_type_json)
            }

            override fun getBody(): ByteArray {
                return accountLogInToString(context, email, password).toByteArray()
            }
        }

        //Volley.newRequestQueue(context).add(loginRequest)
        App.sharedPreferences.requestQueue.add(loginRequest)
    }

    fun createUser(context: Context, name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put(context.getString(R.string.name_json), name)
        jsonBody.put(context.getString(R.string.email_json), email)
        jsonBody.put(context.getString(R.string.avatar_name_json), avatarName)
        jsonBody.put(context.getString(R.string.avatar_color_json), avatarColor)
        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest( Method.POST, URL_CREATE_USER, null, Response.Listener { response ->
            try {
                UserDataService.name = response.getString(context.getString(R.string.name_json))
                UserDataService.email = response.getString(context.getString(R.string.email_json))
                UserDataService.avatarColor = response.getString(context.getString(R.string.avatar_color_json))
                UserDataService.avatarName = response.getString(context.getString(R.string.avatar_name_json))
                UserDataService.id = response.getString(context.getString(R.string.id_json))
                complete(true)
            } catch (exception : JSONException){
                Log.e(ERROR_TAG, "JSON Exception: ${exception.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.e(ERROR_TAG, "Couldn't create user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return context.getString(R.string.content_type_json)
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put(context.getString(R.string.auth_header), "Bearer ${App.sharedPreferences.authToken}")
                return headers
            }
        }

        //Volley.newRequestQueue(context).add(createRequest)
        App.sharedPreferences.requestQueue.add(createRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit){
        val findUserRequest = object :JsonObjectRequest(Method.GET, "$URL_FIND_USER${App.sharedPreferences.userEmail}", null, Response.Listener{ response ->
            try {
                UserDataService.name = response.getString(context.getString(R.string.name_json))
                UserDataService.email = response.getString(context.getString(R.string.email_json))
                UserDataService.avatarColor = response.getString(context.getString(R.string.avatar_color_json))
                UserDataService.avatarName = response.getString(context.getString(R.string.avatar_name_json))
                UserDataService.id = response.getString(context.getString(R.string.id_json))

                val userDataChanged = Intent(BROADCAST_USER_DATA_CHANGED)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChanged)
                complete(true)
            } catch (exception : JSONException){
                Log.e(ERROR_TAG, "JSON Exception: ${exception.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.e(ERROR_TAG, "Couldn't find user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return context.getString(R.string.content_type_json)
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put(context.getString(R.string.auth_header), "Bearer ${App.sharedPreferences.authToken}")
                return headers
            }
        }

        //Volley.newRequestQueue(context).add(findUserRequest)
        App.sharedPreferences.requestQueue.add(findUserRequest)

    }

    fun accountLogInToString( context: Context, email: String, password: String) : String {
        val jsonBody = JSONObject()
        jsonBody.put(context.getString(R.string.email_json), email)
        jsonBody.put(context.getString(R.string.password_json), password)
        return jsonBody.toString()
    }
}