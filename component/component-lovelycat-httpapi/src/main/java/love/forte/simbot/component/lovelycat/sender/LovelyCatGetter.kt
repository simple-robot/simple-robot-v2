/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  simple-robot-S
 *  * File     LovelyCatGetter.kt
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *  *
 *  *
 *
 */

package love.forte.simbot.component.lovelycat.sender

import love.forte.simbot.api.message.containers.BotInfo
import love.forte.simbot.api.message.results.*
import love.forte.simbot.api.sender.Getter
import love.forte.simbot.component.lovelycat.LovelyCatApiTemplate
import love.forte.simbot.component.lovelycat.message.CatGroupInfo
import love.forte.simbot.component.lovelycat.message.event.GROUP_SUFFIX
import love.forte.simbot.component.lovelycat.message.event.lovelyCatBotInfo
import love.forte.simbot.component.lovelycat.message.result.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 可爱猫取信器。
 */
public class LovelyCatGetter(
    private val botId: String,
    private val api: LovelyCatApiTemplate,
    private val def: Getter
) : Getter {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext
    /**
     * 得到当前bot的权限信息。
     */
    override val authInfo: AuthInfo
        get() = emptyAuthInfo()

    /**
     * 获取当前bot的基础信息。
     */
    override val botInfo: BotInfo
        get() = lovelyCatBotInfo(botId, api)


    /**
     * 获取一个好友的信息。
     */
    override suspend fun friendInfo(code: String): FriendInfo {
        val friendList = api.getFriendList(botId, true)
        return friendList.find { it.wxid == code } ?: throw NoSuchElementException("friend: $code")
    }


    /**
     * 获取一个群友信息。
     */
    override suspend fun memberInfo(group: String, code: String): GroupMemberInfo {
        val groupMemberDetailInfo = api.getGroupMemberDetailInfo(botId, group, code, true)
        val groupInfo = findGroupInfo(group)
        return LovelyCatGroupMemberInfo(groupMemberDetailInfo, groupInfo)
    }

    /**
     * 获取一个群详细信息
     */
    override suspend fun groupInfo(group: String): GroupFullInfo {
        val groupInfo = findGroupInfo(group)
        val memberSize = api.getGroupMemberList(botId, group, true).size
        return LovelyCatGroupFullInfo(groupInfo.toString(), groupInfo, memberSize)
    }

    override suspend fun groupInfo(group: Long): GroupFullInfo = groupInfo("$group$GROUP_SUFFIX")


    /**
     * 获取好友列表
     * @param cache 是否使用缓存。
     */
    override suspend fun friendList(cache: Boolean, limit: Int): FriendList {
        val friendList = api.getFriendList(botId, !cache)

        return if (limit > 0 && limit < friendList.size) {
            LovelyCatFriendList(friendList.subList(0, limit - 1))
        } else LovelyCatFriendList(friendList)
    }

    /**
     * 获取群列表
     * @param cache 是否使用缓存。
     */
    override suspend fun groupList(cache: Boolean, limit: Int): GroupList {
        val groupList = api.getGroupList(botId, !cache)

        return if (limit > 0 && limit < groupList.size) {
            LovelyCatGroupList(groupList.subList(0, limit - 1))
        } else LovelyCatGroupList(groupList)

    }

    /**
     * 获取群成员列表
     */
    override suspend fun groupMemberList(group: String, cache: Boolean, limit: Int): GroupMemberList {
        val groupInfo = findGroupInfo(group)

        val memberList = api.getGroupMemberList(botId, group, !cache)

        return LovelyCatGroupMemberList(groupInfo, memberList)
    }

    override suspend fun groupMemberList(group: Long, cache: Boolean, limit: Int): GroupMemberList =
        groupMemberList("$group$GROUP_SUFFIX", cache, limit)

    /**
     * 无法获取禁言列表。
     */
    @Deprecated("Unable to get banned list.")
    override suspend fun banList(group: String, cache: Boolean, limit: Int) = def.banList(group, cache, limit)

    @Suppress("DEPRECATION", "DeprecatedCallableAddReplaceWith")
    @Deprecated("Unable to get banned list.")
    override suspend fun banList(group: Long, cache: Boolean, limit: Int): MuteList =
        banList("$group$GROUP_SUFFIX", cache, limit)


    /**
     * 无法获取群公告。
     */
    @Deprecated("Unable to get the group note list.")
    override suspend fun groupNoteList(group: String, cache: Boolean, limit: Int) = def.groupNoteList(group, cache, limit)

    @Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
    @Deprecated("Unable to get the group note list.")
    override suspend fun groupNoteList(group: Long, cache: Boolean, limit: Int): GroupNoteList =
        groupNoteList("$group$GROUP_SUFFIX", cache, limit)



    @Suppress("NOTHING_TO_INLINE")
    private inline fun findGroupInfo(group: String): CatGroupInfo {
        return api.getGroupList(botId, true).find { it.groupCode == group }
            ?: throw NoSuchElementException("group: $group")
    }

}





