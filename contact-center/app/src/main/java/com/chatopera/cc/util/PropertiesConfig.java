package com.chatopera.cc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * 配置文件属性获取工具类
 *
 * @Author: zhuLong
 * @Date: 2019/8/1 14:01
 */
public class PropertiesConfig {

    private static Logger logger = LoggerFactory.getLogger(PropertiesConfig.class);

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static String getProperty(String key) {
        try {
            return new String(properties.getProperty(key).getBytes("iso-8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
