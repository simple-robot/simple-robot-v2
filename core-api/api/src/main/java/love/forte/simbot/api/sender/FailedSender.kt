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

@file:JvmName("FailedSenderFactories")
package love.forte.simbot.api.sender

import love.forte.common.utils.Carrier
import love.forte.simbot.api.message.assists.Flag
import love.forte.simbot.api.message.containers.BotContainer
import love.forte.simbot.api.message.events.GroupMsg
import love.forte.simbot.api.message.events.MsgGet
import love.forte.simbot.api.message.events.PrivateMsg

/**
 * 永远失效的送信器。不会抛出异常，但是也不会生效。
 */
@Suppress("DEPRECATION", "OverridingDeprecatedMember")
public object FailedSender : Sender.Def {

    override suspend fun groupMsg(
        parent: String?,
        group: String,
        msg: String,
    ): Carrier<out Flag<GroupMsg.FlagContent>> = Carrier.empty()

    override suspend fun privateMsg(code: String, group: String?, msg: String): Carrier<out Flag<PrivateMsg.FlagContent>> =
        Carrier.empty()

    override suspend fun groupNotice(
        group: String,
        title: String?,
        text: String?,
        popUp: Boolean,
        top: Boolean,
        toNewMember: Boolean,
        confirm: Boolean,
    ): Carrier<Boolean> = FalseCarrier
}


/**
 * 获取一个总是返回失败默认值的实现类。
 */
@get:JvmName("getFailedSenderFactory")
public val FailedSenderFactory : DefaultSenderFactory = object: DefaultSenderFactory {
    override fun getOnMsgSender(msg: MsgGet): Sender.Def = FailedSender
    override fun getOnBotSender(bot: BotContainer): Sender.Def = FailedSender
}
