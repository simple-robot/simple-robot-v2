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

package love.forte.simbot.kaiheila.event.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.api.message.MessageContent
import love.forte.simbot.api.message.assists.Permissions
import love.forte.simbot.api.message.containers.GroupAccountInfo
import love.forte.simbot.api.message.containers.GroupBotInfo
import love.forte.simbot.api.message.containers.GroupInfo
import love.forte.simbot.api.message.events.*
import love.forte.simbot.kaiheila.botAvatar
import love.forte.simbot.kaiheila.botCode
import love.forte.simbot.kaiheila.botCodeNumber
import love.forte.simbot.kaiheila.botName
import love.forte.simbot.kaiheila.event.Event
import love.forte.simbot.kaiheila.event.EventLocator
import love.forte.simbot.kaiheila.event.EventLocatorRegistrarCoordinate
import love.forte.simbot.kaiheila.event.registerCoordinate
import love.forte.simbot.kaiheila.objects.Attachments
import love.forte.simbot.kaiheila.objects.Channel
import love.forte.simbot.kaiheila.objects.User


/**
 * [文件消息](https://developer.kaiheila.cn/doc/event/message#%E6%96%87%E4%BB%B6%E6%B6%88%E6%81%AF)
 * @author ForteScarlet
 */
@Serializable
public data class FileEventExtra(
    @SerialName("guild_id")
    override val guildId: String = "",
    @SerialName("channel_name")
    override val channelName: String = "",

    override val mention: List<String> = emptyList(),
    override val mentionAll: Boolean = false,
    override val mentionRoles: List<Long> = emptyList(),
    override val mentionHere: Boolean = false,
    /**
     * 附件
     */
    override val attachments: Attachments,
    override val author: User,
) : AttachmentsMessageEventExtra<Attachments> {
    override val type: Int
        get() = Event.Type.FILE.type
}

@Serializable
public data class FileAttachments(
    override val type: String,
    override val url: String,
    override val name: String,
    override val size: Long,
    @SerialName("file_type")
    val fileType: String
): Attachments


@Serializable
internal sealed class FileEventImpl : AbstractMessageEvent<FileEventExtra>(), FileEvent {
    override val type: Event.Type
        get() = Event.Type.FILE

    /**
     * 群消息.
     */
    @Serializable
    public data class Group(
        @SerialName("target_id")
        override val targetId: String,
        @SerialName("author_id")
        override val authorId: String,
        override val content: String,
        @SerialName("msg_id")
        override val msgId: String,
        @SerialName("msg_timestamp")
        override val msgTimestamp: Long,
        override val nonce: String,
        override val extra: FileEventExtra,
    ) : FileEventImpl(), GroupMsg, FileEvent.Group {


        override val channelType: Channel.Type get() = Channel.Type.GROUP
        override val groupMsgType: GroupMsg.Type = if (authorId == "1") GroupMsg.Type.SYS else GroupMsg.Type.NORMAL

        @Transient
        override val flag: MessageGet.MessageFlag<GroupMsg.FlagContent> =
            MessageFlag(GroupMsgIdFlagContent(msgId))

        //region GroupAccountInfo Ins
        private inner class ImageEventGroupAccountInfo : GroupAccountInfo, GroupInfo, GroupBotInfo {
            override val accountCode: String get() = extra.author.accountCode
            override val accountNickname: String get() = extra.author.accountNickname
            override val accountRemark: String? get() = extra.author.accountRemark
            override val accountAvatar: String get() = extra.author.accountAvatar

            @Suppress("DEPRECATION")
            override val accountTitle: String?
                get() = extra.author.accountTitle

            override val botCode: String get() = bot.botCode
            override val botCodeNumber: Long get() = bot.botCodeNumber
            override val botName: String get() = bot.botName
            override val botAvatar: String? get() = bot.botAvatar

            @Suppress("DEPRECATION")
            override val permission: Permissions
                get() = extra.author.permission

            override val groupAvatar: String?
                get() = null // TODO("Not yet implemented")

            override val parentCode: String get() = extra.guildId
            override val groupCode: String get() = targetId
            override val groupName: String get() = extra.channelName
        }

        @Transient
        private val textEventGroupAccountInfo = ImageEventGroupAccountInfo()

        override val permission: Permissions get() = textEventGroupAccountInfo.permission
        override val accountInfo: GroupAccountInfo get() = textEventGroupAccountInfo
        override val groupInfo: GroupInfo get() = textEventGroupAccountInfo
        override val botInfo: GroupBotInfo get() = textEventGroupAccountInfo
        //endregion

        /**
         * Event coordinate.
         */
        companion object Coordinate : EventLocatorRegistrarCoordinate<Group> {
            override val type: Event.Type get() = Event.Type.FILE

            override val channelType: Channel.Type get() = Channel.Type.GROUP

            override val extraType: String
                get() = type.type.toString()

            override fun coordinateSerializer(): KSerializer<Group> = serializer()
        }
    }

    /**
     * 私聊消息.
     */
    @Serializable
    public data class Person(
        @SerialName("target_id")
        override val targetId: String,
        @SerialName("author_id")
        override val authorId: String,
        override val content: String,
        @SerialName("msg_id")
        override val msgId: String,
        @SerialName("msg_timestamp")
        override val msgTimestamp: Long,
        override val nonce: String,
        override val extra: FileEventExtra,
    ) : FileEventImpl(), PrivateMsg, FileEvent.Person {
        override val channelType: Channel.Type
            get() = Channel.Type.PERSON

        override val privateMsgType: PrivateMsg.Type
            get() = PrivateMsg.Type.FRIEND

        override val flag: MessageGet.MessageFlag<PrivateMsg.FlagContent> = MessageFlag(PrivateMsgIdFlagContent(msgId))

        companion object : EventLocatorRegistrarCoordinate<Person> {
            override val type: Event.Type get() = Event.Type.FILE

            override val channelType: Channel.Type get() = Channel.Type.PERSON

            override val extraType: String
                get() = type.type.toString()

            override fun coordinateSerializer(): KSerializer<Person> = serializer()
        }
    }


    protected override fun initMessageContent(): MessageContent = attachmentsEventMessageContent("file", extra)

    internal companion object {
        internal fun EventLocator.registerCoordinates() {
            registerCoordinate(Group)
            registerCoordinate(Person)
        }
    }
}



