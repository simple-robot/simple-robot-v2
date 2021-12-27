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

package love.forte.simboot.autoconfigure

import love.forte.simboot.SimbootApp
import love.forte.simboot.SimbootContext
import love.forte.simboot.core.CoreBootEntranceContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 *
 * 配置用于启动simboot的执行器。
 *
 * @author ForteScarlet
 */
public open class SimbootAppRunnerConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimbootAppRunner::class)
    public fun defaultSimbootAppRunner(context: CoreBootEntranceContext): SimbootAppRunner {
        return DefaultSimbootAppRunner(context)
    }

}


public interface SimbootAppRunner {
    public fun run(): SimbootContext
}

private class DefaultSimbootAppRunner(val context: CoreBootEntranceContext) : SimbootAppRunner {
    override fun run(): SimbootContext {
        return SimbootApp.run(context, *context.args)
    }
}