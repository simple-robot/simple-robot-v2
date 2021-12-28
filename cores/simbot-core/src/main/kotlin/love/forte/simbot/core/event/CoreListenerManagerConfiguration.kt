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

package love.forte.simbot.core.event

import kotlinx.coroutines.CoroutineScope
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.PriorityConstant
import love.forte.simbot.event.EventListenerInterceptor
import love.forte.simbot.event.EventProcessingInterceptor
import love.forte.simbot.event.EventProcessingResult
import love.forte.simbot.event.EventResult
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@DslMarker
internal annotation class CoreEventManagerConfigDSL

/**
 * [CoreListenerManager] 的配置文件.
 *
 * 当配置文件作为构建参数的时候，他会被立即使用。
 *
 */
@CoreEventManagerConfigDSL
public class CoreListenerManagerConfiguration {

    /**
     * 事件管理器的上下文. 可以基于此提供调度器。
     * 但是 [CoreListenerManager] 并不是一个作用域，因此不可以提供 `Job`.
     */
    @CoreEventManagerConfigDSL
    public var coroutineContext: CoroutineContext = EmptyCoroutineContext

    @Volatile
    @JvmSynthetic
    internal var processingInterceptors =
        TreeSet<EventProcessingInterceptor>(Comparator.comparing { i -> i.id.toString() })

    @Volatile
    @JvmSynthetic
    internal var listenerInterceptors = TreeSet<EventListenerInterceptor>(Comparator.comparing { i -> i.id.toString() })


    /**
     * 自定义的监听函数异常处理器。
     *
     */
    @Volatile
    @JvmSynthetic
    internal var listenerExceptionHandler: ((Throwable) -> EventResult)? = null


    @JvmSynthetic
    @CoreEventManagerConfigDSL
    public fun listenerExceptionHandler(handler: (Throwable) -> EventResult) {
        listenerExceptionHandler = handler
    }

    @Api4J
    @CoreEventManagerConfigDSL
    public fun listenerExceptionHandler(handler: java.util.function.Function<Throwable, EventResult>) {
        listenerExceptionHandler = handler::apply
    }


    /**
     * 添加一个流程拦截器，ID需要唯一。
     * 如果出现重复ID，会抛出 [IllegalStateException] 并且不会真正的向当前配置中追加数据。
     *
     * @throws IllegalStateException 如果出现重复ID
     */
    @Synchronized
    @CoreEventManagerConfigDSL
    public fun addProcessingInterceptors(interceptors: Collection<EventProcessingInterceptor>) {
        val processingInterceptorsCopy =
            TreeSet<EventProcessingInterceptor>(Comparator.comparing { i -> i.id.toString() })
        processingInterceptorsCopy.addAll(processingInterceptors)
        for (interceptor in interceptors) {
            if (!processingInterceptorsCopy.add(interceptor)) {
                throw IllegalStateException("Duplicate ID: ${interceptor.id}")
            }
        }
        processingInterceptors = processingInterceptorsCopy
    }

    /**
     * 添加一个流程拦截器，ID需要唯一。
     * 如果出现重复ID，会抛出 [IllegalStateException] 并且不会真正的向当前配置中追加数据。
     *
     * @throws IllegalStateException 如果出现重复ID
     */
    @Synchronized
    @CoreEventManagerConfigDSL
    public fun addListenerInterceptors(interceptors: Collection<EventListenerInterceptor>) {
        val listenerInterceptorsCopy = TreeSet<EventListenerInterceptor>(Comparator.comparing { i -> i.id.toString() })
        listenerInterceptorsCopy.addAll(listenerInterceptors)
        for (interceptor in interceptors) {
            if (!listenerInterceptorsCopy.add(interceptor)) {
                throw IllegalStateException("Duplicate ID: ${interceptor.id}")
            }
        }
        listenerInterceptors = listenerInterceptorsCopy
    }

    @CoreEventManagerConfigDSL
    public fun interceptors(block: EventInterceptorsGenerator.() -> Unit) {
        val generated = EventInterceptorsGenerator().also(block)
        addListenerInterceptors(generated.listenerInterceptors)
        addProcessingInterceptors(generated.processingInterceptors)
    }


    /**
     * 事件流程上下文的处理器。
     */
    // 暂时不公开
    // @CoreEventManagerConfigDSL
    public var eventProcessingContextResolver: (manager: CoreListenerManager, scope: CoroutineScope) -> EventProcessingContextResolver<*> =
        { _, scope -> CoreEventProcessingContextResolver(scope) }
    internal set



}

@DslMarker
internal annotation class EventInterceptorsGeneratorDSL

@EventInterceptorsGeneratorDSL
public class EventInterceptorsGenerator {
    @Volatile
    private var _processingInterceptors: MutableSet<EventProcessingInterceptor> =
        TreeSet(Comparator.comparing { i -> i.id.toString() })

    public val processingInterceptors: Set<EventProcessingInterceptor>
        get() = _processingInterceptors

    @Volatile
    private var _listenerInterceptors: MutableSet<EventListenerInterceptor> =
        TreeSet(Comparator.comparing { i -> i.id.toString() })

    public val listenerInterceptors: Set<EventListenerInterceptor>
        get() = _listenerInterceptors


    @Synchronized
    private fun addLis(interceptor: EventListenerInterceptor) {
        if (!_listenerInterceptors.add(interceptor)) {
            throw IllegalStateException("Duplicate ID: ${interceptor.id}")
        }
    }

    private fun addPro(interceptor: EventProcessingInterceptor) {
        if (!_processingInterceptors.add(interceptor)) {
            throw IllegalStateException("Duplicate ID: ${interceptor.id}")
        }
    }

    /**
     * 提供一个 [id], [优先级][priority] 和 [拦截函数][interceptFunction],
     * 得到一个流程拦截器 [EventProcessingInterceptor].
     */
    @JvmOverloads
    @EventInterceptorsGeneratorDSL
    public fun processingIntercept(
        id: ID,
        priority: Int = PriorityConstant.NORMAL,
        interceptFunction: suspend (EventProcessingInterceptor.Context) -> EventProcessingResult
    ): EventProcessingInterceptor = coreProcessingInterceptor(id, priority, interceptFunction).also(::addPro)

    @JvmOverloads
    @EventInterceptorsGeneratorDSL
    public fun processingIntercept(
        id: String,
        priority: Int = PriorityConstant.NORMAL,
        interceptFunction: suspend (EventProcessingInterceptor.Context) -> EventProcessingResult
    ): EventProcessingInterceptor = processingIntercept(id.ID, priority, interceptFunction)


    /**
     * 提供一个 [id], [优先级][priority] 和 [拦截函数][interceptFunction],
     * 得到一个流程拦截器 [EventListenerInterceptor].
     */
    @JvmOverloads
    @EventInterceptorsGeneratorDSL
    public fun listenerIntercept(
        id: ID,
        priority: Int = PriorityConstant.NORMAL,
        interceptFunction: suspend (EventListenerInterceptor.Context) -> EventResult
    ): EventListenerInterceptor =
        coreListenerInterceptor(id, priority, interceptFunction).also(::addLis)

    /**
     * 提供一个 [id], [优先级][priority] 和 [拦截函数][interceptFunction],
     * 得到一个流程拦截器 [EventListenerInterceptor].
     */
    @JvmOverloads
    @EventInterceptorsGeneratorDSL
    public fun listenerIntercept(
        id: String,
        priority: Int = PriorityConstant.NORMAL,
        interceptFunction: suspend (EventListenerInterceptor.Context) -> EventResult
    ): EventListenerInterceptor =
        listenerIntercept(id.ID, priority, interceptFunction)


}