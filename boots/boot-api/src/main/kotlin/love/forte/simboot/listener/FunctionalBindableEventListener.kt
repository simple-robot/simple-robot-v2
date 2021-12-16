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

package love.forte.simboot.listener

import love.forte.di.BeanContainer
import love.forte.simbot.Api4J
import love.forte.simbot.PriorityConstant
import love.forte.simbot.SimbotIllegalStateException
import love.forte.simbot.event.EventProcessingContext
import love.forte.simbot.event.EventResult
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend


/**
 *
 * 可以进行动态参数绑定的 [FunctionalEventListener],
 * 可以通过 [binder] 对 [caller] 进行参数绑定。
 *
 *
 */
public abstract class FunctionalBindableEventListener<R> : FunctionalEventListener<R>(), GenericBootEventListener {

    /**
     * 当前监听函数所对应的执行器。
     */
    protected abstract override val caller: KFunction<R>

    /**
     * binder数组，其索引下标与 [KCallable.parameters] 的 [KParameter.index] 相对应。
     */
    protected abstract val binder: Array<ParameterBinder>

    /**
     * 对 [caller] 执行后的返回值进行处理并转化为 [EventResult]. 可覆盖并自定义结果逻辑。
     *
     * 默认情况下，如果结果是 [EventResult] 类型，则会直接返回, 否则通过 [EventResult.of] 转化为 [EventResult].
     *
     */
    protected open suspend fun resultProcess(result: R): EventResult {
        return if (result is EventResult) result
        else EventResult.of(result)
    }

    /**
     * 函数执行。
     */
    final override suspend fun invoke(context: EventProcessingContext): EventResult {
        val result = caller.callSuspend(binder.map { it.arg(context) })
        return resultProcess(result)
    }

}


/**
 * 监听函数动态参数的绑定器。通过所需的执行参数而得到的参数绑定器。
 *
 * 对于一个可执行函数的参数 [KParameter] 所需的结果获取器。
 *
 *
 */
public interface ParameterBinder {

    /**
     * 根据当前事件处理上下文得到参数值。
     *
     * 如果出现无法为当前参数提供注入的情况，通过返回 [Result.Failure] 或抛出异常来提示处理器使用下一个顺序的处理器。
     *
     * @throws BindException 出现无法处理的预期内异常。
     * @throws Throwable 其他预期外的异常。
     */
    @JvmSynthetic
    public suspend fun arg(context: EventProcessingContext): Result<Any?>

}

/**
 * 为 [ParameterBinder] 提供阻塞的Java友好接口, 提供 [getArg] 代替挂起的 [arg] 来提供binder参数获取功能。
 *
 * @see ParameterBinder
 */
@Api4J
public interface ParameterBlockingBinder : ParameterBinder {

    @Api4J
    public fun getArg(context: EventProcessingContext): Any?

    @JvmSynthetic
    override suspend fun arg(context: EventProcessingContext): Result<Any?> {
        return kotlin.runCatching { getArg(context) }
    }
}


/**
 * [ParameterBinder] 的解析工厂，通过提供部分预处理参数来解析得到 [ParameterBinder] 实例。
 */
public interface ParameterBinderFactory {

    /**
     * 根据 [Context] 提供的各项参数进行解析与预变异，并得到一个最终的 [ParameterBinder] 到对应的parameter中。
     * 如果返回 [ParameterBinderResult.Empty] ，则视为放弃对目标参数的匹配。
     *
     * 返回值最终会被整合，并按照 [ParameterBinderResult.priority] 的顺序作为此binder的执行顺序。
     *
     * 在监听函数被执行时将会通过解析的 [ParameterBinder] 对参数进行注入，
     * 会依次执行对应的binder取第一个执行成功的.
     *
     */
    public fun resolveToBinder(context: Context): ParameterBinderResult


    /**
     * [ParameterBinderFactory] 进行参数处理时的可用参数内容. 由解析注解监听函数的解析器进行提供。
     */
    public interface Context {
        /**
         * 目标监听函数所对应的函数体。
         */
        public val source: KFunction<*>

        /**
         * 当前的处理参数目标。
         */
        public val parameter: KParameter

        /**
         * Bean容器，代表在当前环境下所能够提供的Bean获取器。
         */
        public val beanContainer: BeanContainer


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

    public companion object {
        @JvmStatic
        public fun empty(): Empty = Empty

        @JvmStatic
        public fun normal(binder: ParameterBinder, priority: Int = PriorityConstant.NORMAL): NotEmpty =
            Normal(binder, priority)

        @JvmStatic
        public fun only(binder: ParameterBinder, priority: Int = PriorityConstant.NORMAL): NotEmpty =
            Only(binder, priority)

        @JvmStatic
        public fun spare(binder: ParameterBinder, priority: Int = PriorityConstant.NORMAL): NotEmpty =
            Spare(binder, priority)

    }

    /**
     * 没有结果. 此结果应当被抛弃.
     */
    public object Empty : ParameterBinderResult() {
        override val binder: ParameterBinder? get() = null
    }

    /**
     * 存在结果的result。
     */
    public sealed class NotEmpty : ParameterBinderResult() {
        abstract override val binder: ParameterBinder
    }


    /**
     * 基础的结果，会作为所有binder集合中的一员。
     */
    public class Normal internal constructor(
        override val binder: ParameterBinder,
        override val priority: Int
    ) : NotEmpty()

    /**
     * 唯一的结果。当返回此结果的时候，会抛弃此结果之前的所有结果，并仅保留此结果。
     * 并且直到遇到下一个 [Only] 之前，不会追加其他结果。当遇到下一个 [Only] 后，当前的唯一结果将会被替换。
     */
    public class Only internal constructor(
        override val binder: ParameterBinder,
        override val priority: Int
    ) : NotEmpty()

    /**
     * 备用的结果。当其他结果没有任何执行成功的结果时才会使用 [Spare] 中所提供的内容进行尝试。
     * 不会与 [Only] 发生冲突，因此 [Spare] binder 与普通binder是分离的。
     *
     * [Spare] 同样可以存在多个，但是不存在 `Only` 的备用binder。
     *
     */
    public class Spare internal constructor(
        override val binder: ParameterBinder,
        override val priority: Int
    ) : NotEmpty()


}

/**
 * 当 [ParameterBinder.arg] 中出现了异常。
 */
public open class BindException : SimbotIllegalStateException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}