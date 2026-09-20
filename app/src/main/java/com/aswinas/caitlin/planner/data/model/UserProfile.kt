package com.aswinas.caitlin.planner.data.model

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val photoUri: String? = null,
    val isCreated: Boolean = false
)
