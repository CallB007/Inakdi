package com.example.inakdi.activities

import android.Manifest
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
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.adapters.AdapterImagePicked
import com.example.inakdi.databinding.ActivityTambahFasilitasBinding
import com.example.inakdi.models.ModelImagePicked
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class TambahFasilitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahFasilitasBinding

    private companion object {
        private const val TAG = "ADD_FASILITAS_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked: AdapterImagePicked

    private var isEditMode = false
    private var fasilitasId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTambahFasilitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the activity is in edit mode
        val intent = intent
        if (intent.hasExtra("fasilitasId")) {
            isEditMode = true
            fasilitasId = intent.getStringExtra("fasilitasId")!!
        }

        // Initialize UI elements
        initUI()

        if (isEditMode) {
            loadFasilitasData()
        }
    }

    private fun initUI() {
        // setup progressDialog while Adding or Updating content
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu Sebentar")
        progressDialog.setCanceledOnTouchOutside(false)

        // auth related task
        firebaseAuth = FirebaseAuth.getInstance()

        // setup and set the categories adapter
        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        // init imagePickedArrayList
        imagePickedArrayList = ArrayList()
        loadImages()

        // Back Button
        binding.toolBarBackBtn.setOnClickListener {
            onBackPressed()
        }

        // Add Image Button
        binding.toolBarAddImageBtn.setOnClickListener {
            showImagePickOptions()
        }

        binding.postBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")

        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList)

        binding.imagesRv.adapter = adapterImagePicked
    }

    private fun loadFasilitasData() {
        val ref = FirebaseDatabase.getInstance().getReference("Fasilitas Umum")
        ref.child(fasilitasId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val title = snapshot.child("title").value.toString()
                    val category = snapshot.child("category").value.toString()
                    val description = snapshot.child("description").value.toString()

                    binding.titleEt.setText(title)
                    binding.categoryAct.setText(category, false)
                    binding.descEt.setText(description)

                    // Load images
                    loadImagesFromFirebase()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load fasilitas data: ", e)
                Utils.toast(this, "Failed to load data")
            }
    }

    private fun loadImagesFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("Fasilitas Umum")
        ref.child(fasilitasId).child("Images")
            .get()
            .addOnSuccessListener { snapshot ->
                imagePickedArrayList.clear()
                for (ds in snapshot.children) {
                    val id = ds.child("id").value.toString()
                    val imageUrl = ds.child("imageUrl").value.toString()
                    val imageUri = Uri.parse(imageUrl)

                    val modelImagePicked = ModelImagePicked(id, imageUri, imageUrl, true)
                    imagePickedArrayList.add(modelImagePicked)
                }
                loadImages()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load images: ", e)
                Utils.toast(this, "Failed to load images")
            }
    }


    private fun showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ")
        //init popup menu
        val popupMenu = PopupMenu(this, binding.toolBarAddImageBtn)
        //add menu items to our popup menuId param
        popupMenu.menu.add(Menu.NONE, 1, 1, "Kamera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galeri")
        //show pop up menu
        popupMenu.show()
        //handle pop up menu click
        popupMenu.setOnMenuItemClickListener { item ->
            //get the id
            val itemId = item.itemId
            //Logika check id 1 or 2
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                } else {
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery()
                } else {
                    val storagePermissions = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermissions)
                }
            }
            true
        }
    }

    //Request Storage Permission
    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted) {
            // Storage Permission Granted
            pickImageGallery()
        } else {
            Utils.toast(this, "Storage Permission Denied ...")
        }
    }

    //Request Camera Permission
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermission: result: $result")

        var areAllGranted = true
        for (isGranted in result.values) {
            areAllGranted = areAllGranted && isGranted
        }

        if (areAllGranted) {
            //All Permissions Camera
            pickImageCamera()
        } else {
            //Camera or Storage permission denied
            Utils.toast(this, "Camera or Storage or both permissions denied")
        }
    }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActionResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "galleryActivityResultLauncher: ")

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${Utils.getTimestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        } else {
            Utils.toast(this, "Cancelled...")
        }
    }

    private val cameraActionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "cameraActionResultLauncher: ")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "cameraActionResultLauncher: ")

            val timestamp = "${Utils.getTimestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        } else {
            Utils.toast(this, "Cancelled...")
        }
    }

    private var title = ""
    private var category = ""
    private var description = ""

    private fun validateData() {
        Log.d(TAG, "validateData: ")

        title = binding.titleEt.text.toString().trim()
        category = binding.categoryAct.text.toString().trim()
        description = binding.descEt.text.toString().trim()

        //Logika error jika tidak diisi
        if (title.isEmpty()) {
            binding.titleEt.error = "Judul tidak boleh kosong !"
            binding.titleEt.requestFocus()
        } else if (category.isEmpty()) {
            binding.categoryAct.error = "Kategori tidak boleh kosong !"
            binding.categoryAct.requestFocus()
        } else if (description.isEmpty()) {
            binding.descEt.error = "Deskripsi tidak boleh kosong !"
            binding.descEt.requestFocus()
        } else {
            postFasilitas()
        }
    }

    private fun postFasilitas() {
        Log.d(TAG, "postFasilitas: ")

        progressDialog.setMessage("Mengunggah Fasilitas Umum...")
        progressDialog.show()

        // Mencatat waktu diupload (timestamp)
        val timestamp = Utils.getTimestamp()
        // Database reference
        val refFasilitas = FirebaseDatabase.getInstance().getReference("Fasilitas Umum")
        // Get id for database
        val keyId = if (isEditMode) fasilitasId else refFasilitas.push().key

        // Setup data to add in database firebase
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["title"] = "$title"
        hashMap["category"] = "$category"
        hashMap["description"] = "$description"
        hashMap["timestamp"] = timestamp

        // Set data to firebase database
        refFasilitas.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postFasilitas: ")
                uploadImagesStorage(keyId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "postFasilitas: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Gagal mengunggah fasilitas umum. Error ${e.message}")
            }
    }


    private fun uploadImagesStorage(adId: String) {
        for (i in imagePickedArrayList.indices) {
            val modelImagePicked = imagePickedArrayList[i]

            val imageName = modelImagePicked.id

            val filePathAndName = "fasilitas_umum/$imageName"
            val imageIndexForProgress = i + 1

            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(modelImagePicked.imageUri!!)
                .addOnProgressListener { snapshot ->
                    val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                    Log.d(TAG, "uploadImagesStorage: Proses : $progress")
                    val message = "Mengunggah $imageIndexForProgress dari ${imagePickedArrayList.size}, Proses ${progress.toInt()}"

                    progressDialog.setMessage(message)
                    progressDialog.show()
                }
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "uploadImagesStorage: onSuccess")

                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedImageUrl = uriTask.result

                    if (uriTask.isSuccessful) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = "${modelImagePicked.imageUri}"
                        hashMap["imageUrl"] = "$uploadedImageUrl"

                        val ref = FirebaseDatabase.getInstance().getReference("Fasilitas Umum")
                        ref.child(adId).child("Images")
                            .child(imageName)
                            .updateChildren(hashMap)
                    }

                    if (i == imagePickedArrayList.size - 1) {
                        progressDialog.dismiss()

                        // Navigate to MainActivity and show toast
                        val intent = Intent(this@TambahFasilitasActivity, MainActivity::class.java)
                        startActivity(intent)
                        Utils.toast(this@TambahFasilitasActivity, "Berhasil mengunggah Fasilitas Umum")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "uploadImagesStorage: ", e)
                    progressDialog.dismiss()
                }
        }
    }

}
