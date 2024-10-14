package com.fajar.writeocr.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.fajar.writeocr.R
import com.fajar.writeocr.createCustomTempFile
import com.fajar.writeocr.data.api.Api
import com.fajar.writeocr.data.api.Retro
import com.fajar.writeocr.data.model.response.ScanResponse
import com.fajar.writeocr.databinding.ActivityMainBinding
import com.fajar.writeocr.rotateBitmap
import com.fajar.writeocr.uriToFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS_CAMERA.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS_CAMERA,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.ivScanPreview.setOnClickListener {
            startCamera()
        }

        binding.btnGetFromGallery.setOnClickListener {
            startGallery()
        }

        binding.btnGetFromCamera.setOnClickListener {
            startCamera()
        }

        binding.btnScannow.setOnClickListener {
            if (getFile != null && getFile!!.exists()) {
                startScan()
            } else {
                Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private var getFile: File? = null

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCamera.launch(intent)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val result = BitmapFactory.decodeFile(myFile.path)
                binding.ivScanPreview.background = null
                binding.ivScanPreview.setImageBitmap(result)
            } else {
                val result = rotateBitmap(
                    BitmapFactory.decodeFile(myFile.path),
                    isBackCamera
                )
                binding.ivScanPreview.background = null
                binding.ivScanPreview.setImageBitmap(result)
            }
        }
    }
    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }
    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            contentResolver
            val myFile = uriToFile(selectedImg, this@MainActivity)

            getFile = myFile
            binding.ivScanPreview.setImageURI(selectedImg)
        }
    }
    private fun startScan() {
        showLoading(true)
        if (getFile != null) {
            val file = getFile as File

            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestImageFile
            )

            val retro = Retro().getRetroClientInstance().create(Api::class.java)
            retro.scanImage(imageMultipart).enqueue(object : Callback<ScanResponse> {
                override fun onResponse(
                    call: Call<ScanResponse>,
                    response: Response<ScanResponse>
                ) {
                    if (response.isSuccessful && (response.body()?.text ?: "") != "No objects detected") {
                        showLoading(false)
                        val responseBody = response.body()
                        Log.d("TAG", "onResponse: ${responseBody?.text}")
                        intent = Intent(this@MainActivity, ResultActivity::class.java).also {
                            it.putExtra(ResultActivity.EXTRA_RESULT, responseBody?.text)
                            it.putExtra(ResultActivity.EXTRA_IMAGE_FILE, file)
                        }
                        Log.d("TAG", "onResponse: ${responseBody?.text}")
                        startActivity(intent)
                    } else {
                        showLoading(false)
                        Toast.makeText(
                            this@MainActivity,
                            resources.getString(R.string.image_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.terjadi_kesalahan),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        } else {
            showLoading(false)
            Toast.makeText(this, resources.getString(R.string.iv_empty), Toast.LENGTH_SHORT).show()
            Log.d("TAG", "startScan: image empty")
        }
    }
    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}