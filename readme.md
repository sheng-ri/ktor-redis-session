### Usage
* implement `RedisCommand`
* configuration session
```kotlin
val tracker = redisCookie<MySession>("sessionName", LettuceCommand(redis)) {
    cookie.extensions["SameSite"] = "Strict"
}
application.attributes.put(TRACK_KEY, tracker)
```
* Using `tracker` to found session