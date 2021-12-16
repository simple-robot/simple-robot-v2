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

@file:JvmName("AnnotationParseUtil")

package love.forte.simboot.annotation

import love.forte.simboot.filter.FilterData
import love.forte.simboot.filter.FiltersData
import love.forte.simboot.filter.TargetFilterData
import love.forte.simboot.listener.ListenData
import love.forte.simboot.listener.ListenerData
import love.forte.simboot.listener.ListensData


public fun Filter.toData(): FilterData {
    return FilterData(
        value = value,
        matchType = matchType,
        target = target.toData(),
        and = and.toData(),
        or = or.toData(),
        processor = processor

    )
}

public fun TargetFilter.toData(): TargetFilterData {
    return TargetFilterData(
        components = components.toList(),
        bots = bots.toList(),
        authors = authors.toList(),
        groups = groups.toList(),
        channels = channels.toList(),
        guilds = guilds.toList()
    )
}

public fun Filters.toData(): FiltersData {
    return FiltersData(
        value = value.map(Filter::toData),
        multiMatchType = multiMatchType,
        processor = processor,
    )
}


/**
 * 将 [Listen] 转化为 [ListenData].
 *
 */
public fun Listen.toData(): ListenData {
    return ListenData(value)
}

public fun Listens.toData(): ListensData {
    return ListensData(value.map(Listen::toData))
}

@JvmOverloads
public fun Listener.toData(listens: ListensData? = null): ListenerData {
    return ListenerData(
        id = id,
        priority = priority,
        listens = listens
    )
}


public fun Listener.toData(listens: Listens): ListenerData {
    return toData(listens.toData())
}

