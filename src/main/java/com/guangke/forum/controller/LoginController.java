package com.guangke.forum.controller;

import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping(path = "/register")
    //接收username,email,password
    public Map register(User user) {
        //model里面实例化了一个user,user里面是浏览器提交的表单
        Map<String, Object> map = userService.register(user);
        Map<String,Object> res = new HashMap<>();
        if (map.isEmpty()) {
            res.put("status","success");
            return res;
        } else {
            res.put("usernameMsg",map.get("usernameMsg"));
            res.put("passwordMsg",map.get("passwordMsg"));
            res.put("emailMsg"   ,map.get("emailMsg"));
            return res;
        }
    }

}
