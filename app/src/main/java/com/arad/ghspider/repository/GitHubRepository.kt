package com.arad.ghspider.repository

import com.arad.ghspider.api.GitHubApiService
import com.arad.ghspider.cache.FileCacheManager
import com.arad.ghspider.model.GitHubUser
import com.arad.ghspider.model.GitHubRepo

class GitHubRepository(private val apiService: GitHubApiService) {
    val userCache = mutableMapOf<String, GitHubUser>()
    val repoCache = mutableMapOf<String, List<GitHubRepo>>()

    suspend fun getUser(username: String, token: String? = null): Result<GitHubUser> {
        val lowerCaseUsername = username.lowercase()

        val fileUsers = FileCacheManager.loadUsers().mapKeys { it.key.lowercase() }
        fileUsers[lowerCaseUsername]?.let {
            userCache[lowerCaseUsername] = it
            return Result.success(it)
        }

        userCache[lowerCaseUsername]?.let {
            return Result.success(it)
        }

        return try {
            val response = apiService.getUser(lowerCaseUsername, token?.let { "Bearer $it" })
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    userCache[lowerCaseUsername] = user
                    FileCacheManager.saveUsers(userCache.mapKeys { it.key.lowercase() })
                    Result.success(user)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRepositories(username: String, token: String? = null): Result<List<GitHubRepo>> {
        val lowerCaseUsername = username.lowercase()

        val fileRepos = FileCacheManager.loadRepos().mapKeys { it.key.lowercase() }
        fileRepos[lowerCaseUsername]?.let {
            repoCache[lowerCaseUsername] = it
            return Result.success(it)
        }

        repoCache[lowerCaseUsername]?.let {
            return Result.success(it)
        }

        return try {
            val response = apiService.getUserRepositories(lowerCaseUsername, token?.let { "Bearer $it" })
            if (response.isSuccessful) {
                response.body()?.let { repos ->
                    repoCache[lowerCaseUsername] = repos
                    // Save the cache ensuring keys are lowercase
                    FileCacheManager.saveRepos(repoCache.mapKeys { it.key.lowercase() })
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
