package com.nowcoder.community.service.impl;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
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
    private final LoginTicketMapper loginTicketMapper;

    public UserServiceImpl(UserMapper userMapper, MailClient mailClient, LoginTicketMapper loginTicketMapper) {
        this.userMapper = userMapper;
        this.mailClient = mailClient;
        this.loginTicketMapper = loginTicketMapper;
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
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    @Override
    public boolean checkLoginStatus(String ticket) {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return loginTicket == null || loginTicket.getStatus() == 1 || loginTicket.getExpired().before(new Date());
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
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
        return map;
    }
    @Override
    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

}
