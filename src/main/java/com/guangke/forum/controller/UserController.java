package com.guangke.forum.controller;


import com.guangke.forum.pojo.User;

import com.guangke.forum.service.FollowService;
import com.guangke.forum.service.LikeService;
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
    private  String domain;

    @Value("${server.servlet.context-path}")
    private  String context;

    @Value("${forum.path.uploadImage}")
    private  String uploadPath;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    public Map<String,String> updatePassword(String oldPassword, String newPassword) {
        Map<String,String> res = new HashMap<>();
        //获取该请求的用户
        User user = hostHolder.get();
        if(user==null) {
            res.put("tokenErr", "1");
            return res;
        }
        if (StringUtils.isBlank(oldPassword)) {
            res.put("err","原密码不能为空");
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
            res.put("err","原密码不正确");
            return res;
        }
        //进行到这里时说明可以对数据库的密码修改
        newPassword = ForumUtils.md5(newPassword + salt);
        userService.updatePassword(user.getId(), newPassword);
        res.put("status","ok");
        return res;
    }
//    上传头像
    @PostMapping("/upload")
    public Map<String,String> upload(MultipartFile image) {
        Map<String,String> res = new HashMap<>();
        if(hostHolder.get()==null){
            res.put("err","尚未登录");
            return res;
        }
        //图片为空时，中断
        if (image == null) {
            res.put("err", "图片文件不能为空");
            return res;
        }
        String status = uploadImg(domain,context,uploadPath,image);
        if(!status.startsWith("http")){
            res.put("err",status);
            return res;
        }
        userService.updateHeaderUrl(hostHolder.get().getId(), status);
        res.put("status","ok");
        res.put("headerUrl",status);
        return res;
    }
//    如果文件格式错误，返回错误信息；否则返回文件的访问路径
    public static String uploadImg(String domain,String context,String uploadPath,MultipartFile image){
        //给图片生成随机名
        String fileName = image.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        //后缀为空时，中断
        if (!(suffix.equals(".jpg") || suffix.equals(".png") || suffix.equals(".jpeg"))) {
            return "文件格式不正确";
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
        return headerUrl;
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

    //个人详情页面
    @GetMapping(path = "/profile/detail/{userId}")
    public Map<String,Object> getProfilePage(@PathVariable("userId") int userId) {
        Map<String,Object> res = new HashMap<>();
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("当前用户不存在！");
        }
        //点赞数
        int likeCount = likeService.findUserLikeCount(userId);
        res.put("likeCount", likeCount);

        //关注数
        long followeeCount = followService.getFolloweeCount(userId, ENTITY_TYPE_USER);
        res.put("followeeCount", followeeCount);

        //粉丝数
        long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER, userId);
        res.put("followerCount", followerCount);

        //当前用户对该实体的关注状态
        boolean hasFollowed = false;
        //如果未登录则默认为false显示为 未关注,登录时查询是否有关注该实体
        if (hostHolder.get() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.get().getId(), ENTITY_TYPE_USER, userId);
        }
        res.put("hasFollowed", hasFollowed);
        res.put("user",user);
        return res;
    }
}
