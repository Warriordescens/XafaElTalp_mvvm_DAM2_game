package com.example.xafaeltalp.model

object UserRepository {
    private val users = mutableMapOf<String, User>()

    init {
        addUser(User("a", "a"))
        addUser(User("b", "c"))
        addUser(User("b", "c"))
    }

    fun addUser(user: User): Boolean {
        if (users.containsKey(user.username)) {
            return false
        } else {
            users[user.username] = user
            return true
        }
    }

    fun getUser(username: String): User? {
        return users[username]
    }
}