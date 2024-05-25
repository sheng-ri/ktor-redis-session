package top.birthcat.redis

import io.ktor.server.sessions.*
import kotlin.reflect.KType

@Suppress("unused")
open class CookieSessionBuilder<S : Any>(
    typeInfo: KType
) {

    var serializer: SessionSerializer<S> = defaultSessionSerializer(typeInfo)

    private val _transformers = mutableListOf<SessionTransportTransformer>()

    /**
     * Gets transformers used to sign and encrypt session data.
     */
    val transformers: List<SessionTransportTransformer> get() = _transformers

    fun transform(transformer: SessionTransportTransformer) {
        _transformers.add(transformer)
    }

    val cookie: CookieConfiguration = CookieConfiguration()

    var sessionIdProvider: () -> String = { generateSessionId() }
        private set
}