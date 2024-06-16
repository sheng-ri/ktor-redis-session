package top.birthcat

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import top.birthcat.plugins.configureRouting
import top.birthcat.plugins.configureSecurity

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}
