/*
 *
 * Copyright (C) 1999-2012 IFLYTEK Inc.All Rights Reserved.
 *
 * FileName：HttpClient4Util.java
 *
 * Description：
 *
 * History：
 * Version   Author      Date            Operation
 * 1.0    ljwang2   2016年8月3日下午5:02:43         Create
 */
package com.chatopera.cc.util;


import net.sf.json.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;

import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ljwang2
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class HttpClient4Util {

    /**
     * logger
     */
    public static Logger logger = LoggerFactory
            .getLogger(HttpClient4Util.class);

    /**
     * 最大连接数
     */
    private static final int MAX_TOTAL_CONNECTIONS = 2000;

    /**
     * 单个路由的最大连接数
     */
    private static final int MAX_ROUTE_CONNECTIONS = 2000;

    /**
     * 从连接池中取连接的超时时间
     */
    private static final int CONN_MANAGER_TIMEOUT = 1000;

    /**
     * 建立HTTP连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 10000;

    /**
     * 发送HTTP请求超时时间，一次请求最多等待的时间（读取数据超时），超过10秒，认为是无效请求
     */
    private static final int SOCKET_TIMEOUT = 60000;

    /**
     * 链接最多维持时间50分钟。（此时间为链接的生命周期的全部存活时间）;默认两分钟，压测下有效
     */
    private static final long CONN_TIME_TO_LIVE = 3000000L;

    /**
     * conn 空闲检验连接是否有效的时间
     */
    private static final int VALIDATE_AFTER_INACTIVITY = 2000;

    /**
     * 最大的空闲时间 20秒
     */
    private static final long MAX_IDLE_TIME = 20000L;

    /**
     * httpclient
     */
    private static HttpClient httpClient = null;

    /**
     * 设置日期格式
     */
    private static SimpleDateFormat df = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * 字符集
     */
    private static final String CHARSET = "UTF-8";


    static {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
            // 自定义的恢复策略
            @Override
            public boolean retryRequest(IOException exception,
                                        int executionCount, HttpContext context) {
                // 设置恢复策略，在Http请求发生异常时候将自动重试3次
                if (executionCount >= 1) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // Retry if the server dropped connection on us
                    return true;
                }
                HttpRequest request = (HttpRequest) context
                        .getAttribute(ExecutionContext.HTTP_REQUEST);
                boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
                if (!idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };

        RequestConfig.Builder rcBuilder = RequestConfig.custom();
        rcBuilder.setConnectionRequestTimeout(CONN_MANAGER_TIMEOUT);
        rcBuilder.setConnectTimeout(CONNECT_TIMEOUT);
        rcBuilder.setSocketTimeout(SOCKET_TIMEOUT);

        // 每次使用之前都去校验一下，是否可以继续使用
        rcBuilder.setStaleConnectionCheckEnabled(true);

        RequestConfig config = rcBuilder.build();

        builder.setDefaultRequestConfig(config);
        builder.setMaxConnPerRoute(MAX_ROUTE_CONNECTIONS);
        builder.setMaxConnTotal(MAX_TOTAL_CONNECTIONS);

        PoolingHttpClientConnectionManager connectManager = buildConnectionManager();

        builder.setConnectionManager(connectManager);

        // builder.setKeepAliveStrategy(keepAliveStrategy);
        // builder.setDefaultConnectionConfig(connectionConfig);
        builder.setRetryHandler(requestRetryHandler);

        // 链接最多维持时间。（此时间为链接的生命周期的全部存活时间）
        builder.setConnectionTimeToLive(CONN_TIME_TO_LIVE,
                TimeUnit.MILLISECONDS);
        builder.evictExpiredConnections();

        // 链接最多维持空闲时间。
        builder.evictIdleConnections(MAX_IDLE_TIME, TimeUnit.MILLISECONDS);

        httpClient = builder.build();
    }


    /**
     * @return PoolingHttpClientConnectionManager
     * @description
     * @author taoliu
     * @create 2015年11月3日下午6:54:06
     * @version 1.0
     */
    private static PoolingHttpClientConnectionManager buildConnectionManager() {
        LayeredConnectionSocketFactory sslSocketFactoryCopy = null;
        if (sslSocketFactoryCopy == null) {
            PublicSuffixMatcher publicSuffixMatcherCopy = PublicSuffixMatcherLoader
                    .getDefault();
            HostnameVerifier hostnameVerifierCopy = new DefaultHostnameVerifier(
                    publicSuffixMatcherCopy);
            sslSocketFactoryCopy = new SSLConnectionSocketFactory(
                    SSLContexts.createDefault(), hostnameVerifierCopy);
        }
        final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(
                RegistryBuilder
                        .<ConnectionSocketFactory>create()
                        .register("http",
                                PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactoryCopy).build(), null,
                null, null, CONN_TIME_TO_LIVE, TimeUnit.MILLISECONDS);
        poolingmgr.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        poolingmgr.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        poolingmgr.setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY);

        poolingmgr.closeExpiredConnections();
        poolingmgr.closeIdleConnections(10, TimeUnit.SECONDS);

        return poolingmgr;
    }


    /**
     * @param url     请求地址
     * @param charset 支付经济
     * @return String
     * @description
     * @author taoliu
     * @create 2015年11月3日下午6:54:28
     * @version 1.0
     */
    public static String doGet(String url, String charset) {
        String res = null;
        HttpGet get = new HttpGet(url);
        InputStream in = null;
        try {
            HttpResponse response = httpClient.execute(get);
            in = response.getEntity().getContent();
            if (response.getStatusLine().getStatusCode() < 400) {
                res = EntityUtils.toString(response.getEntity(), charset);
            } else {
                // 调用HttpGet的abort，这样就会直接中止这次连接，
                get.abort();
                logger.error("请求失败！返回code为： "
                        + response.getStatusLine().getStatusCode() + "”！Url:"
                        + url);
            }
        } catch (ConnectTimeoutException e) {
            get.abort();
            logger.error("发送请求连接建立超时，！Url:" + url, e);
        } catch (SocketTimeoutException e) {
            get.abort();
            logger.error("发送请求读取数据超时！Url:" + url, e);
        } catch (Exception e) {
            get.abort();
            logger.error("发送请求发生异常！Url:" + url, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return res;
    }

    public static String doPost(String url, Map<String, Object> searchParams) throws IOException {
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(JSONObject.fromObject(searchParams).toString(), "utf-8");
        requestEntity.setContentEncoding("UTF-8");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(requestEntity);
        String returnValue = httpClient.execute(httpPost, responseHandler);
        return returnValue;
    }

}
