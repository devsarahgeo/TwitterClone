package com.sarah.twitterdupewebservice

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class Register : AppCompatActivity() {
    var mAuth:FirebaseAuth?=null
    var database:FirebaseDatabase?=null
    var reference:DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database!!.reference
        signInAnonmously()
        ivProfilePic.setOnClickListener(View.OnClickListener {
            checkPermission()

        })

    }
    val READIMAGE:Int=100
    fun checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)
                return
            }
        }
        loadImage()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            READIMAGE->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }else{
                    Toast.makeText(this,"Cannot access the image", Toast.LENGTH_LONG).show()
                }
            }else-> super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        }
    }

    val PICK_IMAGE=200
    private fun loadImage() {

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
            ivProfilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    private fun signInAnonmously() {
        mAuth!!.signInAnonymously().addOnCompleteListener(this) { task ->
            Log.d("Logininfo",task.isSuccessful.toString())
        }
    }
    fun saveImageInFirebase(){
        val currentUser= mAuth!!.currentUser
        val email=currentUser!!.email.toString()
        val storage= FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitterdupewebservice.appspot.com")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dateObj= Date()
        val imagePath = SplitString(email) + "." + df.format(dateObj) + ".jpg"
        val imageRef=storageRef.child("images/"+imagePath)
        ivProfilePic.isDrawingCacheEnabled=true
        ivProfilePic.buildDrawingCache()

        val drawable = ivProfilePic.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data=baos.toByteArray()
        val uploadTask=imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"Failed",Toast.LENGTH_LONG).show()
        }.addOnSuccessListener{taskSnapshot ->
            var DownloadUrl=taskSnapshot.downloadUrl.toString()
            DownloadUrl = URLEncoder.encode(DownloadUrl,"utf-8")
            val name = URLEncoder.encode(etName.text.toString(),"utf-8")
            val url="http://192.168.1.5/TwitterAndroidServer/register.php?first_name="+name+"&email="+etEmail.text.toString()+"&password="+etPassword.text.toString()+"&picture_path="+DownloadUrl+""
            MyAsyncTask().execute(url)

        }

    }
    fun SplitString(str:String):String{
        val name=str.split("@")
        return name[0]
    }

    fun buRegister(view:View){
        btnRegister.isEnabled = false
        saveImageInFirebase()
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
                Toast.makeText(applicationContext,json.getString("msg"),Toast.LENGTH_LONG).show()
                if(json.getString("msg")=="user is added"){
                    finish()
                }else{
                    btnRegister.isEnabled = true
                }

            }catch (ex:Exception){}
        }

        override fun onPostExecute(result: String?) {

            //after task done
        }


    }
}
