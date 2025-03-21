package com.arad.ghspider

import kotlinx.coroutines.flow.first
import com.arad.ghspider.api.GitHubApiService
import com.arad.ghspider.cache.FileCacheManager
import com.arad.ghspider.repository.GitHubRepository
import com.arad.ghspider.viewmodel.GitHubViewModel
import com.arad.ghspider.viewmodel.GitHubState
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import kotlin.system.exitProcess
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val app = GHSpiderApp()
    app.start()
}

class GHSpiderApp {
    private val baseUrl = "https://api.github.com/"
    private lateinit var viewModel: GitHubViewModel

    init {
        setupDependencies()
    }

    private fun setupDependencies() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GitHubApiService::class.java)
        val repository = GitHubRepository(apiService)

        // Initially load persisted cache data (used when fetching new data)
        repository.userCache.putAll(FileCacheManager.loadUsers())
        repository.repoCache.putAll(FileCacheManager.loadRepos())

        viewModel = GitHubViewModel(repository)
    }

    fun start() {
        println("Welcome to GHSpider - GitHub User Information Fetcher")
        println("------------------------------------------------")

        while (true) {
            println("\nMenu:")
            println("1️⃣ Fetch and display GitHub user data by username")
            println("2️⃣ Search cached users by username (read from file)")
            println("3️⃣ Search cached repositories by repository name (read from file)")
            println("4️⃣ Show all cached data (read from file)")
            println("5️⃣ Exit application")
            print("\nSelect an option: ")

            when (readlnOrNull()?.trim()) {
                "1" -> fetchUserData()
                "2" -> searchCachedUsers()
                "3" -> searchCachedRepositories()
                "4" -> showAllCachedData()
                "5" -> exitApplication()

                else -> println("Invalid option. Please try again.")
            }
        }
    }

    private fun exitApplication() {
        println("\nDo you want to delete the persistent cache files before exiting? (Y/n):")
        val answer = readLine()?.trim()?.lowercase() ?: "y"

        if (answer == "y" || answer == "yes" || answer.isEmpty()) {
            FileCacheManager.deleteCacheFiles()
            println("Cache files deleted.")
        } else {
            println("Cache files retained.")
        }

        println("Thank you for using GHSpider!")
        exitProcess(0)
    }

    private fun fetchUserData() {
        println("\nEnter a GitHub username:")
        val username = readlnOrNull()?.trim() ?: return
        if (username.isNotEmpty()) {
            fetchAndDisplayUserInfo(username)
        } else {
            println("Username cannot be empty.")
        }
    }

    private fun searchCachedUsers() {
        println("\nEnter a username to search in cache (from file):")
        val username = readlnOrNull()?.trim() ?: return
        if (username.isNotEmpty()) {
            val fileUsers = FileCacheManager.loadUsers()
            val user = fileUsers[username.lowercase()]
            if (user != null) {
                println("User found in file cache: $user")
            } else {
                println("User not found in file cache.")
            }
        } else {
            println("Username cannot be empty.")
        }
    }

    private fun searchCachedRepositories() {
        println("\nEnter a repository name to search in cache (from file):")
        val repoName = readlnOrNull()?.trim() ?: return
        if (repoName.isNotEmpty()) {
            val fileRepos = FileCacheManager.loadRepos()
            val repos = fileRepos.values.flatten()
                .filter { it.name.contains(repoName, ignoreCase = true) }
            if (repos.isNotEmpty()) {
                println("Repositories found in file cache:")
                repos.forEach { println(it) }
            } else {
                println("No repositories found in file cache with that name.")
            }
        } else {
            println("Repository name cannot be empty.")
        }
    }

    private fun showAllCachedData() {
        println("\nCached Users (from file):")
        val fileUsers = FileCacheManager.loadUsers()
        fileUsers.values.forEach { println(it) }

        println("\nCached Repositories (from file):")
        val fileRepos = FileCacheManager.loadRepos()
        fileRepos.values.flatten().forEach { println(it) }
    }

    private fun fetchAndDisplayUserInfo(username: String): Unit = runBlocking {
        println("\nFetching information for user: $username")
        viewModel.fetchUserData(username)
        val state = viewModel.state.first { it !is GitHubState.Loading }
        when (state) {
            is GitHubState.Success -> displayUserInfo(state)
            is GitHubState.Error -> println("Error: ${state.message}")
            else -> {}
        }
    }

    private fun displayUserInfo(state: GitHubState.Success) {
        val user = state.user
        val repos = state.repos
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.UK)

        println("\nUser Information:")
        println("----------------")
        println("Username: ${user.username}")
        println("Name: ${user.name ?: "Not specified"}")
        println("Bio: ${user.bio ?: "Not specified"}")
        println("Followers: ${user.followers}")
        println("Following: ${user.following}")
        println("Account created: ${dateFormat.format(user.createdAt)}")
        println("Public repositories: ${user.publicRepos}")

        println("\nTop Repositories:")
        println("----------------")
        repos.sortedByDescending { it.stars }
            .take(5)
            .forEach { repo ->
                println("\nName: ${repo.name}")
                println("Description: ${repo.description ?: "No description"}")
                println("Language: ${repo.language ?: "Not specified"}")
                println("Stars: ${repo.stars}")
                println("Forks: ${repo.forks}")
                println("URL: ${repo.url}")
            }
    }
}
