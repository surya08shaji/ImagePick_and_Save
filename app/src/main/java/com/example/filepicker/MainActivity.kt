package com.example.filepicker

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    private var dir = File(File(Environment.getExternalStorageDirectory(), "/myImage/"), "abc.jpg")

    private lateinit var imageView : ImageView
    private lateinit var button : Button
    private lateinit var  removeButton:Button
    private lateinit var  saveButton:Button
    private val pickImage = 100
    private var imageUri :Uri? = null

    private var bytearrayOutputStream: ByteArrayOutputStream? = null
    private lateinit var file: File
    private lateinit var fileOutputStream: FileOutputStream

    private lateinit var bitmap:Bitmap
    private var photoFile: File? = null

    private val photoFileName = "photo.jpg"

    private lateinit var outputStream: OutputStream


    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image)
        button = findViewById(R.id.selectImage)
        saveButton =findViewById(R.id.saveBtn)
        removeButton =findViewById(R.id.removeBtn)


        val contentValues = ContentValues()

        button.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery,pickImage)
        }

        saveButton.setOnClickListener {

//            val popupMenu = PopupMenu(this@MainActivity, saveButton)
//            popupMenu.menuInflater.inflate(R.menu.popup_menu,popupMenu.menu)
//            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
//                when(item.itemId) {
//                    R.id.newFolder ->
//                        Toast.makeText(this@MainActivity, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
//                    R.id.localFolder ->
//                        Toast.makeText(this@MainActivity, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
//                }
//                true
//            })
//            popupMenu.show()


            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.popup_window, null)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)

            popupView.setOnClickListener {
                askForPermissions()

                val resolver = contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
                Log.e("contentValues...."," $contentValues")
                outputStream = resolver.openOutputStream(uri!!)!!


//                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

                try {
                    if (Build.VERSION.SDK_INT > 27) {
                        val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                        bitmap = ImageDecoder.decodeBitmap(source)
                        Log.e("SDK_INT > 27....","$bitmap")

                        photoFile = getPhotoFileUri(photoFileName)
                        Log.e("photoFile....","$photoFile")

                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,imageUri!!)
                        Log.e("SDK_INT < 27....","$bitmap")

                        photoFile = getPhotoFileUri(photoFileName)
                        Log.e("photoFile....","$photoFile")
                    }
                } catch (e:Exception){
                    e.printStackTrace()
                    Log.e("Exception....","${e.message}")
                }




                popupWindow.dismiss()

                if (button.visibility == View.VISIBLE){
                    button.visibility = View.GONE
                    saveButton.visibility = View.VISIBLE
                    removeButton.visibility = View.VISIBLE
                } else {
                    button.visibility = View.VISIBLE
                    saveButton.visibility = View.GONE
                    removeButton.visibility = View.GONE
                    imageView.setImageResource(0)
                }
            }
        }

        removeButton.setOnClickListener {
            imageView.setImageResource(0)

            if (button.visibility == View.VISIBLE){
                button.visibility = View.GONE
                saveButton.visibility = View.VISIBLE
                removeButton.visibility = View.VISIBLE
            } else {
                button.visibility = View.VISIBLE
                saveButton.visibility = View.GONE
                removeButton.visibility = View.GONE
            }
        }
    }

    private fun getPhotoFileUri(fileName: String): File {
        file = File(Environment.getExternalStorageDirectory().toString() + fileName)

        Log.e("path...."," $file")

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG,60,outputStream)
//            val bitMapData:ByteArray = bytearrayOutputStream!!.toByteArray()
//            file.createNewFile()
//            fileOutputStream = FileOutputStream(file)
//            fileOutputStream.write(bitMapData)
////            fileOutputStream.flush()
//            fileOutputStream.close()
            Log.e("file....","file... $file")
            Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Except....","${e.message}")
            Toast.makeText(this, "Image Saved UnSuccessfully", Toast.LENGTH_LONG).show()
            imageView.setImageResource(0)
        }
        return file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage){
            imageUri = data?.data
            imageView.setImageURI(imageUri)

            if (button.visibility == View.VISIBLE){
                button.visibility = View.GONE
                saveButton.visibility = View.VISIBLE
                removeButton.visibility = View.VISIBLE
            } else {
                button.visibility = View.VISIBLE
                saveButton.visibility = View.GONE
                removeButton.visibility = View.GONE
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                createDir()
            }
        }
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                return
            }
            createDir()
        }
    }

    private fun createDir() {
        if (!dir.exists()) {
            dir.mkdirs()
            dir.createNewFile()
        }
    }
}
