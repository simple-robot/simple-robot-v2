/*
 *
 *  * Copyright (c) 2021. ForteScarlet All rights reserved.
 *  * Project  simple-robot
 *  * File     MiraiAvatar.kt
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *
 */

package love.test.guild

import kotlinx.coroutines.*
import love.forte.simbot.listener.ContinuousSessionScopeContext
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation


var continuation: Continuation<Int>? = null

suspend fun waitInt(): Int = suspendCancellableCoroutine {
    continuation = it
}

suspend fun main() {
    val scope = CoroutineScope(Dispatchers.Default)
    val context = ContinuousSessionScopeContext(scope)

    scope.launch {
        delay(2000)
        context.push("KEY", "1", 2)
    }


    val value = context.waiting<Int>("KEY", "1")

    println("value: $value")

    // scope.launch {
    //     delay(200)
    //     context.remove("KEY", "2")
    // }

    try {
        val value2 = context.waiting<Int>("KEY", "2", 200)
    } catch (c: CancellationException) {
        println("cancellation e: $c")
    } catch (c: TimeoutException) {
        println("timeout e:      $c")
    }


    // context.cancel()
    println("over.")
}