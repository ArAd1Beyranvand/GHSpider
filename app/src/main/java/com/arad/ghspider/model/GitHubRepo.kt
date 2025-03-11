package com.arad.ghspider.model

import com.google.gson.annotations.SerializedName

data class GitHubRepo(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("stargazers_count")
    val stars: Int,
    
    @SerializedName("forks_count")
    val forks: Int,
    
    @SerializedName("language")
    val language: String?,
    
    @SerializedName("html_url")
    val url: String
) 