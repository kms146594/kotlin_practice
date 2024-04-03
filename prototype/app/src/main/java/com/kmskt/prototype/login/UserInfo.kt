package com.kmskt.prototype.login

/*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
 */

// @Entity
data class UserInfo(
    // @Id @GeneratedValue(strategy = GenerationType.IDENTIFY)
    val id: Long = 0,
    val username: String,
    val password: String
)
