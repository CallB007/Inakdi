package com.example.inakdi.models

class ModelBerita {

    //variables for firebase database
    var id: String = ""
    var uid: String = ""
    var title: String = ""
    var description: String = ""
    var timestamp: Long = 0

    //empty constructor for firebase db
    constructor()

    //constructor with all params
    constructor(
        timestamp: Long,
        description: String,
        title: String,
        uid: String,
        id: String
    ) {
        this.timestamp = timestamp
        this.description = description
        this.title = title
        this.uid = uid
        this.id = id
    }
}