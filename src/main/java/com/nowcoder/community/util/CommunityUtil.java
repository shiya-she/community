package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;


import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    private CommunityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * description:生成随机字符串
     * @return String
     */
    public static  String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * MD5加密
     * @param key 待转换的字符串
     * @return String
     */
    public static  String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 处理ajax需要返回一个json格式的字符串
     * @param code 标识类似与http的404 500
     * @param msg  提示信息
     * @param map  需要携带的map数据
     * @return String
     */
    public  static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if(map!=null){
            jsonObject.putAll(map);
        }
        return jsonObject.toString();
    }
    public  static String getJSONString(int code, String msg){
        return  getJSONString(code, msg,null);
    }
    public  static String getJSONString(int code){
        return  getJSONString(code, null,null);
    }


}
