package com.example.data.model

data class UserProfile(
    val name: String = "Aswin",
    val age: Int = 22,
    val photoUri: String? = null,
    val isCreated: Boolean = true
)
