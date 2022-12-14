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

@file:JvmName("SimbotApps")

package love.forte.simbot.core

import cn.hutool.core.io.FileUtil
import kotlinx.coroutines.runBlocking
import love.forte.common.collections.concurrentQueueOf
import love.forte.common.configuration.Configuration
import love.forte.common.configuration.ConfigurationManagerRegistry
import love.forte.common.configuration.ConfigurationParserManager
import love.forte.common.configuration.impl.LinkedMapConfiguration
import love.forte.common.configuration.impl.MergedConfiguration
import love.forte.common.exception.ResourceException
import love.forte.common.ifOr
import love.forte.common.ioc.DependBeanFactory
import love.forte.common.ioc.DependCenter
import love.forte.common.ioc.InstanceBeanDepend
import love.forte.common.ioc.annotation.Beans
import love.forte.common.listAs
import love.forte.common.utils.ResourceUtil
import love.forte.common.utils.annotation.AnnotationUtil
import love.forte.common.utils.scanner.HutoolClassesScanner
import love.forte.common.utils.scanner.Scanner
import love.forte.simbot.*
import love.forte.simbot.annotation.SimbotApplication
import love.forte.simbot.bot.BotManager
import love.forte.simbot.constant.PriorityConstant
import love.forte.simbot.listener.MsgGetProcessor
import love.forte.simbot.utils.newInputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime


private const val RESOURCE_FILE = "file:"
private const val RESOURCE_CLASSPATH = "classpath:"
private const val RESOURCE_HTTP = "http"


/**
 * ????????? [SimbotApp.run] ???????????????????????????????????????????????????????????????????????????????????????
 */
public interface SimbotProcess {
    fun pre(config: Configuration)
    fun post(context: SimbotContext)
}

/**
 * ???????????????????????????????????????
 */
internal object NothingProcess : SimbotProcess {
    override fun pre(config: Configuration) {}
    override fun post(context: SimbotContext) {}
}


/**
 * simbot ???????????????????????????????????????
 */
public class SimbotContext
internal constructor(
    private val dependBeanFactory: DependBeanFactory,
    val botManager: BotManager,
    val environment: SimbotEnvironment,
    val msgProcessor: MsgGetProcessor,
    val configuration: Configuration,
    private val closeHandleList: List<SimbotContextClosedHandle> = emptyList(),
) : DependBeanFactory by dependBeanFactory, Closeable {
    private companion object : TypedCompLogger(SimbotContext::class.java)

    /**
     * Join wait for all tasks.
     */
    @JvmSynthetic
    suspend fun join() {
        SimbotApp.joinedTasks.toList().forEach { it() }
    }

    @JvmName("join")
    fun joinBlocking() = runBlocking {
        join()
    }


    override fun close() {
        // run doClosed list
        closeHandleList.forEach { handle ->
            kotlin.runCatching {
                logger.debug("Execute handle ${handle.handleName}")
                handle.simbotClose(this)
                logger.debug("Execute handle ${handle.handleName} finish.")
            }.getOrElse { e ->
                val handleLogger = if (handle is LogAble) handle.log else LoggerFactory.getLogger(handle::class.java)
                handleLogger.error("SimbotContext close handle '${handle.handleName}' execute failed!", e)
            }
        }
    }
}


/**
 * [SimbotContext] ??? [SimbotContext.close] ?????????????????????????????????
 *
 * ????????????????????????????????????????????????????????????
 *
 * ??????????????????????????????????????????????????????
 *
 * ????????? [SimbotContextClosedHandle] ????????? [SimbotContext] ?????????????????????????????????.
 *
 */
public interface SimbotContextClosedHandle {
    /**
     * ????????????????????????????????????????????????
     */

    val handleName: String get() = "SimbotContextClosedHandle-Default"

    /**
     * ??????close?????????
     *
     * @throws Exception ????????????????????????, ????????????????????????
     */
    @Throws(Exception::class)
    fun simbotClose(context: SimbotContext)
}


internal val simbotAppLogger: Logger = LoggerFactory.getLogger(SimbotApp::class.java)


internal fun coreVersion(simbotAppLoader: ClassLoader): String? {
    return runCatching {
        val path = "META-INF/maven/love.forte.simple-robot/core/pom.properties"
        val pomProperties: Properties =
            (simbotAppLoader.getResource("/$path")
                ?: simbotAppLoader.getResource(path))?.newInputStream()
                .use { input ->
                    Properties().apply { load(input) }
                }

        pomProperties.getProperty("version")
    }.getOrNull()
}


