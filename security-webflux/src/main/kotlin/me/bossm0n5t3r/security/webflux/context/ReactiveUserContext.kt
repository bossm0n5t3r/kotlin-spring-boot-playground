package me.bossm0n5t3r.security.webflux.context

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import reactor.util.context.Context
import reactor.util.context.ContextView

object ReactiveUserContext {
    // Keys for Reactor Context
    const val CTX_START_MILLIS: String = "CURRENT_START_MILLIS"
    const val CTX_USER_KEY: String = "CURRENT_USER_DETAIL_CTX"
    const val CTX_TOKEN_KEY: String = "CURRENT_AUTH_TOKEN_CTX"

    // Keys for ServerWebExchange attributes (for non-reactive access if really needed)
    const val ATTR_START_MILLIS: String = "CURRENT_START_MILLIS"
    const val ATTR_USER_KEY: String = "CURRENT_USER_DETAIL_ATTR"
    const val ATTR_TOKEN_KEY: String = "CURRENT_AUTH_TOKEN_ATTR"

    fun putAll(
        ctx: Context,
        startMillis: Long = System.currentTimeMillis(),
        user: UserDetail,
        token: String,
    ): Context = ctx.put(CTX_START_MILLIS, startMillis).put(CTX_USER_KEY, user).put(CTX_TOKEN_KEY, token)

    fun userFrom(ctxView: ContextView): UserDetail? = ctxView.getOrDefault<UserDetail>(CTX_USER_KEY, null)

    fun tokenFrom(ctxView: ContextView): String? = ctxView.getOrDefault<String>(CTX_TOKEN_KEY, null)

    suspend fun currentUserOrNull(): UserDetail? {
        val reactorContext = currentCoroutineContext()[ReactorContext.Key]
        val ctxView: ContextView? = reactorContext?.context
        return ctxView?.let { userFrom(it) }
    }

    suspend fun currentTokenOrNull(): String? {
        val reactorContext = currentCoroutineContext()[ReactorContext.Key]
        val ctxView: ContextView? = reactorContext?.context
        return ctxView?.let { tokenFrom(it) }
    }
}
