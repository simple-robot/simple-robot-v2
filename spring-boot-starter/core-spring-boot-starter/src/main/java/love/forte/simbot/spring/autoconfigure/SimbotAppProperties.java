/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
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

package love.forte.simbot.spring.autoconfigure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
@RequiredArgsConstructor
public class SimbotAppProperties {
    @Getter
    private final Class<?> appClass;
}
