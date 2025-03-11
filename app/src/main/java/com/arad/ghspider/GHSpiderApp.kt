package com.arad.ghspider

import com.arad.ghspider.api.GitHubApiService
import com.arad.ghspider.repository.GitHubRepository
import com.arad.ghspider.viewmodel.GitHubViewModel
import com.arad.ghspider.viewmodel.GitHubState
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.collect
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

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
            level = HttpLoggingInterceptor.Level.BASIC
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
            println("\nEnter a GitHub username (or 'exit' to quit):")
            val input = readLine()?.trim() ?: continue
            if (input.equals("exit", ignoreCase = true)) {
                break
            }

            if (input.isNotEmpty()) {
                fetchAndDisplayUserInfo(input)
            }
        }

        println("Thank you for using GHSpider!")
    }

    private fun fetchAndDisplayUserInfo(username: String):Unit = runBlocking {
        println("\nFetching information for user: $username")
        
        viewModel.fetchUserData(username)
        println("debug between fetch and collect")
        viewModel.state.collect { state ->
            when (state) {
                is GitHubState.Loading -> {
                    println("Loading...")
                }
                is GitHubState.Success -> {
                    displayUserInfo(state)
                    return@collect
                }
                is GitHubState.Error -> {
                    println("Error: ${state.message}")
                    return@collect
                }
            }
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