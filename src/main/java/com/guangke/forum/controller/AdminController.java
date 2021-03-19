package com.guangke.forum.controller;

import com.guangke.forum.pojo.Carousel;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.CarouselService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController implements ForumConstants {
    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${forum.path.domain}")
    private  String domain;

    @Value("${server.servlet.context-path}")
    private  String context;

    @Value("${forum.path.uploadImage}")
    private  String uploadPath;

    @Autowired
    private CarouselService carouselService;

    @GetMapping("/searchUser")
    public Map<String,Object> searchUser(String username){
        Map<String,Object> res = new HashMap<>();
        User u = hostHolder.get();
        //如果没有登录或者该用户是普通用户
        if(u == null || u.getType() == 0){
            res.put("tokenErr","1");
            return res;
        }
        if(StringUtils.isBlank(username)){
            res.put("paramErr","关键词不能为空");
            return res;
        }
        List<User> users = userService.searchByUsername(username);
        res.put("users",users);
        return res;
    }

    //拉黑或者取消拉黑
    @PostMapping("/block")
    public Map<String,Object> blockUser(int id,int status) {
        Map<String, Object> res = new HashMap<>();
        User u = hostHolder.get();
        //如果没有登录或者该用户是普通用户
        if (u == null || u.getType() == 0) {
            res.put("tokenErr", "1");
            return res;
        }
        userService.updateStatus(id, status);
        res.put("status", "ok");
        return res;
    }
    @GetMapping("/searchPosts")
    public Map<String,Object> searchById(Integer id,String keyword) {
        Map<String, Object> res = new HashMap<>();
        User u = hostHolder.get();
        //如果没有登录或者该用户是普通用户
        if (u == null || u.getType() == 0) {
            res.put("tokenErr", "1");
            return res;
        }
        if (id != null) {
            DiscussPost post = discussPostService.findDiscussPostById2(id);
            res.put("post", post);
        }
        //通过关键词查找title或content
        if (keyword != null) {
            List<DiscussPost> posts = discussPostService.search(keyword);
            res.put("posts", posts);
        }
        return res;
    }
    //置顶type=1或者取消置顶type=0
    @PostMapping("/top")
    public Map<String,Object> top(int id,int type) {
        Map<String, Object> res = new HashMap<>();
        User u = hostHolder.get();
        //如果没有登录或者该用户是普通用户
        if (u == null || u.getType() == 0) {
            res.put("tokenErr", "1");
            return res;
        }
        discussPostService.updateType(id,type);
        res.put("status", "ok");
        return res;
    }
    //删除status=2或者取消删除status=0
    @PostMapping("/del")
    public Map<String,Object> del(int id,int status) {
        Map<String, Object> res = new HashMap<>();
        User u = hostHolder.get();
        //如果没有登录或者该用户是普通用户
        if (u == null || u.getType() == 0) {
            res.put("tokenErr", "1");
            return res;
        }
        discussPostService.updateStatus(id,status);
        res.put("status", "ok");
        return res;
    }

    //    上传轮播图
    @PostMapping("/uploadCarousel")
    public Map<String,String> upload(Integer num, MultipartFile image) {
        Map<String,String> res = new HashMap<>();
        User u = hostHolder.get();
        if (u == null || u.getType() == 0) {
            res.put("tokenErr", "1");
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
        carouselService.updateImg(1,num,status);
        res.put("status","ok");
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
            throw new RuntimeException("图片上传失败，服务器发生错误: " + e.getMessage());
        }
        /**
         修改用户信息中的图片路径headerUrl : http://localhost:8080/forum/user/profile/xxx.jpg
         */
        String headerUrl = domain + context + "/user/profile/" + fileName;
        return headerUrl;
    }

    @GetMapping("/getCarousel")
    public Map<String,Object> getCarousel(){
        Map<String,Object> res = new HashMap<>();
        Carousel carousel = carouselService.selectImg();
        res.put("carousel",carousel);
        return res;
    }








//    @GetMapping("/carousel/{no}")
//    public void getProfile(@PathVariable("no") int no, HttpServletResponse response) {
//        String dest = uploadPath + "/" + fileName;
//        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);
//        response.setContentType("image/" + suffix);
//        try (
//                //获取响应流
//                OutputStream os = response.getOutputStream();
//                //读取本地文件
//                FileInputStream fis = new FileInputStream(dest)
//        ) {
//            byte[] buffer = new byte[1024];
//            int b = -1;
//            while ((b = fis.read(buffer)) != -1) {
//                os.write(buffer, 0, b);
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//    }

}
