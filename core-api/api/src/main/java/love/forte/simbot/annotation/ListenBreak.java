/*
 *
 *  * Copyright (c) 2021. ForteScarlet All rights reserved.
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

package love.forte.simbot.annotation;

import love.forte.simbot.listener.ListenResult;

import java.lang.annotation.*;

/**
 * 当标注此注解在一个监听函数上的时候，此函数如果 **执行成功** ，则会阻断后续其他监听函数的执行。
 *
 * @see ListenResult#isBreak()
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
@Retention(RetentionPolicy.RUNTIME)    //注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE}) //接口、类、枚举、注解、方法
@Documented
public @interface ListenBreak {
}
