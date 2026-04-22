package com.example.xafaeltalp.model

class UserRepository(private val userDao: UserDao) {

    suspend fun addUser(user: User): Boolean {
        val existing = userDao.getUser(user.username)
        return if (existing == null) {
            userDao.insert(user)
            true
        } else {
            false
        }
    }

    suspend fun getUser(username: String): User? {
        return userDao.getUser(username)
    }
}
