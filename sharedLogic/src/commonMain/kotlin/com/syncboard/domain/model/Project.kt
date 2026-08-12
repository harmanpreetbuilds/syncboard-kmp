package com.syncboard.domain.model

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val openTaskCount: Int
)
