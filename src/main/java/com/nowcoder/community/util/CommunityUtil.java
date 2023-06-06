package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;


import java.util.UUID;

public class CommunityUtil {
    private CommunityUtil() {
        throw new IllegalStateException("Utility class");
    }

    //生成随机字符串
    public static final String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    //MD5加密
    //hello->sjaoifhjoaijf
    //hello+slat->asjfaopfa
    public static final String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
