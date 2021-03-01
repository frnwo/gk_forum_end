package com.guangke.forum.controller;

import com.google.code.kaptcha.Producer;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ActivationStatus;
import com.guangke.forum.util.ForumConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController implements ForumConstants {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

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

    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session
        session.setAttribute("kaptcha",text);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try{
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        }catch (IOException e){
            logger.error("响应验证码:"+e.getMessage());
        }
    }
    @PostMapping("/login")
    public Map<String,Object> login(String username, String password, String code,
                                    HttpSession session,HttpServletResponse response,
                                    boolean rememberme) {
        System.out.println(username);
        System.out.println(password);
        System.out.println(code);
        System.out.println(rememberme);

        Map<String,Object> res = new HashMap<>();
        String kaptcha = (String)session.getAttribute("kaptcha");
        System.out.println(kaptcha);
        //检查验证码
         if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)) {
            res.put("code", "验证码不正确");
            return res;
         }
        int ticketSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, ticketSeconds);
        //如果map中没有ticket键，则登录出了问题
        if (!map.containsKey("ticket")) {
            res.put("usernameMsg", map.get("usernameMsg"));
            res.put("passwordMsg", map.get("passwordMsg"));
            return res;
        } else {
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setMaxAge(ticketSeconds);
            cookie.setPath(contextPath);
            response.addCookie(cookie);
            res.put("status","ok");
            return res;
        }

    }
}
