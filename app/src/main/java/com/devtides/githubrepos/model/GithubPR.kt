package com.devtides.githubrepos.model

import com.google.gson.annotations.SerializedName

data class GithubPR(
    val id:String?,
    val title:String?,
    val number:String?,

    @SerializedName("comments_url")
    val commentUrl:String?,
    val user: GithubUser?
){
    override fun toString() = "$title - $id"
}