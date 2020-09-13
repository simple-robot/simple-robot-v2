/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     ResourceUtil.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.utils;

import love.forte.common.exception.ResourceException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * 资源文件读取工具.
 * <p>
 * 使用hutool的资源读取工具。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class ResourceUtil {

    /**
     * 获取一个配置文件的reader
     * @param name 路径
     * @return Reader for resource.
     */
    public static Reader getResourceUtf8Reader(String name) {
        return cn.hutool.core.io.resource.ResourceUtil.getResourceObj(name).getReader(StandardCharsets.UTF_8);
    }


    /**
     * 读取所有配置文件
     * @param name 资源路径。
     * @return Reader list for resources.
     */
    public static List<Reader> getResourcesUtf8Reader(String name) {
        final List<Reader> readers = new LinkedList<>();
        final List<URL> resources = cn.hutool.core.io.resource.ResourceUtil.getResources(name);
        URL nowRel = null;
        try {
            for (URL resource : resources) {
                nowRel = resource;
                final InputStream stream;
                stream = resource.openStream();
                readers.add(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
            }
            nowRel = null;
        } catch (IOException e) {
            // close others.
            for (Reader reader : readers) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            throw new ResourceException("cannot open stream for resource [" + nowRel + "]");
        }
        return readers;
    }


}
