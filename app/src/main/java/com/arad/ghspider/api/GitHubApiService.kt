package com.arad.ghspider.api

import com.arad.ghspider.model.GitHubUser
import com.arad.ghspider.model.GitHubRepo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Header

interface GitHubApiService {
    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") token: String? = null
    ): Response<GitHubUser>

    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Header("Authorization") token: String? = null
    ): Response<List<GitHubRepo>>
} 