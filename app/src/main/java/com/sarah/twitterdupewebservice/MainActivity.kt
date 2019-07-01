package com.sarah.twitterdupewebservice

import android.os.Bundle
import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweets_ticket.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.sarah.twitterdupewebservice.Model.SearchDataModel
import com.sarah.twitterdupewebservice.Model.Ticket
import com.sarah.twitterdupewebservice.SearchRecyclerView.SearchAdapter
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var ListTweets= ArrayList<Ticket>()
    var searchLists = ArrayList<SearchDataModel>()
    lateinit var mAdapter: SearchAdapter
    lateinit var recyclerView: RecyclerView

    var adapters:myTweetsAdapter?=null
    lateinit var tweetText:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.rvSearch)
        mAdapter = SearchAdapter(this,searchLists)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = mLayoutManager

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        val headerView = nav_view.getHeaderView(0)
        headerView.tvName.text = SaveSettings.userName
        nav_view.setNavigationItemSelectedListener(this)
        val saveSettings = SaveSettings(this)
        saveSettings.chkisLoogedIn()
        if(!SaveSettings.isLogOut){
            saveSettings.loadSettings()
            ListTweets.add(Ticket("0", "him", "url", "add", "", "", ""))
            lvTweets.itemsCanFocus=true
            adapters=myTweetsAdapter(this,ListTweets)
            lvTweets.adapter=adapters
            getTweets("0",0)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        val sv: SearchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        val sm= getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(sm.getSearchableInfo(componentName))
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                lvsearch.visibility=View.VISIBLE

                searchDatabase(query,0)
                recyclerView.adapter = mAdapter
                (mAdapter as SearchAdapter).filter.filter(query)
                return false
            }
            override fun onQueryTextChange(query: String): Boolean {
                lvsearch.visibility=View.VISIBLE
                searchDatabase(query,0)
                recyclerView.adapter = mAdapter
                (mAdapter as SearchAdapter).filter.filter(query)
                return false
            }
        })
        sv.setOnCloseListener(object:SearchView.OnCloseListener {
            override fun onClose(): Boolean {
               lvsearch.visibility=View.GONE
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.addHome->{
                //Got to home paage
                getTweets("0",0)

            }
            R.id.logout->{
                val saveSettings = SaveSettings(this)
                saveSettings.logout(this@MainActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_profile -> {
                val intent =Intent(this@MainActivity,Profile::class.java)
                startActivity(intent)
            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun searchDatabase(searchText:String,startFrom:Int) {
        val SearchText = URLEncoder.encode(searchText,"utf-8")
        val url="http://192.168.1.5/TwitterAndroidServer/login.php?op=2&query="+SearchText+"&StartFrom="+startFrom+""
        MyAsyncTask().execute(url)
    }
    private fun getTweets(searchText:String,startFrom:Int) {
        val url="http://192.168.1.5/TwitterAndroidServer/TweetList.php?op=1&user_id="+searchText+"&user_id="+SaveSettings.userId+"&StartFrom="+startFrom+""
        MyAsyncTask().execute(url)
    }

    val PICK_IMAGE=200


    private fun loadimage() {
        val intent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==PICK_IMAGE && data!=null && resultCode== Activity.RESULT_OK){
            val selectedImage=data.data
            val filePathColumn= arrayOf(MediaStore.Images.Media.DATA)
            val cursor=contentResolver.query(selectedImage,filePathColumn,null,null,null)
            cursor.moveToFirst()
            val columnIndex=cursor.getColumnIndex(filePathColumn[0])
            val picturePath=cursor.getString(columnIndex)
            cursor.close()
            UploadImage(BitmapFactory.decodeFile(picturePath))
        }
    }

    var DownloadUrl:String?="noimage"
    fun UploadImage(bitmap: Bitmap){
        ListTweets.add(0, Ticket("0", "him", "url", "loading", "", "", ""))
        adapters!!.notifyDataSetChanged()
        val storage= FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitterdupewebservice.appspot.com")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dateObj= Date()
        val imagePath = SaveSettings.userId + "." + df.format(dateObj) + ".jpg"
        val imageRef=storageRef.child("imagePost/"+imagePath)
        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data=baos.toByteArray()
        val uploadTask=imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"Failed", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener{taskSnapshot ->
            DownloadUrl=taskSnapshot.downloadUrl!!.toString()
            ListTweets.removeAt(0)
            adapters!!.notifyDataSetChanged()
        }
    }

    inner class myTweetsAdapter(context: Context, var listTweetsAdapter: ArrayList<Ticket>) : BaseAdapter() {

        var context:Context?= context


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myTweet=listTweetsAdapter[position]

            if(myTweet.tweetDate.equals("add")){
                var myView=layoutInflater.inflate(R.layout.add_ticket,null)
                myView.ivAttach.setOnClickListener {
                    checkPermission()
                }
                myView.ivPost.setOnClickListener {
                    val followtxt = "FOLLOW+"
                    ListTweets.add(0, Ticket("0", "him", "url", "loading", "", "", ""))
                    adapters!!.notifyDataSetChanged()
                    tweetText = URLEncoder.encode(myView.etPost.text.toString(),"utf-8")
                    DownloadUrl = URLEncoder.encode(DownloadUrl,"utf-8")
                    val url="http://192.168.1.5/TwitterAndroidServer/tweets.php?op=1&user_id="+SaveSettings.userId+"&tweet_text="+tweetText+"&tweet_picture="+DownloadUrl+""
                    MyAsyncTask().execute(url)
                }
                return myView
            }else  if(myTweet.tweetDate.equals("loading")) {
                val myView = layoutInflater.inflate(R.layout.loading_ticket, null)
                return myView
            } else{
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

        override fun onProgressUpdate(vararg values: String?) {
            try{
                val json= JSONObject(values[0])
                Toast.makeText(applicationContext,json.getString("msg"),Toast.LENGTH_LONG).show()
                if(json.getString("msg")=="tweet is added"){
                    DownloadUrl = "noImage"
                    ListTweets.removeAt(0)
                    adapters!!.notifyDataSetChanged()
                }else if(json.getString("msg")=="following is updated"){

                }else if(json.getString("msg")=="pass login"){
                    searchLists.clear()
                    //get users
                    val users = JSONArray(json.getString("info"))
                    for (i in 0 until users.length()){
                        val singleTweet = users.getJSONObject(i)
                        searchLists.add(SearchDataModel(singleTweet.getString("user_id"),singleTweet.getString("picture_path"),singleTweet.getString("first_name")))
                        adapters!!.notifyDataSetChanged()
                    }
                } else if(json.getString("msg")=="has tweet"){
                    ListTweets.clear()
                    ListTweets.add(Ticket("0", "him", "url", "add", "", "", ""))
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
                    ListTweets.add(Ticket("0", "him", "url", "add", "", "", ""))
                    adapters!!.notifyDataSetChanged()

                }
            }catch (ex:Exception){}
        }

        override fun onPostExecute(result: String?) {
        }
    }
    val READIMAGE:Int=100
    fun checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)
                return
            }
        }
        loadimage()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            READIMAGE->{
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    loadimage()
                }else{
                    Toast.makeText(this,"Cannot access the image", Toast.LENGTH_LONG).show()
                }
            }else-> super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        }
    }
}




