package me.bossm0n5t3r.security.mvc.context

import me.bossm0n5t3r.security.mvc.dto.UserDetail

object UserContext {
    private val USER_DETAIL = ThreadLocal<UserDetail>()
    private val AUTH_TOKEN = ThreadLocal<String>()
    private val START_MILLIS = ThreadLocal<Long>()

    fun setUserDetail(userDetail: UserDetail) {
        USER_DETAIL.set(userDetail)
    }

    fun getUserDetail(): UserDetail? = USER_DETAIL.get()

    fun setAuthToken(token: String) {
        AUTH_TOKEN.set(token)
    }

    fun getAuthToken(): String? = AUTH_TOKEN.get()

    fun setStartMillis(startMillis: Long) {
        START_MILLIS.set(startMillis)
    }

    fun getStartMillis(): Long? = START_MILLIS.get()

    fun clear() {
        USER_DETAIL.remove()
        AUTH_TOKEN.remove()
        START_MILLIS.remove()
    }
}
