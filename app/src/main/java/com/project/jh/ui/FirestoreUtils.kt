package com.project.jh.ui

object FirestoreUtils {
    fun getChatRoomId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }
}
