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
import kotlinx.android.synthetic.main.activity_other_users_profile.*
import kotlinx.android.synthetic.main.tweets_ticket.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OtherUsersProfile : AppCompatActivity() {
    var other_user_id:String = ""
    var ListTweets= ArrayList<Ticket>()
    var adapters:   myTweetsAdapter?=null
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_users_profile)
        val name=intent.extras.getString("name","")
        val image=intent.extras.getString("image","")
        other_user_id=intent.extras.getString("users_user_id","")
        val saveSettings = SaveSettings(this)
        saveSettings.chkisLoogedIn()
        if(!SaveSettings.isLogOut){
            saveSettings.loadSettings()
            lvTweets.itemsCanFocus=true
            adapters=myTweetsAdapter(this,ListTweets)
            lvTweets.adapter=adapters
            getTweets()
        }

        tvName.text = name
        Picasso.with(this).load(image).into(ivProfile)

        if(SaveSettings.userName!=name){
            btfollow.visibility=View.VISIBLE
        }else{
            btfollow.visibility=View.GONE

        }
        getFollow()
        followme.setOnClickListener{
            val followtxt = "Following"
            val unfollowtxt  = "Follow"
            if(followme.text==followtxt){
                followme.text=unfollowtxt
                followme.setBackgroundResource(R.drawable.round_blue_border)
                followme.setTextColor(R.color.twitterblue)
                val url="http://192.168.1.5/TwitterAndroidServer/UserFollowing.php?op=2&user_id="+SaveSettings.userId+"&following_user_id="+other_user_id+"&following_txt="+unfollowtxt+""
                MyAsyncTask().execute(url)
            }else{
                followme.text=followtxt
                followme.setBackgroundResource(R.drawable.bluecolor)
                followme.setTextColor(R.color.white)
                val url="http://192.168.1.5/TwitterAndroidServer/UserFollowing.php?op=1&user_id="+SaveSettings.userId+"&following_user_id="+other_user_id+"&following_txt="+followtxt+""
                MyAsyncTask().execute(url)
            }
        }
    }

    private fun getTweets() {
        val url="http://192.168.1.5/TwitterAndroidServer/TweetList.php?op=2&user_id="+other_user_id+"&StartFrom=0"
        MyAsyncTask().execute(url)
    }
    private fun getFollow() {
        val url="http://192.168.1.5/TwitterAndroidServer/isFollowing.php?user_id="+SaveSettings.userId+"&following_user_id="+other_user_id+"&following_txt="+followme.text+""
        MyAsyncTask().execute(url)
    }

    @SuppressLint("StaticFieldLeak")
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

        @SuppressLint("ResourceAsColor")
        override fun onProgressUpdate(vararg values: String?) {
            try{
                val json= JSONObject(values[0])
                Toast.makeText(applicationContext,json.getString("msg"), Toast.LENGTH_LONG).show()
               if(json.getString("msg")=="following is updated"){

                }else if(json.getString("msg")=="is subscribed"){
                   val status = JSONArray(json.getString("info"))
                   for (i in 0 until status.length()) {
                       val followStatus = status.getJSONObject(i)
                       if (followStatus.getString("following_txt") == "Following") {
                           followme.text = followStatus.getString("following_txt")
                           followme.setBackgroundResource(R.drawable.bluecolor)
                           followme.setTextColor(R.color.white)
                       }else{
                           followme.text =followStatus.getString("following_txt")
                           followme.setBackgroundResource(R.drawable.round_blue_border)
                           followme.setTextColor(R.color.twitterblue)
                       }
                   }

               }else if(json.getString("msg")=="has tweet"){
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
//                myView.txtfollow.text=myTweet.follow
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

