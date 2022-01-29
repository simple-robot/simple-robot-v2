<div align="center">
    <img src=".simbot/logo.png" alt="logo" style="width:230px; height:230px; border-radius:50%; " />
    <h2>
        - simply-robot -
    </h2>
    <span>
        <a href="https://github.com/ForteScarlet/simpler-robot" target="_blank">github</a>
    </span> 
    &nbsp;&nbsp; | &nbsp;&nbsp;
    <span>
        <a href="https://gitee.com/ForteScarlet/simpler-robot" target="_blank">gitee</a>
    </span> <br />
    <small> &gt; 感谢 <a href="https://github.com/ForteScarlet/CatCode" target="_blank">CatCode</a> 开发团队成员制作的simbot logo &lt; </small>
    <br>
    <small> &gt; 走过路过，不要忘记点亮一颗⭐喔~ &lt; </small> 
    <br>
   <a href="https://github.com/ForteScarlet/simpler-robot/releases/latest"><img alt="release" src="https://img.shields.io/github/v/release/ForteScarlet/simpler-robot" /></a>
<a href="https://repo1.maven.org/maven2/love/forte/simbot/simbot-api/" target="_blank">
  <img alt="release" src="https://img.shields.io/maven-central/v/love.forte.simbot/simbot-api" /></a>
<a href="https://www.yuque.com/simpler-robot/simpler-robot-doc" target="_blank">
  <img alt="doc" src="https://img.shields.io/badge/doc-yuque-brightgreen" /></a>
   <hr>
   <img alt="stars" src="https://img.shields.io/github/stars/ForteScarlet/simpler-robot" />
   <img alt="forks" src="https://img.shields.io/github/forks/ForteScarlet/simpler-robot" />
   <img alt="watchers" src="https://img.shields.io/github/watchers/ForteScarlet/simpler-robot" />
   <img alt="repo size" src="https://img.shields.io/github/repo-size/ForteScarlet/simpler-robot" />
   <img alt="lines" src="https://img.shields.io/tokei/lines/github/ForteScarlet/simpler-robot" />
   <img alt="issues" src="https://img.shields.io/github/issues-closed/ForteScarlet/simpler-robot?color=green" />
   <img alt="last commit" src="https://img.shields.io/github/last-commit/ForteScarlet/simpler-robot" />
   <a href="./LICENSE"><img alt="license" src="https://img.shields.io/github/license/ForteScarlet/simpler-robot" /></a>
    </div>

## 简介

这是一个通用机器人开发框架，是simple-robot的3.x版本(下文简称`simbot3`)。

`simbot` 是一个JVM平台的通用机器人开发框架，基于simbot核心API并对接开发不同平台的机器人应用，你可以使用相同的代码风格来开发不同平台的机器人。

它提供了丰富的api接口与各种模块以支持机器人开发者与组件开发者使用，对于机器人开发者，你可以通过功能丰富的注解来实现各种较为复杂的事件匹配逻辑。对于组件开发者，你拥有很高的可选择性与灵活性来针对一个平台进行对接。

simbot3相比较于simbot2时代，其(再一次的)完全重构了整体架构，使用全面异步的api提供更加高效更加流畅的使用体验。

<br>

simbot3中，simbot-core与simbot-boot(首先提醒，这里的boot指的不是springboot)之间的使用方式上会有较大的区别：

在simbot-core上，你的使用方式会更加"原生"，其允许你在更加复杂的代码中拥有更强的控制能力与灵活度。这更适合较为小型的系统或者需要更加灵活控制代码的应用。

而在simbot-boot(simboot)上，则提供了更多源于而优于simbot前代的注解开发与自动扫描机制，可以更快速高效的开发你的应用。


<br>
<br>

目前simbot3仍然处于**前期阶段**，如果你想参考simbot2, 可以参考分支: [v2-dev](https://github.com/ForteScarlet/simpler-robot/tree/v2-dev)

## 文档

simbot3的文档与simbot2的文档在一起，都在 [语雀文档](https://www.yuque.com/simpler-robot/simpler-robot-doc)
中。但是这次simbot3中的源码注释相比以前更为丰富，因此我建议对api相关的内容优先查阅代码中的文档注释。

## 组件

在simbot3相关的系列组件中，大部分需要依赖第三方库（也有可能是由simbot团队实现的）的组件，基本上都会使用独立的仓库进行管理，
并且会尽量遵循simbot3的 [命名概述](https://www.yuque.com/simpler-robot/simpler-robot-doc/yqlxig) 中所约定的规则。
<br>
simbot3目前已经实现的组件以及计划中的组件会列举于此，且不定期更新：

腾讯频道组件：<https://github.com/simple-robot/simbot-component-tencent-guild>

Mirai组件：<https://github.com/simple-robot/simbot-component-mirai>


## 走马观花
#### 事件监听
> 下述以 simbot-boot模块中的注解监听形式为例
```kotlin
@Listener
suspend fun GroupMessageEvent.listener() {
    println("事件来源群: ${group().name}")
    replyIfSupport { "你好！" }
}
```

```kotlin
@Filter("你好")
@Listener
suspend fun FriendMessageEvent.listener() {
    friend().send("你也好")
}
```

#### 对象获取
```kotlin
suspend fun GuildMessageEvent.listener() {
    // 频道的所有子频道
    val channels: Flow<Channel> = children()
    // bot的所有好友
    val friends: Flow<Friend> = bot.friends()
    // 获取指定群对象
    val group = bot.group(114514.ID)

    val groupId = group.id
    val groupName = group.name
    val groupIcon = group.icon
}
```

更多示例代码可以参考[3.x文档](https://www.yuque.com/simpler-robot/simpler-robot-doc/mudleb)中的《走马观花》相关内容.

文档持续优化中...
