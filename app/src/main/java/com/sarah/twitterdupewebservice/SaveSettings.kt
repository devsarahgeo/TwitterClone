package com.sarah.twitterdupewebservice

import android.content.Context
import android.content.Intent


class SaveSettings(context:Context) {
    val context= context
    val sharedPreferences=context.getSharedPreferences("myref",Context.MODE_PRIVATE)
    fun saveSettings(userID:String,userName:String,picture:String){
        val editor = sharedPreferences.edit()
        editor.putString("userID",userID)
        editor.putString("username",userName)
        editor.putString("userimage",picture)
        editor.apply()
        loadSettings()
    }
    fun saveFollow(follow:String){
        val editor = sharedPreferences.edit()
        editor.putString("following",follow)
        editor.apply()
        loadFollow()
    }
    fun loadFollow(){
        follow = sharedPreferences.getString("following","0")
    }
    fun loadSettings(){
        userId = sharedPreferences.getString("userID","0")
        userName = sharedPreferences.getString("username","0")
        userImage = sharedPreferences.getString("userimage","0")
        if(userId=="0"){
            val intent = Intent(context,Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
    fun isLoggedOut(){
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLogOut",false)
        editor.apply()
        val intent = Intent(context,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    fun chkisLoogedIn(){
        isLogOut = sharedPreferences.getBoolean("isLogOut",false)
        if(isLogOut == true){
            logout(context)
        }else{
            return
        }
    }
    fun logout(context: Context){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.putBoolean("isLogOut",true)
        editor.apply()
        val intent = Intent(context,Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        context.startActivity(intent)
    }
    companion object {
        var userImage = ""
        var userName = ""
        var userId = ""
        var isLogOut=false
        var follow = "FOLLOW+"
    }

}