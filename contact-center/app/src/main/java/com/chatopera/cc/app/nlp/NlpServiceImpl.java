package com.chatopera.cc.app.nlp;

import com.chatopera.cc.app.webservice.GetWebserviceInfo;
import com.chatopera.cc.util.PropertiesConfig;
import com.iflytek.mt_scylla.mt_scylla;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by zp on 2019/4/26.
 */
public class NlpServiceImpl {
    private static Logger logger = LoggerFactory.getLogger(NlpServiceImpl.class);

    public String nlpNew(String voiceResult, String iss_uid, String nlpAddress, String username, String org, String appid, int merroyNlpCout, mt_scylla miat) throws Exception {
        String resu = null;
        try {
            //初始化语义引
            resu = nlpNewTest(voiceResult, iss_uid, nlpAddress, username, org, appid, miat);
            if (resu == null) {
                logger.info("第一次，输出结果是：" + resu);
                return resu;
            } else {
                return resu;
            }

        } catch (Exception e) {
            throw e;
        } finally {
            // closeSemanticEngine(miat);
            logger.info("调用语义插件结束");
        }
    }

    /**
     * 调语义得到结果
     *
     * @param voiceResult
     * @param iss_uid
     * @param nlpAddress
     * @param miat
     * @return
     * @throws Exception
     */
    public String nlpNewTest(String voiceResult, String iss_uid, String nlpAddress, String username, String org, String appid, mt_scylla miat) throws Exception {
        int textlen = voiceResult.getBytes("UTF-8").length;
        String param = "svc=nlp,newsess=0,nbest=5,username=" + username + ",org=" + org + ",iss_uid=" + iss_uid + ","
                + "url=" + nlpAddress + ",appid=" + appid;
        logger.info(param);
        int[] errCode = new int[1];
        String resu = miat.SCYMTNLPEx(param, voiceResult, textlen, errCode,
                null);
        if (errCode[0] == 0) {
            logger.info("讯飞引擎语义结果为：" + resu);
        } else {
            logger.error("哎呀，出错了，错误码：" + errCode[0] + ".");
            resu = null;
        }
        return resu;
    }

    /**
     * 获取知识库答案
     *
     * @param voiceResult
     * @param newuid
     * @param nlpAddress
     * @param scoreThreshold
     * @param username
     * @param org
     * @param appid
     * @param miat
     * @return
     * @throws Exception
     */
    public String getResult(String voiceResult, String newuid, String nlpAddress, String scoreThreshold, String username, String org, String appid, mt_scylla miat) throws Exception {
        //调用讯飞引擎s
        int merroyNlpCout = 0;
        String ismResult = "";
        String ismContent = "";
        String resu = nlpNew(voiceResult, newuid, nlpAddress, username, org, appid, merroyNlpCout, miat);
        JSONObject result = JSONObject.fromObject(resu).getJSONObject("result");
        String resuId = null;
        resuId = result.getJSONObject("matchInfo")
                .getString("id");

        double score = result.getJSONObject("matchInfo").getDouble(
                "score");
        double scoreThresholdDouble = Double
                .valueOf(scoreThreshold);
        //答案的id 调用知识库的web接口
        if (score >= scoreThresholdDouble) {
            //匹配的得分大于等于0.6分，走标准问获取答案
            //调用讯飞接口获得问答答案的id
            ismResult = GetWebserviceInfo.getYanZhiAnswer(resuId);
            logger.info("获取知识库接口答案====" + ismResult);
            JSONObject ismResult1 = JSONObject.fromObject(ismResult);
            JSONObject ismResult2=ismResult1.getJSONObject("data");
            if (ismResult2.has("content")) {
                ismContent = ismResult2.getString("content");
            }
        }
        return ismContent;
    }

    /**
     * 获取知识库答案
     *
     * @param text
     * @return
     */
    public String getAnswer(String text) {
        mt_scylla miat = new mt_scylla();
        int kk = miat.SCYMTInitializeEx(null);
        logger.info("输出初始化结果kk：" + kk);
        String iss_uid = "";
        String answer = "";
        String nlpAddress = PropertiesConfig.getProperty("nlp.address");
        String scoreThreshold = PropertiesConfig.getProperty("nlp.score");
        String newUsername = PropertiesConfig.getProperty("nlp.newUsername");
        String org = PropertiesConfig.getProperty("nlp.org");
        String appid = PropertiesConfig.getProperty("nlp.appid");
        try {
            answer = getResult(text, iss_uid, nlpAddress, scoreThreshold, newUsername, org, appid, miat);
//            int ret = miat.SCYMTUninitializeEx(null);
//            if (ret == 0) {
//                logger.info("逆初始化成功");
//            } else {
//                logger.info("逆初始化失败，error code:" + ret);
//            }
        } catch (Exception e) {
            logger.error("调用语义引擎出错", e.getMessage());
            return null;
        }
        return answer;
    }
}
