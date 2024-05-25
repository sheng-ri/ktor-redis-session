package top.birthcat.plugins

import io.lettuce.core.api.async.RedisAsyncCommands
import top.birthcat.redis.RedisCommand
import java.util.concurrent.CompletionStage

class LettuceCommand(private val redis: RedisAsyncCommands<String,String?>)
    : RedisCommand {

    override fun get(key: String): CompletionStage<String?> {
        return redis.get(key)
    }

    override fun mset(map: Map<String, String>) {
        redis.mset(map)
    }

    override fun getdel(key: String): CompletionStage<String?> {
        return redis.getdel(key)
    }

    override fun del(vararg keys: String) {
        redis.del(*keys)
    }


}