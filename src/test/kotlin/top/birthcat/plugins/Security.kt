package top.birthcat.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import io.lettuce.core.RedisClient
import top.birthcat.redis.Indexable
import top.birthcat.redis.SessionTrackByIndex
import top.birthcat.redis.redisCookie

data class MySession(val name: String,val count: Int = 0): Indexable {
    override fun index(): String {
        return name
    }
}

val TRACK_KEY = AttributeKey<SessionTrackByIndex<MySession>>("My-Session-Tracker")

fun Application.configureSecurity() {
    val redis = RedisClient.create("redis://localhost").connect().async()
    install(Sessions) {
        val tracker = redisCookie<MySession>("test", LettuceCommand(redis)) {
            cookie.extensions["SameSite"] = "Strict"
        }
        this@configureSecurity.attributes.put(TRACK_KEY, tracker)
    }
    routing {
        get("/session/increment") {
            val tracker = application.attributes[TRACK_KEY]
            val mySession = tracker.load("1")
            if (mySession != null) {
                tracker.store(mySession.copy(count = mySession.count + 10))
            }
            val session = call.sessions.get<MySession>() ?: MySession("1")
//            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}
