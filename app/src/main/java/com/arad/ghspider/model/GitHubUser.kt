package com.arad.ghspider.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class GitHubUser(
    @SerializedName("login")
    val username: String,
    
    @SerializedName("followers")
    val followers: Int,
    
    @SerializedName("following")
    val following: Int,
    
    @SerializedName("created_at")
    val createdAt: Date,
    
    @SerializedName("public_repos")
    val publicRepos: Int,
    
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("bio")
    val bio: String?
) 