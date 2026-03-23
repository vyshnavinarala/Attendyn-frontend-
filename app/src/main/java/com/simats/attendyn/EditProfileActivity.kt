package com.simats.attendyn

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.databinding.ActivityEditProfileBinding
import com.simats.attendyn.model.NameRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedImageBitmap: Bitmap? = null
    private var selectedImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            binding.ivProfileEdit.setPadding(0, 0, 0, 0)
            binding.ivProfileEdit.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.ivProfileEdit.setImageBitmap(it)
            binding.ivProfileEdit.imageTintList = null
            selectedImageBitmap = it
            selectedImageUri = null
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.ivProfileEdit.setPadding(0, 0, 0, 0)
            binding.ivProfileEdit.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.ivProfileEdit.setImageURI(it)
            binding.ivProfileEdit.imageTintList = null
            selectedImageUri = it
            selectedImageBitmap = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentData()
        setupListeners()
    }

    private fun loadCurrentData() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val username = prefs.getString("USER_NAME", "User")
        val email = prefs.getString("USER_EMAIL", "abc@example.com")
        val photoPath = prefs.getString("USER_PHOTO_PATH", null)

        binding.etName.setText(username)
        binding.tvEmail.text = email

        if (photoPath != null) {
            val file = java.io.File(photoPath)
            if (file.exists()) {
                binding.ivProfileEdit.setPadding(0, 0, 0, 0)
                binding.ivProfileEdit.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                binding.ivProfileEdit.setImageURI(Uri.fromFile(file))
                binding.ivProfileEdit.imageTintList = null
            } else {
                checkFallbackAndSetDefault(prefs)
            }
        } else {
            checkFallbackAndSetDefault(prefs)
        }
    }

    private fun checkFallbackAndSetDefault(prefs: android.content.SharedPreferences) {
        val fallbackFile = java.io.File(filesDir, "profile_image.jpg")
        if (fallbackFile.exists()) {
            binding.ivProfileEdit.setPadding(0, 0, 0, 0)
            binding.ivProfileEdit.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.ivProfileEdit.setImageURI(Uri.fromFile(fallbackFile))
            binding.ivProfileEdit.imageTintList = null
            // Restore the path in prefs for next time
            prefs.edit().putString("USER_PHOTO_PATH", fallbackFile.absolutePath).apply()
        } else {
            setDefaultProfileIcon()
        }
    }

    private fun setDefaultProfileIcon() {
        binding.ivProfileEdit.setPadding(dpToPx(28), dpToPx(28), dpToPx(28), dpToPx(28))
        binding.ivProfileEdit.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        binding.ivProfileEdit.setImageResource(R.drawable.ic_user)
        binding.ivProfileEdit.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#5E5CE6"))
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnChangePhoto.setOnClickListener {
            showPhotoOptionDialog()
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun showPhotoOptionDialog() {
        val options = arrayOf("Take Photo", "Upload Photo")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Update Profile Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePictureLauncher.launch(null)
                1 -> pickImageLauncher.launch("image/*")
            }
        }
        builder.show()
    }

    private fun saveChanges() {
        val newName = binding.etName.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveChanges.isEnabled = false
        val request = NameRequest(newName)

        RetrofitClient.instance.updateName("Bearer $token", request)
            .enqueue(object : retrofit2.Callback<CommonResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResponse>,
                    response: retrofit2.Response<CommonResponse>
                ) {
                    if (response.isSuccessful) {
                        prefs.edit().putString("USER_NAME", newName).apply()

                        // Check if photo changed
                        if (selectedImageBitmap != null || selectedImageUri != null) {
                            val userId = prefs.getInt("USER_ID", -1)
                            val photoPath = saveProfileImage(userId)
                            if (photoPath != null) {
                                uploadProfilePhoto(token, photoPath)
                            } else {
                                onUpdateFinished("Profile updated successfully")
                            }
                        } else {
                            onUpdateFinished("Profile updated successfully")
                        }
                    } else {
                        binding.btnSaveChanges.isEnabled = true
                        val errorMsg = response.errorBody()?.string() ?: "Update failed"
                        Toast.makeText(this@EditProfileActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<CommonResponse>, t: Throwable) {
                    binding.btnSaveChanges.isEnabled = true
                    Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun uploadProfilePhoto(token: String, filePath: String) {
        val file = File(filePath)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        RetrofitClient.instance.uploadPhoto("Bearer $token", body)
            .enqueue(object : retrofit2.Callback<CommonResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResponse>,
                    response: retrofit2.Response<CommonResponse>
                ) {
                    binding.btnSaveChanges.isEnabled = true
                    if (response.isSuccessful) {
                        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
                        prefs.edit().putString("USER_PHOTO_PATH", filePath).apply()
                        onUpdateFinished("Profile photo updated successfully")
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Photo upload failed"
                        Toast.makeText(this@EditProfileActivity, "Photo Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<CommonResponse>, t: Throwable) {
                    binding.btnSaveChanges.isEnabled = true
                    Toast.makeText(this@EditProfileActivity, "Photo Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun onUpdateFinished(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun saveProfileImage(userId: Int): String? {
        val fileName = if (userId != -1) "profile_image_$userId.jpg" else "profile_image.jpg"
        val file = java.io.File(filesDir, fileName)
        try {
            if (selectedImageBitmap != null) {
                val out = java.io.FileOutputStream(file)
                selectedImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                return file.absolutePath
            } else if (selectedImageUri != null) {
                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                return file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