/**
 * simbot app ????????????
 *
 * ??????????????????????????????????????????????????????????????????????????????????????????
 *
 * ?????????????????????????????????????????????????????????
 *
 * ??????????????????????????????????????????
 *
 * ... //
 *
 */
public open class SimbotApp
protected constructor(
    /** ???????????? scanPackage ???????????????????????????????????????????????????????????? */
    defaultScanPackage: Array<String>,
    private val loader: ClassLoader,
    private val parentDependBeanFactory: DependBeanFactory?,
    resourceData: List<SimbotResourceData>,
    private val process: SimbotProcess,
    // ????????????????????????????????????????????????
    private val defaultConfiguration: Configuration?,
    args: List<String>,
    internal val logger: Logger = simbotAppLogger,
) {


    private var showLogo: Boolean = runCatching {
        defaultConfiguration?.getConfig(Logo.ENABLE_KEY)?.boolean
    }.getOrNull() ?: true
    private var showTips: Boolean = runCatching {
        defaultConfiguration?.getConfig(Tips.ENABLE_KEY)?.boolean
    }.getOrNull() ?: true

    protected open val defaultScanPackageArray: Array<String> = defaultScanPackage

    /** ????????????????????????????????????????????????????????????????????? [initDependCenterWithRunData] ????????????????????? */
    protected open val simbotResourceEnvironment: SimbotResourceEnvironment =
        SimbotResourceEnvironmentImpl(resourceData)

    /** ??????????????????????????? [initDependCenterWithRunData] ????????????????????? */
    protected open val simbotArgsEnvironment: SimbotArgsEnvironment = SimbotArgsEnvironmentImpl(args.toTypedArray())

    /**
     * ???????????????????????????????????????
     */
    protected open val configurationManager: ConfigurationParserManager = ConfigurationManagerRegistry.defaultManager()

    // /**
    //  * ???????????????
    //  */
    // protected open val appConfiguration = SimbotAppConfiguration()

    /**
     * ??????????????? ????????? [HutoolClassesScanner]???
     * ???????????????????????????????????????
     */
    protected open val scanner: Scanner<String, Class<*>> get() = HutoolClassesScanner()

    /**
     * ?????????????????????
     */
    private lateinit var dependCenter: DependCenter


    /**
     * ???????????????
     */
    @Synchronized
    internal fun run(): SimbotContext {
        // show logo.
        if (showLogo) {
            Logo.show(coreVersion(javaClass.classLoader))
        }

        val tips = Tips()

        runCatching {
            defaultConfiguration?.getConfig(Tips.RESOURCE_CONF_KEY)?.getObject(TipOnline::class.java)?.let {
                tips.TIP_ONLINE_PATH = it
            }
        }

        if (showTips) {
            tips.show()
        }


        // load configs.
        val config: Configuration = loadResourcesToConfiguration().let {
            if (defaultConfiguration != null) MergedConfiguration.merged(defaultConfiguration, it)
            else it
        }
        // process pre config.
        process.pre(config)

        // load all auto config.
        val autoConfigures = initDependCenterWithAutoConfigures(config)
        // init with run data.
        initDependCenterWithRunData()

        // val hi = config.getConfig("simbot.core.init")?.string ?: "hello!!!!!!!!!!!!!!!~"
        // println(hi)

        // merge depend center config.
        // dependCenter.mergeConfig { c -> MergedConfiguration.merged(c, config) }

        // scan and inject.
        scanPackagesAndInject(dependCenter.configuration, autoConfigures)

        // init depend.
        initDependCenter()

        // return.
        return createSimbotContext(config).also {
            // post
            try {
                process.post(it)
            } catch (e: Exception) {
                logger.error("SimbotProcess.post failed.", e)
            }
        }
    }


    /**
     * ???????????????????????????????????????auto config???, ???????????????????????????
     */
    private fun initDependCenterWithAutoConfigures(config: Configuration): AutoConfiguresData {
        // ????????????????????????????????????????????????
        val autoConfigures = autoConfigures(loader, logger)

        dependCenter = DependCenter(parent = parentDependBeanFactory, configuration = config)

        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            dependCenter.close()
        })

        // ??????????????????????????????
        autoConfigures.classes.forEach {
            dependCenter.register(it)
        }

        return autoConfigures
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private fun initDependCenterWithRunData() {
        // depend center ????????????
        dependCenter.registerInstance("dependCenter", dependCenter)
        // ??????????????????:
        // simbotResourceEnvironment: SimbotResourceEnvironment
        // simbotArgsEnvironment: SimbotArgsEnvironment
        dependCenter.registerInstance("simbotResourceEnvironment", simbotResourceEnvironment)
        dependCenter.registerInstance("simbotArgsEnvironment", simbotArgsEnvironment)
    }

    /**
     * ??????????????????????????????????????????????????? [Configuration] ?????????
     */
    private fun loadResourcesToConfiguration(): Configuration {
        val confReaderManager = configurationManager

        val activeResources = simbotResourceEnvironment.resourceDataList.filter {
            val commands = it.commands
            commands.isEmpty() || commands.all { c -> simbotArgsEnvironment.contains(c) }
        }

        logger.info("Active resources: ${activeResources.map { it.resource }}")

        // ??????????????????????????????
        return activeResources.mapNotNull { resourceData ->
            val resourceName = resourceData.resource

            // get reader.
            val resourceReader: Reader? = runCatching {

                when {
                    resourceName.startsWith(RESOURCE_FILE) -> {
                        // starts with 'file', try to get Reader by file
                        FileUtil.getUtf8Reader(resourceName.substring(RESOURCE_FILE.length))
                    }
                    resourceName.startsWith(RESOURCE_CLASSPATH) -> {
                        ResourceUtil.getResourceUtf8Reader(resourceName.substring(RESOURCE_CLASSPATH.length))
                    }
                    resourceName.startsWith(RESOURCE_HTTP) -> {
                        URL(resourceName).connection { "Online resource connection failed. $it" }
                    }
                    else -> {
                        // try file first.
                        val file = File(resourceName)
                        if (file.exists()) {
                            // file exist
                            FileUtil.getUtf8Reader(file)
                        } else {
                            ResourceUtil.getResourceUtf8Reader(resourceName)
                        }
                    }
                }

            }.getOrElse { e ->
                if (resourceData.orIgnore) {
                    null
                } else {
                    throw ResourceException("Unable to read resource: $resourceName", e)
                }
            }

            // parse to configuration.
            resourceReader?.use { reader ->
                logger.debugf("resource [{}] loaded.", resourceName)
                val type: String = resourceData.type
                confReaderManager.parse(type, reader)
            }
        }.reduceOrNull { c1, c2 ->
            MergedConfiguration.merged(c1, c2)
        } ?: LinkedMapConfiguration()
    }


    /**
     * ??????????????????
     */
    private fun scanPackagesAndInject(config: Configuration, autoConfigure: AutoConfiguresData) {
        val ignored: Set<Class<*>> = autoConfigure.classes

        // scanPackage.
        val scanPackages = config.getConfig(SCAN_PACKAGES_KEY)?.getObject(Array<String>::class.java)?.asList()
            ?: (defaultScanPackageArray + autoConfigure.packages).distinct()

        val scanner = this.scanner

        scanPackages.forEach {
            scanner.scan(it) { c ->
                c !in ignored &&
                        AnnotationUtil.containsAnnotation(c, Beans::class.java)
            }
            logger.debug("package scan: {}", it)
        }

        val collection = scanner.collection
        //     .toMutableSet().apply {
        //          // remove all ignored.
        //          removeAll(ignored)
        //      }

        // inject classes.
        dependCenter.inject(types = collection.toTypedArray())
        // register simbotPackageScanEnvironment.

        dependCenter.registerInstance(
            "simbotPackageScanEnvironment",
            SimbotPackageScanEnvironmentImpl(scanPackages.toTypedArray())
        )


    }


    /**
     * ????????? depend center.
     */
    private fun initDependCenter() {
        dependCenter.init()
    }


    /**
     * ???????????? [SimbotContext] ??????????????????
     */
    private fun createSimbotContext(configuration: Configuration): SimbotContext {
        // ?????? botManager.
        val botManager = dependCenter[BotManager::class.java]
        val environment = dependCenter[SimbotEnvironment::class.java]
        val msgGetProcessor = dependCenter[MsgGetProcessor::class.java]

        // ?????????????????????????????????
        val handles: List<SimbotContextClosedHandle> =
            dependCenter.getListByType(SimbotContextClosedHandle::class.java).toList()

        return SimbotContext(dependCenter, botManager, environment, msgGetProcessor, configuration, handles)
    }


    /**
     * companion for static run.
     */
    companion object Run {

        internal const val SCAN_PACKAGES_KEY = "simbot.core.scan-package"
        internal val joinedTasks = concurrentQueueOf<suspend () -> Unit>()

        @JvmSynthetic
        fun onJoined(task: suspend () -> Unit) {
            joinedTasks.add(task)
        }
        /**
         * ?????????????????????class?????????
         */
        @JvmStatic
        @JvmOverloads
        public fun run(
            appType: Class<*>,
            loader: ClassLoader = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader(),
            parentDependBeanFactory: DependBeanFactory? = null,
            defaultConfiguration: Configuration? = null,
            vararg args: String,
        ): SimbotContext {

            // ???????????????????????????appType????????????
            val resourceData: List<SimbotResourceData> =
                AnnotationUtil.getAnnotation(appType, SimbotApplication::class.java)?.value
                    ?.map {
                        it.toData()
                    }
                    ?: throw IllegalArgumentException("There is no resource data info or SimbotApplication annotation.")

            // ?????????????????????
            val process: SimbotProcess = if (SimbotProcess::class.java.isAssignableFrom(appType)) {
                kotlin.runCatching { appType.newInstance() as SimbotProcess }.getOrElse { e ->
                    throw IllegalStateException("$appType cannot be SimbotProcess instance: ${e.localizedMessage}", e)
                }
            } else NothingProcess

            // ???????????????????????????????????????
            val defPackage: String = appType.`package`.name

            // run and return.
            return SimbotApp(
                arrayOf(defPackage),
                loader,
                parentDependBeanFactory,
                resourceData,
                process,
                defaultConfiguration,
                args.asList(),
                LoggerFactory.getLogger(appType)
            ).run()
        }


        /**
         * ???????????????????????????????????????
         */
        @JvmStatic
        @JvmOverloads
        public fun run(
            app: Any,
            loader: ClassLoader = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader(),
            parentDependBeanFactory: DependBeanFactory? = null,
            defaultConfiguration: Configuration? = null,
            vararg args: String,
        ): SimbotContext {

            // ???????????????????????????app?????????????????????????????????
            @Suppress("UNCHECKED_CAST")
            val resourceData: List<SimbotResourceData> = when {
                /** ?????????class??????????????????????????? */
                app is Class<*> -> return run(app, loader, parentDependBeanFactory, defaultConfiguration, *args)
                app is SimbotResourceData -> listOf(app)
                app is List<*> && listAs<SimbotResourceData, Any?>(app) != null -> app as List<SimbotResourceData>
                else -> {
                    AnnotationUtil.getAnnotation(app::class.java, SimbotApplication::class.java)?.value
                        ?.map {
                            it.toData()
                        } ?: throw IllegalArgumentException("There is no resource data info.")
                }
            }

            // ?????????????????????
            val process: SimbotProcess = ifOr<SimbotProcess>(app) { NothingProcess }

            val defPackage: String = app::class.java.`package`.name

            // run and return.
            return SimbotApp(
                arrayOf(defPackage),
                loader,
                parentDependBeanFactory,
                resourceData,
                process,
                defaultConfiguration,
                args.asList(),
                LoggerFactory.getLogger(app::class.java)
            ).run()
        }
    }
}


