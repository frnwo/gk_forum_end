package com.guangke.forum.controller;


import com.guangke.forum.pojo.User;

import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController implements ForumConstants {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String context;

    @Value("${forum.path.uploadImage}")
    private String uploadPath;

    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    public Map<String,String> updatePassword(String oldPassword, String newPassword) {
        Map<String,String> res = new HashMap<>();
        //获取该请求的用户
        User user = hostHolder.get();
        if(user==null) {
            res.put("err", "您无权访问该接口");
            return res;
        }
        if (StringUtils.isBlank(oldPassword)) {
            res.put("err","旧密码不能为空");
            return res;
        }
        if (StringUtils.isBlank(newPassword)) {
            res.put("err","新密码不能为空");
            return res;
        }
        /**
         *验证旧密码与当前用户的密码
         */
        String password = user.getPassword();
        String salt = user.getSalt();
        //对提交的旧密码加密
        oldPassword = ForumUtils.md5(oldPassword + salt);
        if (!oldPassword.equals(password)) {
            res.put("err","旧密码不正确");
            return res;
        }
        //进行到这里时说明可以对数据库的密码修改
        newPassword = ForumUtils.md5(newPassword + salt);
        userService.updatePassword(user.getId(), newPassword);
        res.put("status","ok");
        return res;
    }
    //上传头像
    @PostMapping("/upload")
    public Map<String,String> upload(MultipartFile image) {

        Map<String,String> res = new HashMap<>();
        //图片为空时，中断
        if (image == null) {
            res.put("err", "图片文件不能为空");
            return res;
        }
        //给图片生成随机名
        String fileName = image.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        //后缀为空时，中断
        if (!(suffix.equals(".jpg") || suffix.equals(".png") || suffix.equals(".jpeg"))) {
            res.put("err", "文件格式不正确");
            return res;
        }
        fileName = ForumUtils.generateUUID() + suffix;

        //将上传图片复制到本地路径
        File file = new File(uploadPath + "/" + fileName);
        try {
            image.transferTo(file);
        } catch (IOException e) {
            logger.error("图片上传失败");
            throw new RuntimeException("图片上传失败，服务器发生错误: " + e.getMessage());
        }

        /**
         修改用户信息中的图片路径headerUrl : http://localhost:8080/forum/user/profile/xxx.jpg
         */
        String headerUrl = domain + context + "/user/profile/" + fileName;
        userService.updateHeaderUrl(hostHolder.get().getId(), headerUrl);
        res.put("status","ok");
        return res;
    }
    /**
     * 响应用户头像图片
     */
    @GetMapping("/profile/{fileName}")
    public void getProfile(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        String dest = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);
        response.setContentType("image/" + suffix);
        try (
                //获取响应流
                OutputStream os = response.getOutputStream();
                //读取本地文件
                FileInputStream fis = new FileInputStream(dest)
        ) {
            byte[] buffer = new byte[1024];
            int b = -1;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (Exception e) {
            logger.error("读取/响应文件失败:" + e.getMessage());
        }
    }
}
