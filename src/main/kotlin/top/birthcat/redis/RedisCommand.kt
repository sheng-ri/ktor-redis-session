package top.birthcat.redis

import java.util.concurrent.CompletionStage

@Suppress("SpellCheckingInspection")
interface RedisCommand {
    fun get(key: String): CompletionStage<String?>
    fun mset(map: Map<String, String>)
    fun getdel(key: String): CompletionStage<String?>
    fun del(vararg keys: String)
}