/**
 * ???????????????????????????
 */
internal fun <T> DependCenter.registerInstance(name: String, instance: T) {
    this.register(InstanceBeanDepend(name, PriorityConstant.CORE_TENTH, instance = instance))
}


// logo. logo? logo!
private object Logo {
    private const val DEF_LOGO = """
     _           _           _   
    (_)         | |         | |  
 ___ _ _ __ ___ | |__   ___ | |_ 
/ __| | '_ ` _ \| '_ \ / _ \| __|
\__ \ | | | | | | |_) | (_) | |_ 
|___/_|_| |_| |_|_.__/ \___/ \__|
                  @ForteScarlet |"""
    internal const val ENABLE_KEY = "simbot.core.logo.enable"
    private const val LOGO_PATH: String = "META-INF/simbot/logo"
    val logo: String = runCatching {
        val randomNum = (1..30).random()

        (
                javaClass.classLoader.getResource("$LOGO_PATH/$randomNum.simbLogo")
                    ?: javaClass.classLoader.getResource("$LOGO_PATH/def.simbLogo")
                ).readText(Charsets.UTF_8)
        // ResourcesScanner(javaClass.classLoader).scan(LOGO_PATH) { it.toASCIIString().endsWith("simbLogo") }
        //     .collection.randomOrNull()
        //     ?.toURL()?.readText(Charsets.UTF_8)
        //     ?: return@runCatching DEF_LOGO
    }.getOrElse { e ->
        simbotAppLogger.info("Logo file load failed: ${e.localizedMessage}", e)
        // simbotAppLogger.trace("Logo load failed: ${e.localizedMessage}", e)
        DEF_LOGO
    }


}

