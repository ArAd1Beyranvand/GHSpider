package com.arad.ghspider.viewmodel

import androidx.lifecycle.ViewModel
import com.arad.ghspider.model.GitHubUser
import com.arad.ghspider.model.GitHubRepo
import com.arad.ghspider.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

sealed class GitHubState {
    data object Loading : GitHubState()
    data class Success(val user: GitHubUser, val repos: List<GitHubRepo>) : GitHubState()
    data class Error(val message: String) : GitHubState()
}

class GitHubViewModel(private val repository: GitHubRepository) : ViewModel() {
    private val _state = MutableStateFlow<GitHubState>(GitHubState.Loading)
    val state: StateFlow<GitHubState> = _state

    fun fetchUserData(username: String, token: String? = null) {
        runBlocking {
            _state.value = GitHubState.Loading
            try {
                val userResult = repository.getUser(username, token)
                val reposResult = repository.getUserRepositories(username, token)

                if (userResult.isSuccess && reposResult.isSuccess) {
                    _state.value = GitHubState.Success(
                        userResult.getOrNull()!!,
                        reposResult.getOrNull()!!
                    )
                } else {
                    val errorMessage = userResult.exceptionOrNull()?.message 
                        ?: reposResult.exceptionOrNull()?.message 
                        ?: "Unknown error occurred"
                    _state.value = GitHubState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _state.value = GitHubState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
} 