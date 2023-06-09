package com.nowcoder.community.util;

import java.util.Random;

public class CommunityConstants {
    private CommunityConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 随机器
     */
    public static final Random RANDOM = new Random();
    /**
     * description:激活成功
     */
    public static final int ACTIVATION_SUCCESS = 0;
    /**
     * description:重复激活
     */
    public static final int ACTIVATION_REPEAT = 1;
    /**
     * description:错误的url
     */
    public static final int ACTIVATION_FAILURE = 2;

    /**
     * description:大写字母
     */
    public static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * description:小写字母
     */
    public static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    /**
     * description:数字
     */
    public static final String NUMBERS = "0123456789";
    /**
     * 默认状态的登录凭证的超时时间
     */
    public static final int DEFAULT_EXPIRED_SECONDS = 60 * 60 * 12;
    /**
     * 记住状态的登录凭证超时时间
     */
    public static final int REMEMBER_EXPIRED_SECONDS = 60 * 60 * 24 * 10;
    /**
     * 实体类型:帖子
     */

    public static final int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型:评论
     */
    public static final int ENTITY_TYPE_COMMENT = 2;
    /**
     * 实体类型:用户
     */
    public static final int ENTITY_TYPE_USER = 3;
    /**
     * 记住忘记密码验证码的生存时间单位minute
     */
    public static final int USER_FORGET = 10;
}