private fun Logo.show(version: String?, print: PrintStream = System.out) {
    print.println(logo.trimEnd())
    version?.let { v ->
        val lastLength = logo.lines().last().length
        val versionShow = "v$v"
        val versionShowLength = versionShow.length
        val spaceLength = (lastLength - versionShowLength).takeIf { it > 0 } ?: 0
        repeat(spaceLength) {
            print.print(' ')
        }
        print.println(versionShow)
    }
    print.println()
}

private class DisableTips : NullPointerException("Disable online tips.")


// tips! Do you know?
@Suppress("PropertyName")
private class Tips {

    private val logger: Logger = LoggerFactory.getLogger("love.forte.simbot.tips")

    companion object {
        internal const val RESOURCE_CONF_KEY = "simbot.core.tips.resource"
        internal const val ENABLE_KEY = "simbot.core.tips.enable"
        // internal val TEMP_PATH = F
    }


    private inline val TIP_PATH: String
        get() = "META-INF" + File.separator + "simbot" + File.separator + "simbTip.tips"

    internal var TIP_ONLINE_PATH: TipOnline? = null
        get() {
            // if (field != null) {
            //     return field
            // }
            return field ?: when (val resource = System.getProperty(RESOURCE_CONF_KEY)) {
                "gitee", "GITEE", null -> TipOnline.GITEE
                "github", "GITHUB" -> TipOnline.GITHUB
                else -> {
                    logger.warn("Unknown tips resource: {}, used Gitee resource.", resource)
                    TipOnline.GITEE
                }
            }
        }

