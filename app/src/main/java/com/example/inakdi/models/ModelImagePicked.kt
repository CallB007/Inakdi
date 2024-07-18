package com.example.inakdi.models

import android.net.Uri

class ModelImagePicked {

    /*--Variabel--*/
    var id = ""
    var imageUri: Uri? = null
    var imageUrl: String? = null
    var fromInternet = false

    /*--Empty Variabel for firebase db--*/
    constructor()

    /*--Constructor with all parameters--*/
    constructor(id: String, imageUri: Uri?, imageUrl: String?, fromInternet: Boolean) {
        this.id = id
        this.imageUri = imageUri
        this.imageUrl = imageUrl
        this.fromInternet = fromInternet

    }
}