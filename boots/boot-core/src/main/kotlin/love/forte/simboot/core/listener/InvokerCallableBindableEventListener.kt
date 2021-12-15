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

package love.forte.simboot.core.listener

import love.forte.simbot.PriorityConstant
import love.forte.simbot.event.EventProcessingContext
import love.forte.simbot.event.EventResult
import kotlin.reflect.KCallable


/**
 * 提供
 *
 *
 */
public abstract class InvokerCallableBindableEventListener<R> : GenericBootEventListener {

    /**
     * 当前监听函数所对应的执行器。
     */
    protected abstract val caller: KCallable<R>


    override suspend fun invoke(context: EventProcessingContext): EventResult {
        TODO("Not yet implemented")
    }

}


/**
 * 参数绑定器。通过所需的执行参数而得到的参数绑定器。
 *
 * 对于一个可执行函数的参数列表 [KParameter]
 *
 *
 */
public interface ParameterBinder {

}


public interface ParameterBinderFactory {

    /**
     * 根据所提供的各项参数得到一个最终的 [ParameterBinder] 到对应的parameter中。
     * 如果返回 [ParameterBinderResult.Null] ，则视为放弃对目标参数的匹配。
     *
     */
    public fun resolveToBinder(context: Context): ParameterBinderResult

    /**
     * [ParameterBinderFactory] 进行参数处理时的可用参数内容. 由解析注解监听函数的解析器进行提供。
     */
    public interface Context {

    }


}


/**
 * [ParameterBinderFactory] 的解析处理结果返回值。
 */
public sealed class ParameterBinderResult {
    /**
     * binder
     */
    public abstract val binder: ParameterBinder?

    /**
     * 优先级
     */
    public open val priority: Int = PriorityConstant.NORMAL

    /**
     * 没有结果. 此结果将会被抛弃.
     */
    public object Null : ParameterBinderResult() {
        override val binder: ParameterBinder? get() = null
    }

    public class Normal


}