    @OptIn(ExperimentalTime::class)
    private inline val localPath: Pair<Boolean, Path>
        get() {
            val local = Path(System.getProperty("user.home")) / ".simbot" / "tips"

            val exists = local.exists()

            if (!exists) {
                local.parent.createDirectories()
                local.createFile()
            }

            val lastMod = local.getLastModifiedTime().toMillis()
            if ((System.currentTimeMillis() - lastMod).milliseconds > 7.days) {
                local.deleteIfExists()
            }

            return exists to local
        }


    val randomTip: String?
        get() {
            val (exist, local) = kotlin.runCatching { localPath }.getOrDefault(false to null)
            return runCatching {
                fun readOnline(): Reader {
                    val url = TIP_ONLINE_PATH?.url ?: throw DisableTips()
                    logger.trace("Tips online resource {}, url: {}", TIP_ONLINE_PATH, url)
                    return URL(url).connection { "Online tips connection failed. $it" }
                }
                if (exist) {
                    kotlin.runCatching {
                        local?.reader(Charsets.UTF_8)
                    }.getOrNull() ?: run {
                        readOnline()
                    }
                } else {
                    readOnline()
                }
                // find local cache

            }.getOrElse { e ->
                if (e !is DisableTips) {
                    logger.debugEf("Read online tips failed: {}", e, e.localizedMessage)
                }
                runCatching {
                    ResourceUtil.getResourceUtf8Reader(TIP_PATH)
                }.getOrNull()
            }?.useLines {
                val list = it.filter { s -> s.isNotBlank() }.toList()
                kotlin.runCatching {
                    local?.writeLines(list)
                }
                return list.randomOrNull()
            }
        }


}


internal enum class TipOnline(val url: String) {
    GITHUB("https://raw.githubusercontent.com/ForteScarlet/simpler-robot/dev/tips/tips.tips"),
    GITEE("https://gitee.com/ForteScarlet/simpler-robot/raw/dev/tips/tips.tips"),
}


private fun Tips.show(print: PrintStream = System.out) {
    randomTip?.run {
        print.println("Tips: $this")
        print.println()
    }
}


private inline fun URL.connection(
    readTimeout: Int = 5000, connectTimeout: Int = 5000,
    onError: (errorStreamReaderText: String) -> String,
): Reader {
    // ????????????
    return (this.openConnection() as HttpURLConnection).run {
        this.readTimeout = readTimeout
        this.connectTimeout = connectTimeout
        connect()
        takeIf { responseCode < 300 }
            ?: throw IOException(onError(errorStream.reader().use { it.readText() }))
    }.inputStream.reader(Charsets.UTF_8)
}





