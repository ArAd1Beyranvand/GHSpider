package com.arad.ghspider.repository

import com.arad.ghspider.api.GitHubApiService
import com.arad.ghspider.model.GitHubUser
import com.arad.ghspider.model.GitHubRepo
import retrofit2.Response

class GitHubRepository(private val apiService: GitHubApiService) {
    val userCache = mutableMapOf<String, GitHubUser>()
    val repoCache = mutableMapOf<String, List<GitHubRepo>>()

    suspend fun getUser(username: String, token: String? = null): Result<GitHubUser> {
        // Check cache first
        userCache[username]?.let {
            return Result.success(it)
        }

        return try {
            val response = apiService.getUser(username, token?.let { "Bearer $it" })
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    userCache[username] = user
                    Result.success(user)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRepositories(
        username: String,
        token: String? = null
    ): Result<List<GitHubRepo>> {
        // Check cache first
        repoCache[username]?.let {
            return Result.success(it)
        }

        return try {
            val response = apiService.getUserRepositories(username, token?.let { "Bearer $it" })
            if (response.isSuccessful) {
                response.body()?.let { repos ->
                    repoCache[username] = repos
                    Result.success(repos)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 