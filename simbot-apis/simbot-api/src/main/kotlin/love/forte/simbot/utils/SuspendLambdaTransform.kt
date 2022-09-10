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

@file:JvmName("Lambdas")

package love.forte.simbot.utils

import love.forte.simbot.Api4J
import love.forte.simbot.ExperimentalSimbotApi
import java.util.function.*
import java.util.function.Function


// region consumers


/**
 * Kotlin api:
 * ```kotlin
 * fun foo(block: suspend (T) -> Unit) {  }
 * fun bar(block: suspend (T) -> Unit) {  }
 * ```
 *
 * Use it in Java:
 * ```java
 * foo(Lambdas.toSuspend(t -> {}));
 * bar(Lambdas.toSuspend(t -> {}));
 * ```
 *
 *
 */
@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T> toSuspend(function: Consumer<T>, isRunWithInterruptible: Boolean = true): suspend (T) -> Unit =
    if (isRunWithInterruptible) {
        { runWithInterruptible { function.accept(it) } }
    } else {
        { function.accept(it) }
    }


@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2> toSuspend(
    function: BiConsumer<T1, T2>,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2) -> Unit = if (isRunWithInterruptible) {
    { a, b -> runWithInterruptible { function.accept(a, b) } }
} else {
    { a, b -> function.accept(a, b) }
}

@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, T3> toSuspend(
    function: Consumer3<T1, T2, T3>,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2, T3) -> Unit = if (isRunWithInterruptible) {
    { a, b, c -> runWithInterruptible { function.accept(a, b, c) } }
} else {
    { a, b, c -> function.accept(a, b, c) }
}

@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, T3, T4> toSuspend(
    function: Consumer4<T1, T2, T3, T4>,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2, T3, T4) -> Unit = if (isRunWithInterruptible) {
    { a, b, c, d -> runWithInterruptible { function.accept(a, b, c, d) } }
} else {
    { a, b, c, d -> function.accept(a, b, c, d) }
}

@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, T3, T4, T5> toSuspend(
    function: Consumer5<T1, T2, T3, T4, T5>,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2, T3, T4, T5) -> Unit = if (isRunWithInterruptible) {
    { a, b, c, d, e -> runWithInterruptible { function.accept(a, b, c, d, e) } }
} else {
    { a, b, c, d, e -> function.accept(a, b, c, d, e) }
}

@Api4J
@ExperimentalSimbotApi
public fun interface Consumer3<T1, T2, T3> {
    public fun accept(t1: T1, t2: T2, t3: T3)
}

@Api4J
@ExperimentalSimbotApi
public fun interface Consumer4<T1, T2, T3, T4> {
    public fun accept(t1: T1, t2: T2, t3: T3, t4: T4)
}

@Api4J
@ExperimentalSimbotApi
public fun interface Consumer5<T1, T2, T3, T4, T5> {
    public fun accept(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5)
}
// endregion

// region providers & functions

@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <R> toSuspend(function: Supplier<R>, isRunWithInterruptible: Boolean = true): suspend () -> R =
    if (isRunWithInterruptible) {
        { runWithInterruptible { function.get() } }
    } else {
        { function.get() }
    }


@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T, R> toSuspend(function: Function<T, R>, isRunWithInterruptible: Boolean = true): suspend (T) -> R =
    if (isRunWithInterruptible) {
        { runWithInterruptible { function.apply(it) } }
    } else {
        { function.apply(it) }
    }


@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, R> toSuspend(
    function: BiFunction<T1, T2, R>,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2) -> R =
    if (isRunWithInterruptible) {
        { a, b -> runWithInterruptible { function.apply(a, b) } }
    } else {
        { a, b -> function.apply(a, b) }
    }


@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, T3, R> toSuspend(
    function: (T1, T2, T3) -> R,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2, T3) -> R =
    if (isRunWithInterruptible) {
        { a, b, c -> runWithInterruptible { function(a, b, c) } }
    } else {
        { a, b, c -> function(a, b, c) }
    }

@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, T3, T4, R> toSuspend(
    function: (T1, T2, T3, T4) -> R,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2, T3, T4) -> R =
    if (isRunWithInterruptible) {
        { a, b, c, d -> runWithInterruptible { function(a, b, c, d) } }
    } else {
        { a, b, c, d -> function(a, b, c, d) }
    }


@JvmOverloads
@Api4J
@ExperimentalSimbotApi
public fun <T1, T2, T3, T4, T5, R> toSuspend(
    function: (T1, T2, T3, T4, T5) -> R,
    isRunWithInterruptible: Boolean = true,
): suspend (T1, T2, T3, T4, T5) -> R =
    if (isRunWithInterruptible) {
        { a, b, c, d, e -> runWithInterruptible { function(a, b, c, d, e) } }
    } else {
        { a, b, c, d, e -> function(a, b, c, d, e) }
    }
// endregion






