package com.example.marinar.smackchat.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.marinar.smackchat.Controller.App
import com.example.marinar.smackchat.Model.Channel
import com.example.marinar.smackchat.Model.Message
import com.example.marinar.smackchat.R
import com.example.marinar.smackchat.Utils.ERROR_TAG
import com.example.marinar.smackchat.Utils.URL_GET_CHANNELS
import com.example.marinar.smackchat.Utils.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {
    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(context: Context, complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->
            try {
                for (index in 0 until response.length()) {
                    val channel = response.getJSONObject(index)
                    this.channels.add(Channel(channel.getString(context.getString(R.string.name_json)),
                            channel.getString(context.getString(R.string.description_json)),
                            channel.getString(context.getString(R.string.id_json))))
                }
                complete(true)

            } catch (exception: JSONException) {
                Log.e(ERROR_TAG, exception.localizedMessage)
                complete(false)
            }
        },
                Response.ErrorListener { error ->
                    Log.e(ERROR_TAG, "Could not retreive channels")
                    complete(false)
                }
        ) {

            override fun getBodyContentType(): String {
                return context.getString(R.string.content_type_json)
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put(context.getString(R.string.auth_header), "Bearer ${App.sharedPreferences.authToken}")
                return headers
            }
        }

        //Volley.newRequestQueue(context).add(channelsRequest)
        App.sharedPreferences.requestQueue.add(channelsRequest)
    }

    fun getMessages(context: Context, channelId: String, complete: (Boolean) -> Unit) {
        val url = "$URL_GET_MESSAGES$channelId"

        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
            clearMessages()
            try {
                for (index in 0 until response.length()) {
                    val message = response.getJSONObject(index)
                    val messageBody = message.getString("messageBody")
                    val channelId = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    this.messages.add(newMessage)
                }
                complete(true)
            } catch (exception: JSONException) {
                Log.e(ERROR_TAG, exception.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.e(ERROR_TAG, "Could not retreive channels")
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
        App.sharedPreferences.requestQueue.add(messagesRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChannels(){
        channels.clear()
    }
}