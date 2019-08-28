package com.example.marinar.smackchat.Utils

const val BASE_URL = "https://chatmenot.herokuapp.com/v1/"
const val SOCKET_URL = "https://chatmenot.herokuapp.com/"

const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_FIND_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel"

const val ERROR_TAG = "ERROR"

//Broadcast contstants
const val BROADCAST_USER_DATA_CHANGED = "BROADCAST_USER_DATA_CHANGED"