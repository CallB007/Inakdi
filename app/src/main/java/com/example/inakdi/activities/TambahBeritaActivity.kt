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
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.inakdi.Utils
import com.example.inakdi.adapters.AdapterImagePicked
import com.example.inakdi.databinding.ActivityTambahBeritaBinding
import com.example.inakdi.models.ModelImagePicked
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class TambahBeritaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahBeritaBinding

    private companion object{

        private const val TAG = "ADD_BERITA_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked: AdapterImagePicked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTambahBeritaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //setup progressDialog while Adding or Updating content
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu Sebentar")
        progressDialog.setCanceledOnTouchOutside(false)

        //auth related task
        firebaseAuth = FirebaseAuth.getInstance()

//        //setup and set the categories adapter
//        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
//        binding.categoryAct.setAdapter(adapterCategories)

        //init imagePickedArrayList
        imagePickedArrayList = ArrayList()
        loadImages()

        //Back Button
        binding.toolBarBackBtn.setOnClickListener{

            onBackPressed()
        }


        //Add Image Button
        binding.toolBarAddImageBtn.setOnClickListener{
            showImagePickOptions()
        }

        binding.postBtn.setOnClickListener{
            validateData()
        }

    }

    private fun loadImages(){
        Log.d(TAG, "loadImages: ")

        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList)

        binding.imagesRv.adapter = adapterImagePicked
    }

    private fun showImagePickOptions(){
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
            if(itemId == 1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                } else {
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

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
    ){ isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted){
            // Storage Permission Granted
            pickImageGallery()
        } else {

            Utils.toast(this, "Storage Permission Denied ...")
        }
    }

    //Request Camera Permission
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
            result ->
        Log.d(TAG, "requestCameraPermission: result: $result")

        var areAllGranted = true
        for (isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if (areAllGranted){
            //All Permissions Camera
            pickImageCamera()
        } else {
            //Camera or Storage permission denied
            Utils.toast(this, "Camera or Storage or both permissions denied")
        }
    }

    private fun pickImageGallery (){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera (){
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
    ){
            result ->
        Log.d(TAG, "galleryActivityResultLauncher: ")

        if (result.resultCode == Activity.RESULT_OK){

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
    ){
            result ->
        Log.d(TAG, "cameraActionResultLauncher: ")

        if (result.resultCode == Activity.RESULT_OK){

            Log.d(TAG, "cameraActionResultLauncher: ")

            val timestamp = "${Utils.getTimestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        } else{

            Utils.toast(this, "Cancelled...")
        }
    }

    private var title = ""
    //    private var category = ""
    private var description = ""

    private fun validateData(){
        Log.d(TAG, "validateData: ")

        title = binding.titleEt.text.toString().trim()
//        category = binding.categoryAct.text.toString().trim()
        description = binding.descEt.text.toString().trim()

        //Logika error jika tidak diisi
        if (title.isEmpty()){

            binding.titleEt.error = "Judul tidak boleh kosong !"
            binding.titleEt.requestFocus()
        } else if (description.isEmpty()){

            binding.descEt.error = "Deskripsi tidak boleh kosong !"
            binding.descEt.requestFocus()
        } else {

            postBerita()
        }
    }

    private fun postBerita(){
        Log.d(TAG, "postBerita: ")

        progressDialog.setMessage("Mengunggah Berita ...")
        progressDialog.show()

        //mencatat waktu diupload (timestamp)
        val timestamp = Utils.getTimestamp()
        //database reference
        val refBerita = FirebaseDatabase.getInstance().getReference("Berita")
        //get id for database
        val keyId = refBerita.push().key

        //setup data to add in database firebase
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["title"] = "$title"
//        hashMap["category"] = "$category"
        hashMap["description"] = "$description"
        hashMap["timestamp"] = timestamp

        //set data to firebase database
        refBerita.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postBerita: ")
                uploadImagesStorage(keyId)
            }
            .addOnFailureListener{e ->

                Log.e(TAG, "postBerita: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Gagal mengunggah Berita. Error ${e.message}")
            }
    }

    private fun uploadImagesStorage(adId: String){

        for(i in imagePickedArrayList.indices){

            val modelImagePicked = imagePickedArrayList[i]

            val imageName = modelImagePicked.id

            val filePathAndName = "berita/$imageName"
            val imageIndexForProgress = i + 1

            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(modelImagePicked.imageUri!!)
                .addOnProgressListener {snapshot ->

                    val progress = 100.0 *snapshot.bytesTransferred / snapshot.totalByteCount
                    Log.d(TAG, "uploadImagesStorage: Proses : $progress")
                    val message = "Mengunggah $imageIndexForProgress dari ${imagePickedArrayList.size}, Proses ${progress.toInt()}"

                    progressDialog.setMessage(message)
                    progressDialog.show()
                }
                .addOnSuccessListener {taskSnapshot ->
                    Log.d(TAG, "uploadImagesStorage: onSucces")

                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedImageUrl = uriTask.result

                    if (uriTask.isSuccessful){
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = "${modelImagePicked.imageUri}"
                        hashMap["imageUrl"] = "$uploadedImageUrl"

                        val ref = FirebaseDatabase.getInstance().getReference("Berita")
                        ref.child(adId).child("Images")
                            .child(imageName)
                            .updateChildren(hashMap)
                    }

                    progressDialog.dismiss()

                    // Navigate to MainActivity and show toast
                    val intent = Intent(this@TambahBeritaActivity, MainActivity::class.java)
                    startActivity(intent)
                    Utils.toast(this@TambahBeritaActivity, "Berhasil mengunggah Berita")
                }
                .addOnFailureListener{e ->
                    Log.e(TAG, "uploadImagesStorage: ", e)
                    progressDialog.dismiss()

                }
        }
    }
}