package com.sarah.twitterdupewebservice

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.sarah.twitterdupewebservice.Model.Ticket
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.tweets_ticket.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Profile : AppCompatActivity() {
    var ListTweets= ArrayList<Ticket>()
    var adapters:  myTweetsAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        txtName.text = SaveSettings.userName
        Picasso.with(this).load(SaveSettings.userImage).into(imgProfile)
        if(SaveSettings.userId!=""){
            btnfollow.visibility=View.GONE
        }else{
            btnfollow.visibility=View.VISIBLE
        }
        val saveSettings = SaveSettings(this)
        saveSettings.chkisLoogedIn()
        if(!SaveSettings.isLogOut){
            saveSettings.loadSettings()
            lvTweets.itemsCanFocus=true
            adapters=myTweetsAdapter(this,ListTweets)
            lvTweets.adapter=adapters
            getTweets()
        }
    }

    private fun getTweets() {
        val url="http://192.168.1.5/TwitterAndroidServer/TweetList.php?op=2&user_id="+SaveSettings.userId+"&StartFrom=0"
        MyAsyncTask().execute(url)
    }
    @SuppressLint("StaticFieldLeak")
    inner class MyAsyncTask: AsyncTask<String, String, String>() {

        override fun onPreExecute() {
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

        @SuppressLint("ResourceAsColor")
        override fun onProgressUpdate(vararg values: String?) {
            try{
                val json= JSONObject(values[0])
                Toast.makeText(applicationContext,json.getString("msg"), Toast.LENGTH_LONG).show()
                if(json.getString("msg")=="has tweet"){
                    ListTweets.clear()

                    //get tweets
                    val tweets = JSONArray(json.getString("info"))
                    for (i in 0 until tweets.length()){
                        val singleTweet = tweets.getJSONObject(i)
                        ListTweets.add(Ticket(singleTweet.getString("tweet_id"), singleTweet.getString("tweet_text"), singleTweet.getString("tweet_picture"),
                                singleTweet.getString("tweet_date"), singleTweet.getString("first_name"), singleTweet.getString("picture_path"), singleTweet.getString("user_id")))
                        adapters!!.notifyDataSetChanged()
                    }
                }else if(json.getString("msg")=="no tweet"){
                    ListTweets.clear()
                    adapters!!.notifyDataSetChanged()
                }
            }catch (ex:Exception){}
        }

        override fun onPostExecute(result: String?) {
        }
    }
    inner class myTweetsAdapter(context: Context, var listTweetsAdapter: ArrayList<Ticket>) : BaseAdapter() {
        var context: Context?= context
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val myTweet=listTweetsAdapter[position]
            val myView=layoutInflater.inflate(R.layout.tweets_ticket,null)
            myView.txt_tweet.text=myTweet.tweetText
            myView.txt_tweet_date.text = myTweet.tweetDate
            Picasso.with(context).load(myTweet.tweetimageURL).into(myView.tweet_picture)
            myView.txtUserName.text=myTweet.personName
            Picasso.with(context).load(myTweet.personImage).into(myView.picture_path)
            myView.txtUserName.setOnClickListener{
                val url="http://192.168.1.5/TwitterAndroidServer/TweetList.php?op=2&user_id="+myTweet.personId+"&StartFrom=0"
                MyAsyncTask().execute(url)
            }

            return myView
        }
        override fun getItem(position: Int): Any {
            return listTweetsAdapter[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getCount(): Int {
            return listTweetsAdapter.size
        }
    }
}
