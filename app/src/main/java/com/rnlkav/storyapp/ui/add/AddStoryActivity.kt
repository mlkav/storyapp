package com.rnlkav.storyapp.ui.add

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.databinding.ActivityAddStoryBinding
import com.rnlkav.storyapp.ui.ViewModelFactory
import com.rnlkav.storyapp.utils.getImageUri
import com.rnlkav.storyapp.utils.reduceFileImage
import com.rnlkav.storyapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding

    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            buttonGallery.setOnClickListener { startGallery() }
            buttonCamera.setOnClickListener { startCamera() }
            buttonAdd.setOnClickListener { uploadImage() }
        }

        viewModel.currentImageUri.observe(this) { uri ->
            if (uri != null) {
                showImage(uri)
            }
        }

        viewModel.uploadResponse.observe(this) { response ->
            if (response.error == false) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModel.message.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCurrentImageUri(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        val uri = getImageUri(this)
        viewModel.setCurrentImageUri(uri)
        launcherIntentCamera.launch(uri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { isSuccess ->
        if (!isSuccess) {
            viewModel.setCurrentImageUri(null)
        }
    }

    private fun showImage(uri: Uri) {
        Log.d("Image URI", "showImage: $uri")

        val isAvailable = checkImageAvailability(uri)

        if (isAvailable) {
            loadImage(uri)
        } else {
            Log.w("Image URI", "File not ready, retrying... $uri")
            binding.root.postDelayed({
                if (checkImageAvailability(uri)) {
                    loadImage(uri)
                } else {
                    Log.e("Image URI", "File still not available after retry: $uri")
                }
            }, 500)
        }
    }

    private fun checkImageAvailability(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.use { it.available() > 0 } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_place_holder)
            .error(R.drawable.ic_place_holder)
            .into(binding.ivAddPhoto)
    }

    private fun uploadImage() {
        viewModel.currentImageUri.value?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.edAddDescription.text.toString()
            if (description.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_field), Toast.LENGTH_SHORT).show()
                return
            }

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile,
            )

            viewModel.uploadImage(multipartBody, requestBody)
        } ?: Toast.makeText(this, getString(R.string.empty_image_warning), Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            buttonAdd.isEnabled = !isLoading
            buttonCamera.isEnabled = !isLoading
            buttonGallery.isEnabled = !isLoading
        }
    }
}
