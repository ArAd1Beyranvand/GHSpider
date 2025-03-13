package com.arad.ghspider
import kotlinx.coroutines.flow.first
import com.arad.ghspider.api.GitHubApiService
import com.arad.ghspider.repository.GitHubRepository
import com.arad.ghspider.viewmodel.GitHubViewModel
import com.arad.ghspider.viewmodel.GitHubState
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
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
        viewModel = GitHubViewModel(repository)
    }

    fun start() {
        println("Welcome to GHSpider - GitHub User Information Fetcher")
        println("------------------------------------------------")

        while (true) {
            println("\nMenu:")
            println("1️⃣ Fetch and display GitHub user data by username")
            println("2️⃣ Search cached users by username")
            println("3️⃣ Search cached repositories by repository name")
            println("4️⃣ Show all cached data")
            println("5️⃣ Exit application")
            print("\nSelect an option: ")

            when (readLine()?.trim()) {
                "1" ->
                    fetchUserData()
                "2" -> searchCachedUsers()
                "3" -> searchCachedRepositories()
                "4" -> showAllCachedData()
                "5" -> {
                    println("Exiting application. Thank you for using GHSpider!")
                    return
                }

                else -> println("Invalid option. Please try again.")
            }
        }
    }

    private fun fetchUserData() {
        println("\nEnter a GitHub username:")
        val username = readLine()?.trim() ?: return
        if (username.isNotEmpty()) {
            fetchAndDisplayUserInfo(username)
        } else {
            println("Username cannot be empty.")
        }
    }

    private fun searchCachedUsers() {
        println("\nEnter a username to search in cache:")
        val username = readLine()?.trim() ?: return
        if (username.isNotEmpty()) {
            val user = viewModel.repository.userCache[username]
            if (user != null) {
                println("User found in cache: $user")
            } else {
                println("User not found in cache.")
            }
        } else {
            println("Username cannot be empty.")
        }
    }

    private fun searchCachedRepositories() {
        println("\nEnter a repository name to search in cache:")
        val repoName = readLine()?.trim() ?: return
        if (repoName.isNotEmpty()) {
            val repos = viewModel.repository.repoCache.values.flatten()
                .filter { it.name.contains(repoName, ignoreCase = true) }
            if (repos.isNotEmpty()) {
                println("Repositories found in cache:")
                repos.forEach { println(it) }
            } else {
                println("No repositories found in cache with that name.")
            }
        } else {
            println("Repository name cannot be empty.")
        }
    }

    private fun showAllCachedData() {
        println("\nCached Users:")
        viewModel.repository.userCache.values.forEach { println(it) }

        println("\nCached Repositories:")
        viewModel.repository.repoCache.values.flatten().forEach { println(it) }
    }

    private fun fetchAndDisplayUserInfo(username: String): Unit = runBlocking {
        println("\nFetching information for user: $username")


        viewModel.fetchUserData(username)
        val state = viewModel.state.first { it !is GitHubState.Loading }
        when (state) {
            is GitHubState.Success -> {
                displayUserInfo(state)
            }

            is GitHubState.Error -> {
                println("Error: ${state.message}")
            }

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