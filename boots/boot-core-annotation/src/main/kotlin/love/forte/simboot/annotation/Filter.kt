/*
 *  Copyright (c) 2021-2021 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 */

package love.forte.simboot.annotation

import love.forte.simboot.filter.FilterAnnotationProcessor
import love.forte.simboot.filter.FiltersAnnotationProcessor
import love.forte.simboot.filter.MatchType
import love.forte.simboot.filter.MultiFilterMatchType
import love.forte.simbot.event.ChannelEvent
import love.forte.simbot.event.GroupEvent
import love.forte.simbot.event.GuildEvent
import kotlin.reflect.KClass

/**
 * 与 [Listener] 或 [Listen] 配合使用，会被解析为对应监听函数的默认过滤器。
 *
 * @property value 匹配规则值。
 * @property matchType 针对匹配目标所使用的匹配规则。
 * 默认情况下使用 [正则完全匹配][MatchType.REGEX_MATCHES].
 *
 * @property target 目标过滤内容 see [TargetFilter]
 * @property and 可以再提供一个 `&` 与关系的子过滤器，最终结果为 `当前filter && and-filters`
 *
 * ### Kotlin
 * ```kotlin
 * @Filter("Foo1")
 * @Filter("Foo2", and = Filters(
 *      Filter("Foo3"),
 *      Filter("Foo4"),
 * ))
 * @Listener
 * suspend fun Event.test(){ ... }
 * ```
 *
 * ### Java
 * ```java
 * @Filter("Foo1")
 * @Filter(value = "Foo2", and = @Filters(value = {
 *       @Filter("Foo3"),
 *       @Filter("Foo4")
 * }))
 * @Listener
 * public void listen() {
 * }
 *
 * ```
 *
 *
 * 与 [or] 同时存在时候，匹配效果则如：`this-filter && and-filters || or-filters`
 *
 * 不建议在注解中存在过多的filter嵌套，假如有需要，考虑使用 [processor].
 *
 * @property or 可以再提供一个 `|` 或关系的子过滤器，最终结果为 `当前filter || and-filters`.
 *
 * ### Kotlin
 * ```kotlin
 * @Filter("Foo1")
 * @Filter("Foo2", or = Filters(
 *      Filter("Foo3"),
 *      Filter("Foo4"),
 * ))
 * @Listener
 * suspend fun Event.test(){ ... }
 * ```
 *
 * ### Java
 * ```java
 * @Filter("Foo1")
 * @Filter(value = "Foo2", or = @Filters(value = {
 *       @Filter("Foo3"),
 *       @Filter("Foo4")
 * }))
 * @Listener
 * public void listen() {
 * }
 *
 * ```
 *
 * 与 [and] 同时存在时候，匹配效果则如：`this filter && and filter || or filter`
 *
 * @property processor 当前注解应使用的注解处理器。
 * 对应的处理器类型如果是 `object` 类型，则会直接获取其实例。
 *
 * 如果为普通class则在环境允许的情况下会尝试通过依赖管理器获取，
 * 如果当前环境不存在诸如依赖获取器一类的东西，则会直接实例化。
 *
 * 当指定了自定义 [processor] 后，
 * 上述所有字段的最终表现形式均无法得到保证，其最终结果由指定的处理器全权负责。
 *
 * 当 [Filters.processor] 被重新指定后，无法保证对于当前 [processor] 的处理方式。
 *
 *
 */
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@JvmRepeatable(Filters::class)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
public annotation class Filter(
    val value: String,
    val matchType: MatchType = MatchType.REGEX_MATCHES,
    val target: TargetFilter = TargetFilter(),
    val and: Filters = Filters(),
    val or: Filters = Filters(),
    val processor: KClass<out FilterAnnotationProcessor> = FilterAnnotationProcessor::class,
)


/**
 * 针对目标对象（例如事件组件、bot、群、联系人等）的匹配规则，不可标记在任何地方，作为 [Filter] 的参数 [Filter.target].
 *
 * 以下所有属性的匹配结果为并集，即全部匹配成功后得到true。如果是多个参数，假如参数为空，则认为其永远为 true.
 *
 * @property components 对接收事件的组件匹配. 大多数情况下，对于组件的唯一ID，组件实现库都应当有所说明或通过常量提供。 `["comp1", "comp2"]`
 * @property bots 对接收事件的botID匹配。
 * @property authors 对消息发送者的ID匹配。
 * @property groups 如果这是个[群相关事件][GroupEvent] ，则对群ID匹配。
 * @property channels 如果是个[频道相关事件][ChannelEvent], 则对频道ID匹配。
 * @property guilds 如果是个[频道服务器相关事件][GuildEvent], 则对频道服务器ID匹配。
 *
 */
public annotation class TargetFilter(
    val components: Array<String> = [],
    val bots: Array<String> = [],
    val authors: Array<String> = [],
    val groups: Array<String> = [],
    val channels: Array<String> = [],
    val guilds: Array<String> = []
)


/**
 * 多个 [子过滤器][Filter] 的集合。一个 [Filters] 最终会表现为一个汇总过滤器。
 *
 *
 * @property value 所有子过滤器。
 * @property multiMatchType 多个过滤器之间的匹配策略。
 * @property processor 当前注解应使用的注解处理器。
 * 对应的处理器类型如果是 `object` 类型，则会直接获取其实例。
 *
 * 如果为普通class则在环境允许的情况下会尝试通过依赖管理器获取，
 * 如果当前环境不存在诸如依赖获取器一类的东西，则会直接实例化。
 *
 * 当指定了自定义 [processor] 后，
 * 上述所有字段的最终表现形式均无法得到保证，其最终结果由指定的处理器全权负责。
 *
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
public annotation class Filters(
    vararg val value: Filter,
    val multiMatchType: MultiFilterMatchType = MultiFilterMatchType.ALL,
    val processor: KClass<out FiltersAnnotationProcessor> = FiltersAnnotationProcessor::class
)