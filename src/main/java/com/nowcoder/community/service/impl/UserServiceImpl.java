package com.nowcoder.community.service.impl;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final MailClient mailClient;



    public UserServiceImpl(UserMapper userMapper, MailClient mailClient) {
        this.userMapper = userMapper;
        this.mailClient = mailClient;

    }

    @Override
    public User findUserById(int id) {
        return userMapper.selectById(id);
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
        user.setHeaderUrl(String.format("http://image.newcoder.com/head/%dt.png", CommunityConstants.RANDOM.nextInt(1000)));
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
            return CommunityConstants.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstants.ACTIVATION_FAILURE;
        }
    }

}
