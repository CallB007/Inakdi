package com.example.inakdi.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private companion object {
        private const val TAG = "PROFILE_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var myUserType = ""

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show Profile saat udate profile
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu Sebentar ...")
        progressDialog.setCanceledOnTouchOutside(false)

        // memanggil fungsi firebase
        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()

        // Tombol Balik
        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""

    private fun validateData() {
        // input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        phoneCode = binding.countryCodePicker.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()

        // validate data
        if (imageUri == null) {
            // no Image to upload storage just update db
            updateProfileDb(null)
        } else {
            // image need to upload to storage
            updateProfileImageStorage()
        }
    }

    private fun updateProfileImageStorage() {
        Log.d(TAG, "updateProfileImageStorage: ")
        // show progress
        progressDialog.setMessage("Mengunggah Foto Profil ...")
        progressDialog.show()

        // setup image name and path
        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"
        // storage reference
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot ->
                // check image upload progress
                val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "updateProfileImageStorage: progress: $progress")
                progressDialog.setMessage("Mengunggah ${progress.toInt()}%")
            }
            .addOnSuccessListener { taskSnapshot ->
                // image succes upload
                Log.d(TAG, "updateProfileImageStorage: Image Uploaded ...")

                val uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);

                val uploadedImageUrl = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    updateProfileDb(uploadedImageUrl)
                }
            }
            .addOnFailureListener { e ->
                // failed to upload image
                Log.e(TAG, "updateProfileImageStorage: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to upload due to ${e.message}")

            }
    }

    private fun updateProfileDb(uploadedImageUrl: String?) {
        Log.d(TAG, "updateProfileDb: uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Mengubah Data Akun")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = name

        if (uploadedImageUrl != null) {
            hashMap["profileImageUrl"] = uploadedImageUrl
        }

        if (myUserType.equals("Phone", true)) {
            hashMap["email"] = email
        } else if (myUserType.equals("Email", true)) {
            hashMap["phoneCode"] = phoneCode
            hashMap["phoneNumber"] = phoneNumber
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "updateProfileDb: Updated ...")
                progressDialog.dismiss()
                Utils.toast(this, "Data akun berhasil disimpan")

                imageUri = null

            }
            .addOnFailureListener { e ->

                Log.e(TAG, "updateProfileDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Gagal mengubah data akun. Error: ${e.message}")
            }
    }

    private fun loadMyInfo() {
        Log.d(TAG, "loadMyInfo: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.child("email").value.toString()
                    val name = snapshot.child("name").value.toString()
                    val phoneCode = snapshot.child("phoneCode").value.toString()
                    val phoneNumber = snapshot.child("phoneNumber").value.toString()
                    val profileImageUrl = snapshot.child("profileImageUrl").value.toString()
                    val timestamp = snapshot.child("timestamp").value.toString()
                    myUserType = snapshot.child("userType").value.toString()
                    // combination to make full phone number
                    val phone = phoneCode + phoneNumber

                    // Set Data to UI
                    binding.emailEt.setText(email)
                    binding.nameEt.setText(name)
                    binding.phoneNumberEt.setText(phoneNumber)

                    try {
                        val phoneCodeInt = phoneCode.replace("+", "").toInt()
                        binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile)
                            .into(binding.profileTv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialog() {

        val popupMenu = PopupMenu(this, binding.profileImagePickFab)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Kamera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galeri")

        popupMenu.show()
        // handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item ->
            // get id of the menu item clicked
            val itemId = item.itemId

            if (itemId == 1) {
                // camera is clicked
                Log.d(TAG, "imagePickDialog: Camera Clicked, check if camera permission(s) granted or not")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // device version is TIRAMISU or UPPER need to allow permission to launch Gallery
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    // device version is TIRAMISU need to allow permission to launch Gallery
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            } else if (itemId == 2) {
                // gallery is clicked
                Log.d(TAG, "imagePickDialog: Gallery Clicked, check if storage permission(s) granted or not")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // device version is TIRAMISU don't need to allow permission to launch Gallery
                    pickImageGallery()
                } else {
                    // device version is TIRAMISU need to allow permission to launch Gallery
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            true
        }
    }

    // NEED CAMERA PERMISSION
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            Log.d(TAG, "requestCameraPermission: result: $result")
            // check permission
            var areAllGranted = true
            for (isGranted in result.values) {
                areAllGranted = areAllGranted && isGranted
            }

            if (areAllGranted) {
                Log.d(TAG, "requestCameraPermission: all permissions granted e.g Camera, Storage")
                pickImageCamera()
            } else {
                Log.d(TAG, "requestCameraPermission: All or either one is denied ...")
                Utils.toast(this, "Ijinkan akses Kamera dan Penyimpanan ? ")
            }

        }

    // NEED STORAGE PERMISSION
    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

            if (isGranted) {
                pickImageGallery()
            } else {

                Utils.toast(this, "Storage permission is denied ...")
            }
        }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }


    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // check img is captured or not
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "cameraActivityResultLauncher: Image Captured: imageUri: $imageUri")
                // set to profileTv
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.profile)
                        .into(binding.profileTv)
                } catch (e: Exception) {
                    Log.e(TAG, "cameraActivityResultLauncher: ", e)
                }
            } else {
                // cancel
                Utils.toast(this, "Cancelled !")
            }
        }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // check image
            if (result.resultCode == Activity.RESULT_OK) {
                // get data
                val data = result.data
                // get uri of image picked
                imageUri = data!!.data

                // set to profileTv
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.profile)
                        .into(binding.profileTv)
                } catch (e: Exception) {
                    Log.e(TAG, "galleryActivityResultLauncher: ", e)
                }
            }
        }
}
