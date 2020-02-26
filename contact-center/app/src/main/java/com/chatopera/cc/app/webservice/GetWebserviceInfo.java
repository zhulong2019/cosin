package com.chatopera.cc.app.webservice;


import com.chatopera.cc.util.HttpClient4Util;
import com.chatopera.cc.util.WebServiceConfigUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * web接口统一调用工具类
 */
public class GetWebserviceInfo {

    // 知识库接口域名
    private static String ism_domain_name = WebServiceConfigUtils.getProperty("ISM_DOMAIN_NAME");

    // 73知识库接口域名
    private static String yanzhi_ism_domain_name = WebServiceConfigUtils.getProperty("yanzhi_ism_domain_name");


    /**
     * 获取知识库接口答案
     *
     * @param id
     * @return
     */
    public static String getAnswer(String id) {
        String url = ism_domain_name + WebServiceConfigUtils.getProperty("ISMWEBAPI_ADDRESS");
        return HttpClient4Util.doGet(url + id, "UTF-8");
    }

    /**
     * 获取73言知知识库接口答案
     *
     * @param id
     * @return
     */
    public static String getYanZhiAnswer(String id) throws Exception{
        String url = yanzhi_ism_domain_name + WebServiceConfigUtils.getProperty("YANZHI_ISMWEBAPI_ADDRESS");
        Map map=new HashMap<>();
        map.put("id",id);
        return HttpClient4Util.doPost(url,map);
    }
}

