package com.guangke.forum.controller;

import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ActivationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping(path = "/register")
//    接收username,email,password
    public Map register(@RequestBody User user) {
//        System.out.println(user.getUsername());
//        System.out.println(user.getPassword());
//        System.out.println(user.getEmail());
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


    @GetMapping("/activation/{userId}/{activationCode}")
    public ModelAndView activate( @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode) {
        ActivationStatus status = userService.activate(userId, activationCode);
        ModelAndView modelAndView = new ModelAndView("activation.html");
        if (status == ActivationStatus.ACTIVATION_SUCCESS) {
            modelAndView.addObject("msg", "您的账号已激活成功,请登录");
        } else if (status == ActivationStatus.ACTIVATION_REPEAT) {
            modelAndView.addObject("msg", "该用户已经激活过了");
        } else {
            modelAndView.addObject("msg", "无效的激活码");
        }
        return modelAndView;
    }
}
