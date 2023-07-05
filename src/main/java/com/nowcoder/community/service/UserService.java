package com.nowcoder.community.service;


import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;


import java.util.Map;

public interface UserService {


    User findUserById(int id);

    Map<String, Object> register(User user);

    int activation(int userId, String code);


    Map<String, Object> login(User user, int expiredSeconds);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);


    int updateHeader(int userId, String headerUrl);


    Map<String, Object> updatePassword(String oldPassword, String newPassword, User user);


    User findUserByName(String name);

    User findUserByEmail(String email);


    void forgetCode(int userId, String email, String code, int minutes);

    Map<String,Object> resetPassword(String email, String code, String password);
}
