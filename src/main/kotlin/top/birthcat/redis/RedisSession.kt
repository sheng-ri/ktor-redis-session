package top.birthcat.redis

import io.ktor.server.sessions.*
import kotlin.reflect.typeOf


inline fun <reified S : Indexable> SessionsConfig.redisCookie(
    name: String,
    redis: RedisCommand,
    block: CookieSessionBuilder<S>.() -> Unit
): SessionTrackByIndex<S> {
    val sessionType = S::class
    val builder = CookieSessionBuilder<S>(typeOf<S>()).apply(block)
    val transport = SessionTransportCookie(name, builder.cookie, builder.transformers)
    val tracker =
        SessionTrackByIndex(
            sessionType, builder.serializer, redis, builder.sessionIdProvider
        )
    val provider = SessionProvider(
        name, sessionType, transport, tracker
    )
    register(provider)
    return tracker
}

interface Indexable {
    fun index(): String
}
