package com.example.marinar.smackchat.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.marinar.smackchat.Model.Message
import com.example.marinar.smackchat.R
import com.example.marinar.smackchat.Services.UserDataService
import com.example.marinar.smackchat.Utils.ERROR_TAG
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages : ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(context).inflate(R.layout.message_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMessage(context, messages[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImageView = itemView.findViewById<ImageView>(R.id.message_user_imageView)
        val timeStamp = itemView.findViewById<TextView>(R.id.message_timestamp_tv)
        val userName = itemView.findViewById<TextView>(R.id.message_username_tv)
        val messageBody = itemView.findViewById<TextView>(R.id.message_body_tv)

        fun bindMessage (context: Context, message: Message){
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)
            userImageView.setImageResource(resourceId)
            userImageView.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))
            userName.text = message.userName
            timeStamp.text = returnDateString(message.timeStamp)
            messageBody.text = message.message
        }

        fun returnDateString(isoString: String): String {

            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())//format to parse the string
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()
            try {
                convertedDate = isoFormatter.parse(isoString)

            } catch (exception: ParseException){
                Log.d(ERROR_TAG, context.getString(R.string.parse_date_error_message))
            }

            val outDateString = SimpleDateFormat("EEE, d MMM yyyy,  h:mm a", Locale.getDefault())

            return outDateString.format(convertedDate)
        }
    }
}