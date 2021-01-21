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

package love.forte.test.listener

import love.forte.common.ioc.annotation.Beans
import love.forte.simbot.annotation.OnGroup
import love.forte.simbot.annotation.OnPrivate
import love.forte.simbot.api.message.events.MsgGet

/**
 * @author ForteScarlet
 */
@Beans
class TestListener {

    @OnPrivate
    @OnGroup
    fun listen(msg: MsgGet) {
        println(msg)

    }// = Reply.reply("Hello. this is Quick reply.", at = true)
}

