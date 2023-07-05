package com.nowcoder.community.service.impl;


import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final MailClient mailClient;

    private final RedisTemplate redisTemplate;


    @Override
    public User findUserById(int id) {
        User user = getCache(id);
        user = Optional.ofNullable(user)
                .orElse(initCache(id));
        return user;
    }

    @Override
    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        //验证密码
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已被注册！");
            return map;
        }
        if (!mailClient.checkMail(user)) {
            map.put("emailMsg", "邮箱不存在请检查你的邮箱！");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", CommunityConstants.RANDOM.nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        return map;
    }

    @Override
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return CommunityConstants.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return CommunityConstants.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstants.ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(User user, int expiredSeconds) {
        HashMap<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(user.getUsername()) && StringUtils.isBlank(user.getEmail())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        User user1 = null;
        //验证账号
        if (user.getUsername() != null) {
            user1 = userMapper.selectByName(user.getUsername());
        } else if (user.getEmail() != null) {
            user1 = userMapper.selectByEmail(user.getEmail());
        }
        if (user1 == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        if (user1.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }
        //验证密码
        String password = CommunityUtil.md5(user.getPassword() + user1.getSalt());
        if (!password.equals(user1.getPassword())) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user1.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        String redisKey = RedisKeyUtil.getTicket(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {

        String redisKey = RedisKeyUtil.getTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        Optional.ofNullable(loginTicket)
                .ifPresent(loginTicket1 -> loginTicket1.setStatus(1)
                );
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }


    @Override
    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicket(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    @Override
    public Map<String, Object> updatePassword(String oldPassword, String newPassword, User user) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "旧密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if (!StringUtils.equals(user.getPassword(), oldPassword)) {
            map.put("oldPasswordMsg", "旧密码错误!");
            return map;
        }
        if (StringUtils.equals(oldPassword, newPassword)) {
            map.put("newPasswordMsg", "新密码不能与旧密码相同");
            return map;
        }
        userMapper.updatePassword(user.getId(), newPassword);
        clearCache(user.getId());
        return map;
    }

    @Override
    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    @Override
    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public void forgetCode(int userId, String email, String code, int minutes) {
        String userForgetKey = RedisKeyUtil.getUserForgetKey(userId);
        mailClient.forgetMail(email, code, minutes);
        redisTemplate.opsForValue().set(userForgetKey, code, Duration.ofMinutes(minutes));
    }

    @Override
    public Map<String, Object> resetPassword(String email, String code, String password) {
        User user = findUserByEmail(email);
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            map.put("emailMsg", "email输入有误！");
            return map;
        }
        String userForgetKey = RedisKeyUtil.getUserForgetKey(user.getId());
        String code1 = (String) redisTemplate.opsForValue().get(userForgetKey);
        if (code1 == null) {
            map.put("emailMsg", "请重新获取验证码！");
            return map;
        }
        if (!code.equals(code1)) {
            map.put("codeMsg", "请输入正确的验证码");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        clearCache(user.getId());
        return map;
    }

    //1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);

        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.取不到时初始化数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    //3.数据变更时清除缓存
    public void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
