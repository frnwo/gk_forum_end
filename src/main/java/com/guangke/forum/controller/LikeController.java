package com.guangke.forum.controller;

import com.guangke.forum.pojo.User;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import com.guangke.forum.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController implements ForumConstants {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;


    @Autowired
    private RedisTemplate redisTemplate;

    //异步 点赞
    @PostMapping(path = "/like")
    public Map<String,Object> like(int entityType, int entityId, int entityUserId) {
        Map<String, Object> map = new HashMap<>();
        User user = hostHolder.get();
        if(user == null){
            map.put("tokenErr","1");
            return map;
        }
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //点赞数量
        long likeCount = likeService.findLikeCount(entityType, entityId);
        map.put("likeCount", likeCount);
        //点赞状态 1点赞 0未点赞
        int likeStatus = likeService.findLikeStatus(user.getId(), entityType, entityId);
        map.put("likeStatus", likeStatus);

        return map;
    }
}
