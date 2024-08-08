package com.example.textrecognition

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.textrecognition.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    companion object{
        private const val IMAGE_REQUEST_CODE= 100
        private const val STORAGE_REQUEST_CODE= 101
    }

    private var imageUrl:Uri?=null

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraPermissions = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        binding.shapeImg.setOnClickListener{
            showInpImageDialog()
        }
    }

    private fun showInpImageDialog() {

    }

    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
    }
    private fun galleryActivity(){
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUrl = data?.data

                binding.shapeImg.setImageURI(imageUrl)
            }
            else{

            }

        }
    }
}