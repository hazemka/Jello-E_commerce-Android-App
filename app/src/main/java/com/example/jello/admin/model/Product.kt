package com.example.jello.admin.model

import com.google.firebase.firestore.DocumentId

class Product(@DocumentId var id:String,var name:String,var description:String,var image:String
                    , var price:Double,var rate:Double,var latitude:Double,var longitude: Double, var numberOfBuyers:Int){
    var categoryId:String = ""
    constructor(id:String, name:String, description:String, image:String, price:Double,
                rate:Double, latitude:Double, longitude: Double, numberOfBuyers:Int
                , categoryId:String) : this(id,name,description,image,price,rate,latitude,longitude,numberOfBuyers
                ) {
        this.categoryId = categoryId
    }
}
