
package top.birthcat.redis

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.future.await
import kotlin.reflect.KClass

const val SESSION_DATA_PREFIX = "ktor.session.data"
const val SESSION_INDEX_PREFIX = "ktor.session.index"

class SessionTrackByIndex<S : Indexable>(
    private val type: KClass<S>,
    val serializer: SessionSerializer<S>,
    val redis: RedisCommand,
    private val sessionIdProvider: () -> String
) : SessionTracker<S> {
    private val sessionIdKey: AttributeKey<String> = AttributeKey("SessionId")

    suspend fun load(name: String): S? {
        val sessionId = redis.get("$SESSION_INDEX_PREFIX.${name}").await()
            ?: return null
        val session = redis.get("$SESSION_DATA_PREFIX.${sessionId}.data").await()!!
        return serializer.deserialize(session)
    }

    @Suppress("NAME_SHADOWING", "MemberVisibilityCanBePrivate")
    suspend inline fun store(value: S,sessionId: String? = null, ) {
        var sessionId = sessionId
        if (sessionId == null) {
            sessionId = redis.get("$SESSION_INDEX_PREFIX.${value.index()}").await()
                ?: return
        }
        val serialized = serializer.serialize(value)
        redis.mset(mapOf(
            Pair("$SESSION_DATA_PREFIX.${sessionId}.data",serialized),
            Pair("$SESSION_DATA_PREFIX.${sessionId}.name",value.index()),
            Pair("$SESSION_INDEX_PREFIX.${value.index()}",sessionId)
        ))
    }

    override suspend fun load(call: ApplicationCall, transport: String?): S? {
        val sessionId = transport ?: return null

        call.attributes.put(sessionIdKey, sessionId)
        try {
            val serialized = redis.get("$SESSION_DATA_PREFIX.${sessionId}.data").await()
                ?: throw NoSuchElementException()
            return serializer.deserialize(serialized)
        } catch (notFound: NoSuchElementException) {
            call.application.log.debug(
                "Failed to lookup session: ${notFound.message ?: notFound.toString()}. " + "The session id is wrong or outdated."
            )
        }

        // Remove the wrong session identifier if no related session was found
        call.attributes.remove(sessionIdKey)

        return null
    }

    override suspend fun store(call: ApplicationCall, value: S): String {
        val sessionId = call.attributes.computeIfAbsent(sessionIdKey, sessionIdProvider)
        store(value,sessionId)
        return sessionId
    }

    override suspend fun clear(call: ApplicationCall) {
        val sessionId = call.attributes.takeOrNull(sessionIdKey)
        if (sessionId != null) {
            val name = redis.getdel("$SESSION_INDEX_PREFIX.${sessionId}.name").await()
                ?: return
            redis.del(
                "$SESSION_INDEX_PREFIX.${sessionId}.name",
                "$SESSION_INDEX_PREFIX.${sessionId}.data",
                "$SESSION_INDEX_PREFIX.${name}"
            )
        }
    }

    override fun validate(value: S) {
        if (!type.isInstance(value)) {
            throw IllegalArgumentException("Value for this session tracker expected to be of type $type but was $value")
        }
    }

    override fun toString(): String {
        return "RedisSessionTrackerById: $redis"
    }
}