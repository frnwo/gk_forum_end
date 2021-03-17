package com.guangke.forum.controller;

import com.guangke.forum.pojo.User;
import com.guangke.forum.service.FollowService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FollowController implements ForumConstants {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    @PostMapping(path = "/follow")
    public Map<String,Object> follow(int entityType, int entityId) {
        Map<String,Object> res = new HashMap<>();
        User user = hostHolder.get();
        if(user == null){
            res.put("tokenErr","1");
            return res;
        }
        followService.follow(user.getId(), entityType, entityId);
        res.put("status","ok");
        return res;
    }

    @PostMapping(path = "/unfollow")
    public Map<String,Object> unfollow(int entityType, int entityId) {
        Map<String,Object> res = new HashMap<>();
        User user = hostHolder.get();
        if(user == null){
            res.put("tokenErr","1");
            return res;
        }
        followService.unfollow(user.getId(), entityType, entityId);
        res.put("status","ok");
        return res;
    }

    @GetMapping(path = "/followees/{userId}")
    public Map<String,Object> getFollowees(@PathVariable("userId") int userId) {
        Map<String,Object> res = new HashMap<>();
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        List<Map<String, Object>> userList = followService.findFollowees(userId,0,100);
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hostHolder.get() == null ? false : followService.hasFollowed(hostHolder.get().getId(), ENTITY_TYPE_USER, u.getId()));
            }
        }

        res.put("users", userList);
        return res;
    }

    @GetMapping(path = "/followers/{userId}")
    public Map<String,Object> getFollowers(@PathVariable("userId") int userId) {
        Map<String,Object> res = new HashMap<>();
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        List<Map<String, Object>> userList = followService.findFollowers(userId, 0,100);
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hostHolder.get() == null ? false : followService.hasFollowed(hostHolder.get().getId(), ENTITY_TYPE_USER, u.getId()));
            }
        }
        res.put("users", userList);
        return res;
    }
}
