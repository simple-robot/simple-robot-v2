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

package love.forte.simbot.kaiheila.api.v3.channel

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.api.message.results.GroupAdmin
import love.forte.simbot.api.message.results.GroupFullInfo
import love.forte.simbot.api.message.results.GroupOwner
import love.forte.simbot.kaiheila.api.*
import love.forte.simbot.kaiheila.objects.Channel
import love.forte.simbot.kaiheila.objects.ChannelPermissionOverwrites


/**
 *
 * [获取频道详情](https://developer.kaiheila.cn/doc/http/channel#%E8%8E%B7%E5%8F%96%E9%A2%91%E9%81%93%E8%AF%A6%E6%83%85)
 *
 * request method: GET
 *
 * @author ForteScarlet
 */
public class ChannelViewReq(private val targetId: String) : GetChannelApiReq<ObjectResp<ChannelView>> {
    companion object Key : ApiData.Req.Key by key("/channel/view") {
        private val ROUTE = listOf("channel", "view")
        private val DATA_SERIALIZER: DeserializationStrategy<ObjectResp<ChannelView>> =
            objectResp(ChannelView.serializer())
    }

    override val key: ApiData.Req.Key get() = Key

    override val dataSerializer: DeserializationStrategy<ObjectResp<ChannelView>>
        get() = DATA_SERIALIZER

    override val body: Any?
        get() = null

    override fun route(builder: RouteInfoBuilder) {
        builder.apiPath = ROUTE
        builder.parameters {
            append("target_id", targetId)
        }
    }
}


/**
 * [频道详情](https://developer.kaiheila.cn/doc/http/channel#%E8%8E%B7%E5%8F%96%E9%A2%91%E9%81%93%E8%AF%A6%E6%83%85)
 */
@Serializable
data class ChannelView(
    /** 频道id */
    override val id: String,
    /** 服务器id */
    @SerialName("guild_id")
    override val guildId: String,
    /** 频道创建者id */
    @SerialName("user_id")
    override val userId: String,
    /** 父分组频道id */
    @SerialName("parent_id")
    override val parentId: String,
    /** 频道名称 */
    override val name: String,
    /** 频道简介 */
    override val topic: String,
    /** 频道类型，1 文字，2 语音 */
    override val type: Int,
    /** 频道排序 */
    override val level: Int,
    /** 慢速限制，单位秒。用户发送消息之后再次发送消息的等待时间。 */
    @SerialName("slow_mode")
    override val slowMode: Int,
    /** 人数限制 */
    @SerialName("limit_amount")
    val limitAmount: Int,
    /** 是否为分组类型 */
    @SerialName("is_category")
    override val isCategory: Boolean,
    /** 语音服务器地址，HOST:PORT的格式 */
    @SerialName("server_url")
    val serverUrl: String,
    // maybe miss
    @SerialName("permission_overwrites")
    override val permissionOverwrites: List<ChannelPermissionOverwrites> = emptyList(),
    @SerialName("permission_users")
    override val permissionUsers: List<String> = emptyList(),
    @SerialName("permission_sync")
    override val permissionSync: Int = 0,

    ) : ChannelApiRespData(), Channel, GroupFullInfo {
    override val originalData: String
        get() = toString()

    override val maximum: Int
        get() = limitAmount

    override var total: Int = -1

    override val createTime: Long
        get() = -1
    override val simpleIntroduction: String?
        get() = null
    override val fullIntroduction: String?
        get() = null
    override lateinit var owner: GroupOwner
    override lateinit var admins: List<GroupAdmin>
}
