package com.fajar.writeocr.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fajar.writeocr.R
import com.fajar.writeocr.databinding.ActivityResultBinding
import java.io.File

class ResultActivity : AppCompatActivity() {
    private lateinit var binding : ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.hide()
        showLoading(true)
        val imageFile = intent.getSerializableExtra(EXTRA_IMAGE_FILE) as File

        binding.ivScanPreview.setImageURI(Uri.fromFile(imageFile))
        binding.tvLabelPendeteksian.text = intent.getStringExtra(EXTRA_RESULT)
        showLoading(false)
        // Back to main activity
        binding.include.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
    companion object {
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_IMAGE_FILE = "extra_image_file"
    }
}