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
package love.forte.simbot.core.listener

import love.forte.simbot.api.SimbotExperimentalApi
import love.forte.simbot.filter.FilterData
import love.forte.simbot.listener.ListenResult
import love.forte.simbot.listener.ListenerFunction
import love.forte.simbot.listener.ListenerFunctionInvokeData
import love.forte.simbot.listener.ListenerInvoker


public class ListenerInvokerImpl(override val function: ListenerFunction) : ListenerInvoker {

    @OptIn(SimbotExperimentalApi::class)
    private fun doFilter(data: ListenerFunctionInvokeData): Boolean {
        return filter?.test(FilterData(data.msgGet, data.atDetection, data.context, function)) ?: true
    }

    override suspend fun invoke(data: ListenerFunctionInvokeData): ListenResult<*> {
        if (!doFilter(data) || data.listenerInterceptorChain.intercept().prevent) {
            return ListenResult
        }

        return function(data)
    }
}





