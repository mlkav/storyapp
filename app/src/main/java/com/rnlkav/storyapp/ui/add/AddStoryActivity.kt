package com.rnlkav.storyapp.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.rnlkav.storyapp.R
import com.rnlkav.storyapp.databinding.ActivityAddStoryBinding
import com.rnlkav.storyapp.ui.ViewModelFactory
import com.rnlkav.storyapp.utils.getImageUri
import com.rnlkav.storyapp.utils.reduceFileImage
import com.rnlkav.storyapp.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.apply {
            buttonGallery.setOnClickListener { startGallery() }
            buttonCamera.setOnClickListener { startCamera() }
            buttonAdd.setOnClickListener { uploadImage() }
            switchLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    getMyLocation()
                }
            }
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
        viewModel.tempCameraUri = uri
        launcherIntentCamera.launch(uri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { isSuccess ->
        if (isSuccess) {
            viewModel.setCurrentImageUri(viewModel.tempCameraUri)
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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLocation()
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLocation()
                }

                else -> {
                    binding.switchLocation.isChecked = false
                }
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    viewModel.setLocation(location)
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        getString(R.string.location_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.switchLocation.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun uploadImage() {
        viewModel.currentImageUri.value?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.edAddDescription.text.toString()
            if (description.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_field), Toast.LENGTH_SHORT)
                    .show()
                return
            }

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile,
            )

            var lat: RequestBody? = null
            var lon: RequestBody? = null
            if (binding.switchLocation.isChecked) {
                viewModel.location.value?.let {
                    lat = it.latitude.toString().toRequestBody("text/plain".toMediaType())
                    lon = it.longitude.toString().toRequestBody("text/plain".toMediaType())
                }
            }

            viewModel.uploadImage(multipartBody, requestBody, lat, lon)
        } ?: Toast.makeText(this, getString(R.string.empty_image_warning), Toast.LENGTH_SHORT)
            .show()
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
