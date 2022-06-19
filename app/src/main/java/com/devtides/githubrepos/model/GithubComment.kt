package com.devtides.githubrepos.model

import com.google.gson.annotations.SerializedName

data class GithubComment(
    val id:String?,
    val body:String?,
){
    override fun toString() = "$body - $id"
}