package com.sarah.twitterdupewebservice

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
    fun buLogin(view:View){
        val url="http://192.168.1.5/TwitterAndroidServer/login.php?op=1&email="+etEmail.text.toString()+"&password="+etPassword.text.toString()+""
        MyAsyncTask().execute(url)
        val saveSettings=SaveSettings(this)
        saveSettings.isLoggedOut()

    }

    fun buRegister(view:View){
        val intent = Intent(this@Login,Register::class.java)
        startActivity(intent)
    }
    inner class MyAsyncTask: AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            //Before task started
        }
        override fun doInBackground(vararg p0: String?): String {
            try {

                val url= URL(p0[0])

                val urlConnect=url.openConnection() as HttpURLConnection
                urlConnect.connectTimeout=7000
                val op=Operations()

                val inString= op.ConvertStreamToString(urlConnect.inputStream)
                //Cannot access to ui
                publishProgress(inString)
            }catch (ex:Exception){
                print("ex:$ex")}


            return " "

        }

        override fun onProgressUpdate(vararg values: String?) {
            try{
                val json= JSONObject(values[0])
                if(json.getString("msg")=="pass login"){
                    val userInfo = JSONArray(json.getString("info"))
                    val userCredentials = userInfo.getJSONObject(0)
                    val userId = userCredentials.getString("user_id")
                    val username = userCredentials.getString("first_name")
                    val picture = userCredentials.getString("picture_path")
                    Toast.makeText(applicationContext,userCredentials.getString("first_name"), Toast.LENGTH_LONG).show()
                    val saveSettings  = SaveSettings(applicationContext)
                    saveSettings.saveSettings(userId,username,picture)
                    finish()

                }else{
                    Toast.makeText(applicationContext,json.getString("msg"), Toast.LENGTH_LONG).show()
                }

            }catch (ex:Exception){}
        }

        override fun onPostExecute(result: String?) {

            //after task done
        }
    }

//    override fun onBackPressed() {
////        super.onBackPressed()
//    }
}
