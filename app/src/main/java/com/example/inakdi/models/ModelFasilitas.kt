package com.example.inakdi.models

    //model class
    class ModelFasilitas {

    //variables for firebase database
    var category: String = ""
    var description: String = ""
    var id: String = ""
    var timestamp: Long = 0
    var title: String = ""
    var uid: String = ""

    //empty constructor for firebase db
    constructor()

    //constructor with all params
    constructor(
        category: String,
        description: String,
        id: String,
        timestamp: Long,
        title: String,
        uid: String,
    ) {
        this.category = category
        this.description = description
        this.id = id
        this.timestamp = timestamp
        this.title = title
        this.uid = uid
    }


}