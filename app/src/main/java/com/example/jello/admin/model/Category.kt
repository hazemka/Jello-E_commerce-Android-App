package com.example.jello.admin.model

import com.google.firebase.firestore.DocumentId

data class Category(@DocumentId var id:String, var name:String)