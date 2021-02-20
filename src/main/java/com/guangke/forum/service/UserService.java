package com.guangke.forum.service;

import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.User;
import com.guangke.forum.util.ActivationStatus;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${forum.path.domain}")
    private String domain;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空");
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
        User u = userMapper.selectByUsername(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "用户已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已被注册");
            return map;
        }

        user.setCreateTime(new Date());
        //普通用户
        user.setType(0);
        //未激活
        user.setStatus(0);
        user.setSalt(ForumUtils.generateUUID().substring(0, 5));
        user.setPassword(ForumUtils.md5(user.getPassword() + user.getSalt()));
        user.setActivationCode(ForumUtils.generateUUID());
        String headerUrl = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        user.setHeaderUrl(headerUrl);
        userMapper.insertUser(user);
        //激活邮件
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        String content = "<div>" +
                "<p><b>"+user.getEmail()+"</b>, 您好!</p>"+
                "<p>您正在注册广科校园WebAPP,请点击<a href='"+url+"'>此链接</a>激活您的账号</p>"+
                "</div>";
        mailClient.sendMail(user.getEmail(), "广科论坛webApp激活账号", content);
        return map;
    }

    public ActivationStatus activate(int userId, String activationCode) {
        User u = userMapper.selectById(userId);
        if (u.getStatus() == 1) {
            return ActivationStatus.ACTIVATION_REPEAT;
        } else if (u.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            return ActivationStatus.ACTIVATION_SUCCESS;
        } else {
            //可能是防止激活没有这个id的用户或者伪造激活码
            return ActivationStatus.ACTIVATION_FAILURE;
        }
    }

}
