package com.guangke.forum.controller;

import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.CommentService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.service.MessageService;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController implements ForumConstants {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @Autowired
    MessageService messageService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CommentService commentService;
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
        //触发点赞,自己给自己点赞不会通知
        if (entityUserId != user.getId() && likeStatus == 1) {
            Message message = new Message();
            message.setFromId(SYSTEM_USER);
            message.setToId(entityUserId);
            message.setConversationId(TOPIC_LIKE);
            message.setCreateTime(new Date());
            //content字段是固定的三个数据,实体类型+实体id+实体所属的用户id
            Map<String, Object> content = new HashMap<>();
            content.put("userId", user.getId());
            content.put("entityType", entityType);
            int postId;
            //如果是对评论点赞，找出该评论所属的帖子的id
            if(entityType==2){
                postId = commentService.findCommentById(entityId).getEntityId();
            }else{
                postId = entityId;
            }
            content.put("postId", postId);
            message.setContent(JSONObject.toJSONString(content));
            messageService.addMessage(message);
        }
        return map;
    }
}
