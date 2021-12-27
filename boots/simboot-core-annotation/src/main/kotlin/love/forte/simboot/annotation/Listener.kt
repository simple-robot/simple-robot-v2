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

import love.forte.simboot.listener.EventListenerTextContentProcessor
import love.forte.simbot.PriorityConstant
import kotlin.reflect.KClass


/**
 * 标记一个函数为监听器/监听函数，并尝试自动检测其监听类型。
 *
 * 当标记在一个kotlin扩展函数上的时候，此函数的 receiver 应当是你想要监听的事件类型，eg：
 * ```kotlin
 *
 * @Listener
 * suspend fun ChannelMessageEvent.myListener() { ... }
 *
 * ```
 * 默认情况下，此函数的ID为其全限定名，你可以通过 [Listener.id] 指定一个ID。
 * 当不指定的时候默认为当前标记对象的全限定二进制名称。
 *
 * ### textContentProcessors
 * 你可以通过 [textContentProcessors] 来对一次事件中的 `textContent` 进行前置处理, 当使用多个的时候，
 * 他们会依次执行。
 *
 * ```kotlin
 * @Listener(textContentProcessors = [StandardTextContentProcessor.Trim::class])
 * @Filter("abc") // 此处'abc'对事件的文本内容[textContent]进行匹配的时候，匹配目标将会被提前trim
 * suspend fun Event.myListener() { ... }
 *
 * ```
 *
 * @property id 监听函数ID。
 * @property priority 此事件的优先级。
 * @property async 此函数是否为异步函数。
 * @property textContentProcessors 针对于 [love.forte.simbot.event.EventListenerProcessingContext.textContent] 的前置处理器。
 * 如果为 `object` 类型，则会直接获取实例，否则会尝试通过可用途径获取对应结果。
 *
 * @see love.forte.simbot.event.EventListener
 * @see EventListenerTextContentProcessor
 * @see love.forte.simboot.listener.StandardTextContentProcessor
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
public annotation class Listener(
    val id: String = "",
    val priority: Int = PriorityConstant.NORMAL,
    val async: Boolean = false,
    // TODO
    val textContentProcessors: Array<KClass<out EventListenerTextContentProcessor>> = []
)