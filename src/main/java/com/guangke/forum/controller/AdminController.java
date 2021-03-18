package com.guangke.forum.controller;

import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
