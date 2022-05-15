/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simply-robot (或称 simple-robot 3.x 、simbot 3.x ) 的一部分。
 *
 *  simply-robot 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU 通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simply-robot 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU 通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU 通用公共许可证的复本。如果没有，请看:
 *  https://www.gnu.org/licenses
 *  https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *  https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 *
 */

package love.forte.simbot.application

import love.forte.simbot.Bot
import love.forte.simbot.BotVerifyInfo
import love.forte.simbot.Component
import love.forte.simbot.ComponentFactory
import love.forte.simbot.ability.CompletionPerceivable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 用于构建 [Application.Environment] 的工厂。
 *
 * @author ForteScarlet
 */
public interface ApplicationFactory<
        Config : ApplicationConfiguration,
        Builder : ApplicationBuilder<A>,
        A : Application,
        > {
    
    /**
     * 提供配置函数和构建器函数，构建一个 [Application] 实例。
     */
    public suspend fun create(configurator: Config.() -> Unit, builder: suspend Builder.(Config) -> Unit): A
}


/**
 * [Application] 的构建器.
 * @param A 目标 [Application] 类型
 */
public interface ApplicationBuilder<A : Application> : CompletionPerceivable<A> {
    
    /**
     * 注册一个 [组件][Component].
     */
    @ApplicationBuilderDsl
    public fun <C : Component, Config : Any> install(
        componentFactory: ComponentFactory<C, Config>,
        configurator: Config.(perceivable: CompletionPerceivable<A>) -> Unit = {},
    )
    
    /**
     * 注册一个事件提供者。
     */
    @ApplicationBuilderDsl
    public fun <P : EventProvider, Config : Any> install(
        eventProviderFactory: EventProviderFactory<P, Config>,
        configurator: Config.(perceivable: CompletionPerceivable<A>) -> Unit = {},
    )
    
    
    /**
     * 提供一个可以使用 [BotVerifyInfo] 进行通用性bot注册的配置方式。
     */
    @ApplicationBuilderDsl
    public fun bots(registrar: suspend BotRegistrar.() -> Unit)
    
    
    /**
     * 注册一个当 [Application] 构建完成后的回调函数。
     *
     * 假如当前builder已经构建完毕，再调用此函数无效果。
     */
    @ApplicationBuilderDsl
    override fun onCompletion(handle: suspend (application: A) -> Unit)
    
}


/**
 *
 * TODO 补注释
 */
public interface BotRegistrar {
    
    /**
     * TODO 补注释
     */
    public fun register(botVerifyInfo: BotVerifyInfo): Bot?
}


/**
 * 标记为用于 [ApplicationBuilder] 的 dsl api.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class ApplicationBuilderDsl


/**
 * 整个应用程序进行构建所需的基本配置信息。
 */
public open class ApplicationConfiguration {
    
    /**
     * 当前application内所使用的协程上下文。
     *
     */
    public open var coroutineContext: CoroutineContext = EmptyCoroutineContext
    
    /**
     * 提供一个用于Application内部的日志对象。
     */
    public open var logger: Logger = LoggerFactory.getLogger("love.forte.simbot.application.ApplicationConfiguration")
    
}