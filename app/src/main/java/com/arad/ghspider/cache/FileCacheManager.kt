package com.arad.ghspider.cache

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import com.arad.ghspider.model.GitHubUser
import com.arad.ghspider.model.GitHubRepo

object FileCacheManager {
    private val gson = Gson()
    private val usersFile = File("users.json")
    private val reposFile = File("repos.json")

    private fun ensureFileExists(file: File) {
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("{}")
        }
    }

    fun loadUsers(): MutableMap<String, GitHubUser> {
        ensureFileExists(usersFile) // Ensure file exists with an empty JSON object
        return if (usersFile.readText().isNotEmpty()) {
            gson.fromJson(usersFile.readText(), object : TypeToken<MutableMap<String, GitHubUser>>() {}.type)
        } else mutableMapOf()
    }

    fun saveUsers(users: Map<String, GitHubUser>) {
        usersFile.writeText(gson.toJson(users))
    }

    fun loadRepos(): MutableMap<String, List<GitHubRepo>> {
        ensureFileExists(reposFile)
        return if (reposFile.readText().isNotEmpty()) {
            gson.fromJson(reposFile.readText(), object : TypeToken<MutableMap<String, List<GitHubRepo>>>() {}.type)
        } else mutableMapOf()
    }

    fun saveRepos(repos: Map<String, List<GitHubRepo>>) {
        reposFile.writeText(gson.toJson(repos))
    }


    fun deleteCacheFiles() {
        if (usersFile.exists()) {
            usersFile.delete()
        }
        if (reposFile.exists()) {
            reposFile.delete()
        }
    }
}
